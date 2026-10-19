# Student Admission Portal

Responsive student admission portal with two separate implementations that share the same registration design:
- **PHP + MySQL** for Q1/Q2: registration validation, CRUD pages, upload validation, and file metadata.
- **Jakarta Servlet + JDBC + JSP** for Q3: registration, login/session filter, student listing, and protected document streaming.

## Project structure
- `index.html`, `assets/style.css`, `assets/script.js` — shared Q1 registration UI and client-side validation.
- `php/` — PHP/PDO CRUD and file handling.
- `servlet-version/` — Maven WAR application for Tomcat 10.1.
- `servlet-version/src/main/webapp/assets/` — same CSS and JavaScript used by the Servlet registration page.
- `sql/schema.sql` — database and both `students` and `student_files` tables.
- `screenshots/` — supplied UI preview; capture fresh screenshots from your own running app for submission.

## Database setup
1. Run `sql/schema.sql` in MySQL. It creates/uses `student_registration`.
2. Configure PHP environment variables for Apache: `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.
3. Configure the Servlet Tomcat process with `DB_URL=jdbc:mysql://localhost:3306/student_registration`, `DB_USER`, `DB_PASSWORD`, and optionally `STUDENT_UPLOAD_DIR`.
4. Do not commit passwords or real student documents to GitHub. Use a fresh database password because an earlier local config contained a hard-coded password.

## Run PHP version
Serve the project through XAMPP/Apache and open `http://localhost/student-registration-portal (1)/student-registration/` (adjust the URL to the folder name in your `htdocs`). Registration submits to `php/register.php`; CRUD pages are `php/view.php`, `php/edit.php`, and `php/delete.php`.

## Run Servlet version
Build from `servlet-version` using `mvn clean package` and deploy `target/StudentRegistration.war` to Tomcat 10.1 `webapps`. Start Tomcat with the database environment variables set in the same process. Open `http://localhost:8080/StudentRegistration/`. Login is at `/StudentRegistration/login.html`, student list at `/StudentRegistration/students`.

The Servlet version uses its own upload directory (default: `${java.io.tmpdir}/student-uploads`), so PHP uploads and Servlet uploads are stored separately unless `STUDENT_UPLOAD_DIR` is deliberately configured.

## Security notes
Client-side validation is for usability; server-side validation remains mandatory. The Servlet student list is currently visible to any authenticated account, so use test data only and add administrator/student authorization before production use. For production, add CSRF protection, login rate limiting, and malware scanning for uploaded documents.
