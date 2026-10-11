
<?php
try {
    require __DIR__ . '/db.php';
    echo "Database connection successful.";
} catch (Throwable $e) {
    echo "Error type: " . get_class($e) . "<br>";
    echo "Error message: " .
        htmlspecialchars($e->getMessage(), ENT_QUOTES, 'UTF-8');
}
