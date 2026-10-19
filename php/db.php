<?php
// Keep database credentials outside source control. Configure these environment
// variables for Apache/PHP: DB_HOST, DB_NAME, DB_USER, DB_PASSWORD.
$host = getenv('DB_HOST') ?: 'localhost';
$db = getenv('DB_NAME') ?: 'student_registration';
$user = getenv('DB_USER') ?: 'root';
$pass = getenv('DB_PASSWORD') ?: '';
$charset = 'utf8mb4';
$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES => false,
];
try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (PDOException $e) {
    error_log('Database connection error: ' . $e->getMessage());
    http_response_code(500);
    exit('Database connection failed. Please check the server configuration.');
}
