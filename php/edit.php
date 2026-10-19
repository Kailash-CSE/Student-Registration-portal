
<?php
require __DIR__ . '/db.php';

function escapeHtml($value) {
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}

$courses = [
    'B.E. Computer Science and Engineering',
    'B.E. Electronics and Communication Engineering',
    'B.E. Electrical and Electronics Engineering',
    'B.Tech. Information Technology',
    'B.Tech. Artificial Intelligence and Data Science',
    'Other'
];

$error = '';
$student = null;

$id = filter_input(INPUT_GET, 'id', FILTER_VALIDATE_INT);

if (!$id || $id < 1) {
    http_response_code(400);
    exit('Invalid student ID.');
}

$stmt = $pdo->prepare(
    'SELECT id, full_name, roll_number, email, course
     FROM students WHERE id = ?'
);
$stmt->execute([$id]);
$student = $stmt->fetch();

if (!$student) {
    http_response_code(404);
    exit('Student not found.');
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = trim($_POST['full_name'] ?? '');
    $roll = trim($_POST['roll_number'] ?? '');
    $email = trim($_POST['email'] ?? '');
    $course = trim($_POST['course'] ?? '');

    if ($name === '' || $roll === '' || $email === '' || $course === '') {
        $error = 'All fields are required.';
    } elseif (mb_strlen($name) > 120 || mb_strlen($roll) > 20) {
        $error = 'Name or roll number is too long.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($email) > 254) {
        $error = 'Enter a valid email address.';
    } elseif (!in_array($course, $courses, true)) {
        $error = 'Select a valid course.';
    } else {
        try {
            $update = $pdo->prepare(
                'UPDATE students
                 SET full_name = ?, roll_number = ?, email = ?, course = ?
                 WHERE id = ?'
            );
            $update->execute([$name, $roll, $email, $course, $id]);

            header('Location: view.php?updated=1');
            exit;
        } catch (PDOException $e) {
            if ($e->getCode() === '23000') {
                $error = 'That roll number or email may already exist.';
            } else {
                error_log($e->getMessage());
                $error = 'Unable to update the student. Please try again.';
            }
        }
    }

    // Preserve submitted values if validation fails.
    $student['full_name'] = $name;
    $student['roll_number'] = $roll;
    $student['email'] = $email;
    $student['course'] = $course;
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Student</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            padding: 30px;
            color: #222;
        }

        .container {
            max-width: 520px;
            margin: auto;
            background: white;
            padding: 25px;
            border-radius: 10px;
        }

        h1 { color: #1f3a5f; }

        label {
            display: block;
            margin-top: 15px;
            font-weight: bold;
        }

        input, select {
            width: 100%;
            box-sizing: border-box;
            padding: 10px;
            margin-top: 6px;
        }

        button {
            margin-top: 20px;
            padding: 11px 18px;
            background: #1f3a5f;
            color: white;
            border: 0;
            border-radius: 5px;
            cursor: pointer;
        }

        .error { color: #b42318; }
    </style>
</head>
<body>
<div class="container">
    <h1>Edit Student</h1>

    <?php if ($error !== ''): ?>
        <p class="error"><?= escapeHtml($error) ?></p>
    <?php endif; ?>

    <form method="POST">
        <label for="full_name">Full Name</label>
        <input id="full_name" name="full_name" maxlength="120"
               value="<?= escapeHtml($student['full_name']) ?>" required>

        <label for="roll_number">Roll Number</label>
        <input id="roll_number" name="roll_number" maxlength="20"
               value="<?= escapeHtml($student['roll_number']) ?>" required>

        <label for="email">Email</label>
        <input id="email" type="email" name="email" maxlength="254"
               value="<?= escapeHtml($student['email']) ?>" required>

        <label for="course">Course</label>
        <select id="course" name="course" required>
            <option value="">Select a course</option>
            <?php foreach ($courses as $courseOption): ?>
                <option value="<?= escapeHtml($courseOption) ?>"
                    <?= $student['course'] === $courseOption ? 'selected' : '' ?>>
                    <?= escapeHtml($courseOption) ?>
                </option>
            <?php endforeach; ?>
        </select>

        <button type="submit">Save Changes</button>
        <a href="view.php">Cancel</a>
    </form>
</div>
</body>
</html>
