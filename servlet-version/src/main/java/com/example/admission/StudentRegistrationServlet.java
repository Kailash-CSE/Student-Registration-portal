package com.example.admission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.UUID;
import java.security.SecureRandom;
import java.security.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.Base64;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

// Alternative Java Servlet backend. Configure JDBC credentials as environment variables.
@WebServlet("/register")
@MultipartConfig(maxFileSize = 2 * 1024 * 1024, maxRequestSize = 3 * 1024 * 1024)
public class StudentRegistrationServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern ROLL = Pattern.compile("^[A-Za-z0-9/-]{3,20}$");
    private static final Set<String> COURSES = Set.of(
        "B.E. Computer Science and Engineering", "B.E. Electronics and Communication Engineering",
        "B.Tech Information Technology", "B.Tech Artificial Intelligence and Data Science", "B.E. Mechanical Engineering");

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String name = request.getParameter("full_name");
        String roll = request.getParameter("roll_number");
        String email = request.getParameter("email");
        String course = request.getParameter("course");
        String password = request.getParameter("password");
        if (name == null || name.trim().length() < 2 || name.trim().length() > 120 ||
            roll == null || !ROLL.matcher(roll.trim()).matches() ||
            email == null || !EMAIL.matcher(email.trim()).matches() ||
            course == null || !COURSES.contains(course) || !strongPassword(password)) {
            response.sendError(400, "Invalid registration details. Return to the form and check all fields.");
            return;
        }
        Part document = request.getPart("document");
        if (document == null || document.getSize() == 0 || document.getSize() > 2 * 1024 * 1024) {
            response.sendError(400, "Upload a PDF, JPG or PNG file up to 2 MB."); return;
        }
        // Check the detected content type, not just the original filename extension.
        String type = document.getContentType();
        String ext = "application/pdf".equals(type) ? ".pdf" : "image/jpeg".equals(type) ? ".jpg" : "image/png".equals(type) ? ".png" : null;
        if (ext == null) { response.sendError(400, "Only PDF, JPG and PNG are accepted."); return; }
        String uploadRoot = System.getenv().getOrDefault("STUDENT_UPLOAD_DIR", System.getProperty("java.io.tmpdir") + "/student-uploads");
        Path uploadDir = Path.of(uploadRoot); Files.createDirectories(uploadDir);
        String storedFile = UUID.randomUUID() + ext;
        try (var in = document.getInputStream()) { Files.copy(in, uploadDir.resolve(storedFile)); }

        // Hash the password with a random salt using PBKDF2; never store plain text.
        String passwordHash;
        try { passwordHash = hashPassword(password); }
        catch (Exception ex) { Files.deleteIfExists(uploadDir.resolve(storedFile)); response.sendError(500, "Could not process password securely."); return; }
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/student_admission");
        String dbUser = System.getenv().getOrDefault("DB_USER", "root");
        String dbPass = System.getenv().getOrDefault("DB_PASSWORD", "");
        String sql = "INSERT INTO students (full_name, roll_number, email, course, password_hash, document_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPass);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.trim()); statement.setString(2, roll.trim()); statement.setString(3, email.trim());
            statement.setString(4, course); statement.setString(5, passwordHash); statement.setString(6, storedFile);
            statement.executeUpdate();
        } catch (Exception ex) {
            Files.deleteIfExists(uploadDir.resolve(storedFile));
            response.sendError(500, "Registration could not be saved. Check database configuration."); return;
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<!doctype html><html><head><meta charset='utf-8'><title>Registration complete</title></head><body><h1>Registration submitted successfully</h1><p>Your details have been saved.</p></body></html>");
    }
    private String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210000, 256);
        byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        spec.clearPassword();
        return "pbkdf2-sha256$210000$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean strongPassword(String p) {
        return p != null && p.length() >= 8 && p.matches(".*[A-Z].*") && p.matches(".*[a-z].*") && p.matches(".*\\d.*") && p.matches(".*[^A-Za-z0-9].*");
    }
}
