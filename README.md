# Student Admission Portal

Responsive student registration form built with HTML, CSS and JavaScript, with two alternative server-side implementations: PHP/PDO and Java Jakarta Servlet. The shared MySQL schema is in `sql/schema.sql`.

## Files
- `index.html` — form controls: name, roll number, email, course, password, document upload and confirmation.
- `assets/style.css` — responsive layout, focus states, field errors, upload area and password-strength bar.
- `assets/script.js` — client-side validation and demo submission message.
- `php/` — PHP/PDO backend; validates input again, checks upload MIME/size, hashes passwords, and uses prepared statements.
- `servlet-version/` — alternative Jakarta Servlet backend and `WEB-INF/web.xml` deployment descriptor.
- `sql/schema.sql` — MySQL database and table.
- `screenshots/registration-form.png` — browser screenshot of the form.

## Run the frontend demo
Open `index.html` in a browser. The form demonstrates client-side validation; it displays a demo message rather than saving records.

## Run PHP + MySQL
1. Install PHP with `pdo_mysql`, `fileinfo`, and `mbstring` enabled, plus MySQL.
2. Run `sql/schema.sql` in MySQL.
3. Update credentials in `php/db.php`.
4. Serve the project directory with PHP's development server from the project root: `php -S localhost:8000`.
5. Open `http://localhost:8000/index.html`. The PHP form action saves registrations into MySQL.

## Run the Servlet version
Deploy the web app to a Jakarta EE 10-compatible servlet container (such as Tomcat 10.1), add the Jakarta Servlet API as a provided dependency and the MySQL Connector/J driver, and package `index.html` plus `assets/` into the web root. Change the form action to `/YOUR_CONTEXT_PATH/register` for this version. Set `DB_URL`, `DB_USER`, `DB_PASSWORD`, and optionally `STUDENT_UPLOAD_DIR` in the server environment. Run the SQL schema first.

## Important security note
The PHP implementation uses `password_hash()`. The Java servlet uses salted PBKDF2-HMAC-SHA256 for password hashing. For production, also add CSRF protection, rate limiting, malware scanning for uploaded documents, and keep uploaded files outside the public web root. Client-side validation improves usability but never replaces server-side validation.

## Screenshot
`screenshots/registration-form-preview.png` is a visual preview of the intended browser layout. For an actual browser screenshot for submission, run the page locally and use your browser's screenshot tool (Windows: `Win + Shift + S`) after opening `index.html` or the PHP-served URL.
