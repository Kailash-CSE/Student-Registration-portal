
package com.example.admission;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/students")
public class StudentListServlet extends HttpServlet {

    public static class Student {
        private final long id;
        private final String name;
        private final String rollNumber;
        private final String email;
        private final String course;

        public Student(long id, String name, String rollNumber,
                       String email, String course) {
            this.id = id;
            this.name = name;
            this.rollNumber = rollNumber;
            this.email = email;
            this.course = course;
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public String getRollNumber() { return rollNumber; }
        public String getEmail() { return email; }
        public String getCourse() { return course; }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        List<Student> students = new ArrayList<>();

        String sql = """
            SELECT id, full_name, roll_number, email, course
            FROM students
            ORDER BY id DESC
            """;

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                students.add(new Student(
                    result.getLong("id"),
                    result.getString("full_name"),
                    result.getString("roll_number"),
                    result.getString("email"),
                    result.getString("course")
                ));
            }

            request.setAttribute("students", students);
            request.getRequestDispatcher("/students.jsp")
                   .forward(request, response);

        } catch (Exception exception) {
            log("Could not load student records.", exception);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to load student records."
            );
        }
    }
}
