
package com.example.admission;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/student-file")
public class StudentFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        // Require an authenticated session.
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.html"
            );
            return;
        }

        String idValue = request.getParameter("id");
        long studentId;

        try {
            studentId = Long.parseLong(idValue);
            if (studentId <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception exception) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid student ID."
            );
            return;
        }

        String storedFile;

        String sql = "SELECT document_path FROM students WHERE id = ?";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, studentId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Document not found."
                    );
                    return;
                }

                storedFile = result.getString("document_path");
            }

        } catch (Exception exception) {
            log("Could not retrieve document information.", exception);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to retrieve document."
            );
            return;
        }

        if (storedFile == null ||
                !storedFile.matches(
                    "[0-9a-fA-F-]{36}\\.(pdf|jpg|png)"
                )) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Invalid document reference."
            );
            return;
        }

        String uploadRoot = System.getenv().getOrDefault(
            "STUDENT_UPLOAD_DIR",
            System.getProperty("java.io.tmpdir") + "/student-uploads"
        );

        Path uploadDir = Paths.get(uploadRoot)
                              .toAbsolutePath()
                              .normalize();

        Path file = uploadDir.resolve(storedFile).normalize();

        // Prevent a path from escaping the upload directory.
        if (!file.startsWith(uploadDir) || !Files.isRegularFile(file)) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Document file is unavailable."
            );
            return;
        }

        String contentType;

        if (storedFile.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (storedFile.endsWith(".jpg")) {
            contentType = "image/jpeg";
        } else {
            contentType = "image/png";
        }

        response.setContentType(contentType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader(
            "Content-Disposition",
            "inline; filename=\"student-document." +
            storedFile.substring(storedFile.lastIndexOf('.') + 1) + "\""
        );
        response.setContentLengthLong(Files.size(file));

        try (InputStream input = Files.newInputStream(file);
             var output = response.getOutputStream()) {
            input.transferTo(output);
        }
    }
}
