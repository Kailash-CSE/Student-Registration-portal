
package com.example.admission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/register")
@MultipartConfig(
    maxFileSize = 2 * 1024 * 1024,
    maxRequestSize = 3 * 1024 * 1024
)
public class StudentRegistrationServlet extends HttpServlet {

    private static final Pattern EMAIL =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private static final Pattern ROLL =
        Pattern.compile("^[A-Za-z0-9/-]{3,20}$");

    private static final Set<String> COURSES = Set.of(
        "B.E. Computer Science and Engineering",
        "B.E. Electronics and Communication Engineering",
        "B.Tech Information Technology",
        "B.Tech Artificial Intelligence and Data Science",
        "B.E. Mechanical Engineering"
    );

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("full_name");
        String roll = request.getParameter("roll_number");
        String email = request.getParameter("email");
        String course = request.getParameter("course");
        String password = request.getParameter("password");

        // Validate registration details.
        if (name == null
                || name.trim().length() < 2
                || name.trim().length() > 120
                || roll == null
                || !ROLL.matcher(roll.trim()).matches()
                || email == null
                || !EMAIL.matcher(email.trim()).matches()
                || course == null
                || !COURSES.contains(course)
                || !strongPassword(password)) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid details. Check your name, roll number, email, course and password."
            );
            return;
        }

        // Validate uploaded document.
        Part document = request.getPart("document");

        if (document == null
                || document.getSize() == 0
                || document.getSize() > 2 * 1024 * 1024) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Upload a PDF, JPG or PNG file up to 2 MB."
            );
            return;
        }

        String type = document.getContentType();
        String extension;

        if ("application/pdf".equalsIgnoreCase(type)) {
            extension = ".pdf";
        } else if ("image/jpeg".equalsIgnoreCase(type)) {
            extension = ".jpg";
        } else if ("image/png".equalsIgnoreCase(type)) {
            extension = ".png";
        } else {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Only PDF, JPG and PNG files are accepted."
            );
            return;
        }

        // Save uploaded file using a generated filename.
        String uploadRoot = System.getenv().getOrDefault(
            "STUDENT_UPLOAD_DIR",
            System.getProperty("java.io.tmpdir") + "/student-uploads"
        );

        Path uploadDir = Path.of(uploadRoot);
        Files.createDirectories(uploadDir);

        String storedFile = UUID.randomUUID() + extension;
        Path savedFile = uploadDir.resolve(storedFile);

        try (var input = document.getInputStream()) {
            Files.copy(input, savedFile);
        } catch (IOException ex) {
            getServletContext().log("Document upload failed", ex);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Could not save the uploaded document."
            );
            return;
        }

        // Hash the password before storing it.
        String passwordHash;

        try {
            passwordHash = hashPassword(password);
        } catch (Exception ex) {
            getServletContext().log("Password hashing failed", ex);
            Files.deleteIfExists(savedFile);

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Could not process the password securely."
            );
            return;
        }

        // Database configuration.
        String dbUser = System.getenv().getOrDefault("DB_USER", "root");
String dbUrl = System.getenv().getOrDefault(
    "DB_URL",
    "jdbc:mysql://localhost:3306/student_registration"
);
String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "");
   

        String sql =
            "INSERT INTO students " +
            "(full_name, roll_number, email, course, password_hash, document_path) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

       try {
    Class.forName("com.mysql.cj.jdbc.Driver");

    try (
        Connection connection =
            DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        PreparedStatement statement =
            connection.prepareStatement(sql)
    ) {
        statement.setString(1, name.trim());
        statement.setString(2, roll.trim());
        statement.setString(3, email.trim());
        statement.setString(4, course);
        statement.setString(5, passwordHash);
        statement.setString(6, storedFile);

        statement.executeUpdate();

    }
} catch (Exception ex) {
    getServletContext().log(
        "Registration database save failed. Check DB_URL, DB_USER, DB_PASSWORD, table structure and duplicate values.",
        ex
    );

            try {
                Files.deleteIfExists(savedFile);
            } catch (IOException fileEx) {
                getServletContext().log(
                    "Could not delete the uploaded file after database failure.",
                    fileEx
                );
            }

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Registration could not be saved. Check the Tomcat logs."
            );
            return;
        }

        // Successful registration.
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(
            "<!doctype html>" +
            "<html><head><meta charset='utf-8'>" +
            "<title>Registration Complete</title></head>" +
            "<body><h1>Registration submitted successfully</h1>" +
            "<p>Your details have been saved.</p></body></html>"
        );
    }

    private String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            210000,
            256
        );

        try {
            byte[] hash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

            return "pbkdf2-sha256$210000$"
                + Base64.getEncoder().encodeToString(salt)
                + "$"
                + Base64.getEncoder().encodeToString(hash);
        } finally {
            spec.clearPassword();
        }
    }

    private boolean strongPassword(String password) {
        return password != null
            && password.length() >= 8
            && password.matches(".*[A-Z].*")
            && password.matches(".*[a-z].*")
            && password.matches(".*\\d.*")
            && password.matches(".*[^A-Za-z0-9].*");
    }
}
