
<?php
require __DIR__ . '/db.php';

function escapeHtml($value) {
    return htmlspecialchars((string) $value, ENT_QUOTES, 'UTF-8');
}

$id = filter_input(INPUT_GET, 'id', FILTER_VALIDATE_INT);

if (!$id || $id < 1) {
    http_response_code(400);
    exit('Invalid student ID.');
}

$stmt = $pdo->prepare(
    'SELECT id, full_name, roll_number
     FROM students WHERE id = ?'
);
$stmt->execute([$id]);
$student = $stmt->fetch();

if (!$student) {
    http_response_code(404);
    exit('Student not found.');
}

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (($_POST['confirm'] ?? '') !== 'yes') {
        header('Location: view.php');
        exit;
    }

    try {
        $delete = $pdo->prepare('DELETE FROM students WHERE id = ?');
        $delete->execute([$id]);

        header('Location: view.php?deleted=1');
        exit;
    } catch (PDOException $e) {
        error_log($e->getMessage());
        http_response_code(500);
        $error = 'Unable to delete the student. Please try again.';
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Delete Student</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            padding: 30px;
        }

        .container {
            max-width: 480px;
            margin: 60px auto;
            padding: 25px;
            background: white;
            border-radius: 10px;
        }

        .warning { color: #b42318; }

        button, .cancel {
            display: inline-block;
            margin-top: 15px;
            padding: 10px 16px;
            border-radius: 5px;
            text-decoration: none;
        }

        button {
            background: #b42318;
            color: white;
            border: 0;
            cursor: pointer;
        }

        .cancel {
            background: #e5e7eb;
            color: #222;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Delete Student</h1>

    <?php if ($error !== ''): ?>
        <p class="warning"><?= escapeHtml($error) ?></p>
    <?php else: ?>
        <p>Are you sure you want to delete this student?</p>
        <p><strong>Name:</strong> <?= escapeHtml($student['full_name']) ?></p>
        <p><strong>Roll Number:</strong> <?= escapeHtml($student['roll_number']) ?></p>

        <form method="POST">
            <input type="hidden" name="confirm" value="yes">
            <button type="submit">Confirm Delete</button>
            <a class="cancel" href="view.php">Cancel</a>
        </form>
    <?php endif; ?>
</div>
</body>
</html>
