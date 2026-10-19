
<%@ page import="java.util.List" %>
<%@ page import="com.example.admission.StudentListServlet.Student" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
%>
<%
    List<Student> students =
        (List<Student>) request.getAttribute("students");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Student Records</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #eef2f7;
            margin: 0;
            padding: 24px;
        }
        .container {
            max-width: 1100px;
            margin: auto;
            background: white;
            padding: 24px;
            border-radius: 12px;
            box-shadow: 0 4px 15px #0002;
        }
        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
        }
        h1 { color: #173b70; }
        a.button {
            display: inline-block;
            background: #173b70;
            color: white;
            text-decoration: none;
            padding: 10px 14px;
            border-radius: 5px;
        }
        .table-wrap { overflow-x: auto; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 18px;
        }
        th, td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
            text-align: left;
        }
        th { background: #173b70; color: white; }
        tr:hover { background: #f5f8fc; }
    </style>
</head>
<body>
<div class="container">
    <header>
        <div>
            <h1>Student Records</h1>
            <p>Welcome, <%= escapeHtml(
                String.valueOf(session.getAttribute("studentName"))
            ) %></p>
        </div>
        <div>
            <a class="button" href="<%= request.getContextPath() %>/index.html">
                Register Student
            </a>
            <a class="button" href="<%= request.getContextPath() %>/logout">
                Logout
            </a>
        </div>
    </header>

    <div class="table-wrap">
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Roll Number</th>
                <th>Email</th>
                <th>Course</th>
                <th>Document</th>
            </tr>
            </thead>
            <tbody>
            <% if (students != null && !students.isEmpty()) {
                   for (Student student : students) { %>
                <tr>
                    <td><%= student.getId() %></td>
                    <td><%= escapeHtml(student.getName()) %></td>
                    <td><%= escapeHtml(student.getRollNumber()) %></td>
                    <td><%= escapeHtml(student.getEmail()) %></td>
                    <td><%= escapeHtml(student.getCourse()) %></td>
                    <td>
                        <a href="<%= request.getContextPath() %>/student-file?id=<%= student.getId() %>">
                            View document
                        </a>
                    </td>
                </tr>
            <%     }
               } else { %>
                <tr><td colspan="6">No student records found.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
