<?php
// Server-side validation is essential; browser validation can be bypassed.
require __DIR__ . '/db.php';
function fail(string $message, int $status = 422): never {
    http_response_code($status);
    echo '<!doctype html><html lang="en"><meta charset="utf-8"><title>Registration</title><body style="font:16px Arial;padding:40px"><h2>Registration not completed</h2><p>' . htmlspecialchars($message, ENT_QUOTES, 'UTF-8') . '</p><p><a href="../index.html">Return to form</a></p></body></html>';
    exit;
}
if ($_SERVER['REQUEST_METHOD'] !== 'POST') fail('Please submit the registration form.', 405);
$name = trim($_POST['full_name'] ?? '');
$roll = trim($_POST['roll_number'] ?? '');
$email = trim($_POST['email'] ?? '');
$course = trim($_POST['course'] ?? '');
$password = $_POST['password'] ?? '';
$allowedCourses = ['B.E. Computer Science and Engineering','B.E. Electronics and Communication Engineering','B.Tech Information Technology','B.Tech Artificial Intelligence and Data Science','B.E. Mechanical Engineering'];
if (mb_strlen($name) < 2 || mb_strlen($name) > 120) fail('Enter a name between 2 and 120 characters.');
if (!preg_match('/^[A-Za-z0-9\/-]{3,20}$/', $roll)) fail('Roll number must contain 3–20 letters, numbers, slash or hyphen.');
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) fail('Enter a valid email address.');
if (!in_array($course, $allowedCourses, true)) fail('Select a valid course.');

if (
    strlen($password) < 8 ||
    !preg_match('/[A-Z]/', $password) ||
    !preg_match('/[a-z]/', $password) ||
    !preg_match('/\d/', $password) ||
    !preg_match('/[^A-Za-z0-9]/', $password)
) {
    fail('Password does not meet the strength requirements.');
}

if (!isset($_FILES['document']) || $_FILES['document']['error'] !== UPLOAD_ERR_OK) fail('Please upload a document.');
$file = $_FILES['document'];
if ($file['size'] > 2 * 1024 * 1024) fail('Document must be 2 MB or smaller.');
$finfo = new finfo(FILEINFO_MIME_TYPE);
$mime = $finfo->file($file['tmp_name']);
$allowedTypes = ['application/pdf'=>'pdf', 'image/jpeg'=>'jpg', 'image/png'=>'png'];
if (!isset($allowedTypes[$mime])) fail('Only PDF, JPG and PNG files are accepted.');
// Store uploads outside the public web root in production where possible.
$uploadDir = __DIR__ . '/../uploads';
if (!is_dir($uploadDir) && !mkdir($uploadDir, 0750, true)) fail('Could not create upload directory.', 500);
$storedName = bin2hex(random_bytes(16)) . '.' . $allowedTypes[$mime];
if (!move_uploaded_file($file['tmp_name'], $uploadDir . '/' . $storedName)) fail('Could not save uploaded file.', 500);
try {
    // Prepared statements prevent SQL injection; passwords are stored as hashes.
    $stmt = $pdo->prepare('INSERT INTO students (full_name, roll_number, email, course, password_hash, document_path) VALUES (?, ?, ?, ?, ?, ?)');
    $stmt->execute([$name, $roll, $email, $course, password_hash($password, PASSWORD_DEFAULT), 'uploads/' . $storedName]);
} catch (PDOException $e) {
    @unlink($uploadDir . '/' . $storedName);
    if ($e->getCode() === '23000') fail('That roll number or email is already registered.');
    fail('Could not save registration. Please try again later.', 500);
}
?><!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Registration successful</title><body style="font:16px Arial;background:#f3f6fa;padding:50px"><main style="max-width:560px;margin:auto;background:white;padding:36px;border-radius:16px"><h1 style="color:#218653">Registration submitted</h1><p>Thank you, <?= htmlspecialchars($name, ENT_QUOTES, 'UTF-8') ?>. Your details have been saved successfully.</p><p><a href="../index.html">Back to portal</a></p></main></body></html>
