
<?php
require __DIR__ . '/db.php';

try {
    $stmt = $pdo->query(
        "SELECT id, full_name, roll_number, email, course,
                document_path, created_at
         FROM students
         ORDER BY id DESC"
    );

    $students = $stmt->fetchAll();
} catch (PDOException $e) {
    error_log($e->getMessage());
    http_response_code(500);
    exit('Unable to load student records.');
}

function escapeHtml($value) {
    return htmlspecialchars(
        (string) $value,
        ENT_QUOTES,
        'UTF-8'
    );
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Records</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            margin: 0;
            padding: 30px;
            color: #222;
        }

        h1 {
            text-align: center;
            color: #1f3a5f;
        }

        .container {
            max-width: 1100px;
            margin: auto;
            background: white;
            padding: 24px;
            border-radius: 10px;
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        th, td {
            padding: 12px;
            border: 1px solid #ddd;
            text-align: left;
        }

        th {
            background: #1f3a5f;
            color: white;
        }

        tr:nth-child(even) {
            background: #f7f9fc;
        }

        a {
            color: #175cd3;
        }

        .empty {
            text-align: center;
            padding: 25px;
        }
    </style>
</head>

<body>
    <div class="container">
        <h1>Student Records</h1>

        <p>Total registered students:
            <strong><?= count($students) ?></strong>
        </p>

        <?php if (count($students) > 0): ?>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Roll Number</th>
                        <th>Email</th>
                        <th>Course</th>
                        <th>Document</th>
                        <th>Registered On</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <?php foreach ($students as $student): ?>
                        <tr>
                            <td><?= escapeHtml($student['id']) ?></td>
                            <td><?= escapeHtml($student['full_name']) ?></td>
                            <td><?= escapeHtml($student['roll_number']) ?></td>
                            <td><?= escapeHtml($student['email']) ?></td>
                            <td><?= escapeHtml($student['course']) ?></td>
                            <td>
                                    <a href="edit.php?id=<?= (int) $student['id'] ?>">Edit</a>
    |
                                 <a href="delete.php?id=<?= (int) $student['id'] ?>">Delete</a>
                            </td>

                            <td>
                                <?php if (!empty($student['document_path'])): ?>
                                    <a
                                        href="../uploads/<?= rawurlencode(basename($student['document_path'])) ?>"
                                        target="_blank"
                                        rel="noopener noreferrer"
                                    >
                                        View document
                                    </a>
                                <?php else: ?>
                                    No document
                                <?php endif; ?>
                            </td>
                            

                            <td><?= escapeHtml($student['created_at']) ?></td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        <?php else: ?>
            <p class="empty">No student records found.</p>
        <?php endif; ?>
    </div>
</body>
</html>
