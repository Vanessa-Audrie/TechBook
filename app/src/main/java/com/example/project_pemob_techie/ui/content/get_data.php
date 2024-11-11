<?php
header('Content-Type: application/json');

// Database connection
$servername = "localhost";
$username = "test";
$password = "123456";
$dbname = "techbook-techie";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "SELECT * FROM book_products";
$result = $conn->query($sql);

$books = array();
while ($row = $result->fetch_assoc()) {
    $book = array(
        'isbn' => $row['isbn'],
        'book_title' => $row['book_title'],
        'price' => $row['price'],
        'image' => base64_encode($row['book_img']) // Convert binary image data to base64 string
    );
    array_push($books, $book);
}

echo json_encode($books);

$conn->close();
?>
