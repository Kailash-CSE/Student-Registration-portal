
package com.example.admission;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private boolean verifyPassword(String password, String storedHash)
            throws Exception {

        String[] parts = storedHash.split("\\$");

        if (parts.length != 4 ||
                !parts[0].equals("pbkdf2-sha256")) {
            return false;
        }

        int iterations = Integer.parseInt(parts[1]);

        // Reject malformed or unexpectedly expensive hash parameters.
        if (iterations < 10000 || iterations > 1000000) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

        KeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, iterations,
            expectedHash.length * 8
        );

        byte[] actualHash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .getEncoded();

        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String rollNumber = request.getParameter("roll_number");
        String password = request.getParameter("password");

        if (rollNumber == null || password == null ||
                rollNumber.isBlank() || password.isEmpty()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Enter your roll number and password."
            );
            return;
        }

        String sql = """
            SELECT id, full_name, password_hash
            FROM students
            WHERE roll_number = ?
            """;

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, rollNumber.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() ||
                        !verifyPassword(password, result.getString("password_hash"))) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().println(
                        "<!DOCTYPE html><html><body>" +
                        "<h2>Invalid roll number or password.</h2>" +
                        "<a href='login.html'>Try again</a>" +
                        "</body></html>"
                    );
                    return;
                }

                // Prevent session fixation after successful login.
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("studentId", result.getLong("id"));
                session.setAttribute("studentName", result.getString("full_name"));
                session.setMaxInactiveInterval(20 * 60);

                response.sendRedirect(
                    request.getContextPath() + "/students"
                );
            }

        } catch (Exception exception) {
            log("Login failed due to a server-side error.", exception);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to process login. Check the server logs."
            );
        }
    }
}
