
<?php
ini_set('display_errors', '1');
ini_set('display_startup_errors', '1');
ini_set('log_errors', '1');
ini_set('error_log', __DIR__ . '/register_errors.log');
error_reporting(E_ALL);

require __DIR__ . '/db.php';

function fail(string $message, int $status = 422): never {
    http_response_code($status);
    echo '<!doctype html>
    <html lang="en">
    <meta charset="utf-8">
    <title>Registration</title>
    <body style="font:16px Arial;padding:40px">
        <h2>Registration not completed</h2>
        <p>' . htmlspecialchars($message, ENT_QUOTES, 'UTF-8') . '</p>
        <p><a href="../index.html">Return to form</a></p>
    </body>
    </html>';
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail('Please submit the registration form.', 405);
}

// 1. Read and validate student details
$name = trim($_POST['full_name'] ?? '');
$roll = trim($_POST['roll_number'] ?? '');
$email = trim($_POST['email'] ?? '');
$course = trim($_POST['course'] ?? '');
$password = $_POST['password'] ?? '';

$allowedCourses = [
    'B.E. Computer Science and Engineering',
    'B.E. Electronics and Communication Engineering',
    'B.Tech Information Technology',
    'B.Tech Artificial Intelligence and Data Science',
    'B.E. Mechanical Engineering'
];

if (mb_strlen($name) < 2 || mb_strlen($name) > 120) {
    fail('Enter a name between 2 and 120 characters.');
}

if (!preg_match('/^[A-Za-z0-9\/-]{3,20}$/', $roll)) {
    fail('Roll number must contain 3–20 letters, numbers, slash or hyphen.');
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($email) > 254) {
    fail('Enter a valid email address.');
}

if (!in_array($course, $allowedCourses, true)) {
    fail('Select a valid course.');
}

// Correct password validation

if (
    strlen($password) < 8 ||
    !preg_match('/[A-Z]/', $password) ||
    !preg_match('/[a-z]/', $password) ||
    !preg_match('/\d/', $password) ||
    !preg_match('/[^A-Za-z0-9]/', $password)
) {
    fail('Password must contain at least 8 characters, uppercase and lowercase letters, a digit, and a special character.');
}


// 2. Validate uploaded document
if (!isset($_FILES['document'])) {
    fail('Please upload a document.');
}

$file = $_FILES['document'];

if ($file['error'] !== UPLOAD_ERR_OK) {
    fail('The upload failed. Please select a document and try again.');
}

if (!is_uploaded_file($file['tmp_name'])) {
    fail('Invalid uploaded file.');
}

if ($file['size'] <= 0 || $file['size'] > 2 * 1024 * 1024) {
    fail('Document must be greater than 0 bytes and no larger than 2 MB.');
}

$finfo = new finfo(FILEINFO_MIME_TYPE);
$mime = $finfo->file($file['tmp_name']);

$allowedTypes = [
    'application/pdf' => 'pdf',
    'image/jpeg' => 'jpg',
    'image/png' => 'png'
];

if (!isset($allowedTypes[$mime])) {
    fail('Only PDF, JPG and PNG files are accepted.');
}

// 3. Prepare a unique filename
$uploadDir = __DIR__ . '/../uploads';

if (!is_dir($uploadDir) &&
    !mkdir($uploadDir, 0750, true) &&
    !is_dir($uploadDir)) {
    fail('Could not create upload directory.', 500);
}

if (!is_writable($uploadDir)) {
    fail('Upload directory is not writable.', 500);
}

$storedName = bin2hex(random_bytes(16)) . '.' . $allowedTypes[$mime];
$destination = $uploadDir . '/' . $storedName;
$filePath = 'uploads/' . $storedName;

// Keep the original filename as metadata, not as the saved filename.
$originalName = basename(str_replace('\\', '/', $file['name']));
$originalName = mb_substr($originalName, 0, 255);

if (!move_uploaded_file($file['tmp_name'], $destination)) {
    fail('Could not save uploaded file.', 500);
}

// 4. Insert student and file metadata together
try {
    $pdo->beginTransaction();

    $stmt = $pdo->prepare(
        'INSERT INTO students
         (full_name, roll_number, email, course, password_hash, document_path)
         VALUES (?, ?, ?, ?, ?, ?)'
    );

    $stmt->execute([
        $name,
        $roll,
        $email,
        $course,
        password_hash($password, PASSWORD_DEFAULT),
        $filePath
    ]);

    $studentId = (int) $pdo->lastInsertId();

    $fileStmt = $pdo->prepare(
        'INSERT INTO student_files
         (student_id, original_name, stored_name, file_path, file_type, file_size)
         VALUES (?, ?, ?, ?, ?, ?)'
    );

    $fileStmt->execute([
        $studentId,
        $originalName,
        $storedName,
        $filePath,
        $mime,
        $file['size']
    ]);

    $pdo->commit();

} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }

    if (is_file($destination)) {
        unlink($destination);
    }

    error_log('Registration error: ' . $e->getMessage());

    if ($e->getCode() === '23000') {
        fail('That roll number or email is already registered.');
    }

    fail('Could not save registration. Please try again later.', 500);

} catch (Throwable $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }

    if (is_file($destination)) {
        unlink($destination);
    }

    error_log('Registration error: ' . $e->getMessage());
    fail('Could not complete registration. Please try again.', 500);
}
?>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Registration Successful</title>
</head>
<body style="font:16px Arial;background:#f3f6fa;padding:50px">
    <main style="max-width:560px;margin:auto;background:white;padding:36px;border-radius:16px">
        <h1 style="color:#218653">Registration submitted</h1>
        <p>Thank you,
            <?= htmlspecialchars($name, ENT_QUOTES, 'UTF-8') ?>.
            Your details and document metadata have been saved successfully.
        </p>
        <p><a href="../index.html">Back to portal</a></p>
        <p><a href="view.php">View student records</a></p>
    </main>
</body>
</html>
