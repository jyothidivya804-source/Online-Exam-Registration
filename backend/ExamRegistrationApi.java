import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class ExamRegistrationApi {
    private static final String DB_URL = System.getenv().getOrDefault(
            "ONLINE_EXAM_DB_URL", "jdbc:mysql://localhost:3306/online_exam");
    private static final String DB_USER = System.getenv("ONLINE_EXAM_DB_USER");
    private static final String DB_PASSWORD = System.getenv("ONLINE_EXAM_DB_PASSWORD");
    private static final int PASSWORD_ITERATIONS = 210_000;
    private static final int PASSWORD_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, Integer> SESSIONS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        if (DB_USER == null || DB_USER.isBlank()
                || DB_PASSWORD == null || DB_PASSWORD.isBlank()) {
            throw new IllegalStateException(
                    "Set ONLINE_EXAM_DB_USER and ONLINE_EXAM_DB_PASSWORD before starting the server.");
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = connect()) {
            if (!connection.isValid(2)) {
                throw new SQLException("Could not verify the database connection.");
            }
            System.out.println("Database connected successfully.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", ExamRegistrationApi::handle);
        server.setExecutor(null);
        server.start();
        System.out.println("Online Exam Registration API running at http://localhost:8080");
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, error("Use POST for this endpoint."));
                return;
            }

            Map<String, String> data = readForm(exchange);
            switch (exchange.getRequestURI().getPath()) {
                case "/register":
                    register(exchange, data);
                    break;
                case "/login":
                    login(exchange, data);
                    break;
                case "/dashboard":
                    dashboard(exchange, data);
                    break;
                case "/logout":
                    logout(exchange, data);
                    break;
                case "/status":
                    status(exchange, data);
                    break;
                default:
                    respond(exchange, 404, error("Endpoint not found."));
            }
        } catch (IllegalArgumentException e) {
            respond(exchange, 400, error(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            int code = "23000".equals(e.getSQLState()) ? 409 : 500;
            String message = code == 409
                    ? "This email, roll number, or application is already registered."
                    : "The database request failed. Check the server configuration and logs.";
            respond(exchange, code, error(message));
        } catch (Exception e) {
            e.printStackTrace();
            respond(exchange, 500, error("The request could not be completed."));
        } finally {
            exchange.close();
        }
    }

    private static void register(HttpExchange exchange, Map<String, String> data)
            throws Exception {
        String firstName = required(data, "firstName", "First name");
        String lastName = required(data, "lastName", "Last name");
        String fatherName = required(data, "fatherName", "Father's name");
        LocalDate dob = parseDate(required(data, "dob", "Date of birth"), "Date of birth");
        if (!dob.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth must be in the past.");
        }
        String gender = required(data, "gender", "Gender");
        String mobile = required(data, "mobile", "Mobile number");
        if (!mobile.matches("[0-9+() -]{7,20}")) {
            throw new IllegalArgumentException("Enter a valid mobile number.");
        }
        String email = required(data, "email", "Email").toLowerCase();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        String password = required(data, "password", "Password");
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        String examName = required(data, "examName", "Examination");
        String examLevel = required(data, "examLevel", "Exam level");
        String examCenter = required(data, "examCenter", "Exam centre");
        String qualification = required(data, "qualification", "Qualification");
        String course = optional(data, "course");
        String institution = optional(data, "institution");
        String board = optional(data, "board");
        String academicScore = optional(data, "marks");
        String rollNumber = optional(data, "rollNumber");
        Integer passingYear = parseOptionalYear(optional(data, "passingYear"));

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        String hashText = hashPassword(password, salt);

        String applicationNumber = "APP" + LocalDate.now().getYear()
                + UUID.randomUUID().toString().replace("-", "")
                        .substring(0, 10).toUpperCase();

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                int examId;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT exam_id FROM competitive_exams WHERE exam_name = ? AND active = TRUE")) {
                    statement.setString(1, examName);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException("The selected examination is unavailable.");
                        }
                        examId = result.getInt("exam_id");
                    }
                }

                int studentId;
                String insertStudent = "INSERT INTO students "
                        + "(first_name, last_name, father_name, dob, gender, mobile, email, "
                        + "roll_number, qualification, institution, board_university, course, "
                        + "academic_score, passing_year, password_salt, password_hash) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(
                        insertStudent, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setString(3, fatherName);
                    statement.setDate(4, Date.valueOf(dob));
                    statement.setString(5, gender);
                    statement.setString(6, mobile);
                    statement.setString(7, email);
                    statement.setString(8, rollNumber);
                    statement.setString(9, qualification);
                    statement.setString(10, institution);
                    statement.setString(11, board);
                    statement.setString(12, course);
                    statement.setString(13, academicScore);
                    if (passingYear == null) {
                        statement.setNull(14, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(14, passingYear);
                    }
                    statement.setString(15, saltText);
                    statement.setString(16, hashText);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Student ID was not generated.");
                        }
                        studentId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO applications "
                                + "(application_number, student_id, exam_id, exam_level, "
                                + "exam_center, status) VALUES (?, ?, ?, ?, ?, 'Submitted')")) {
                    statement.setString(1, applicationNumber);
                    statement.setInt(2, studentId);
                    statement.setInt(3, examId);
                    statement.setString(4, examLevel);
                    statement.setString(5, examCenter);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }

        respond(exchange, 201, jsonObject(
                "applicationNumber", applicationNumber,
                "status", "Submitted",
                "message", "Registration submitted successfully."));
    }

    private static void login(HttpExchange exchange, Map<String, String> data)
            throws Exception {
        String email = required(data, "email", "Email").toLowerCase();
        String password = required(data, "password", "Password");
        String sql = "SELECT student_id, first_name, last_name, password_salt, password_hash "
                + "FROM students WHERE email = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !verifyPassword(password,
                        result.getString("password_salt"), result.getString("password_hash"))) {
                    respond(exchange, 401, error("Email or password is incorrect."));
                    return;
                }
                String token = UUID.randomUUID().toString();
                SESSIONS.put(token, result.getInt("student_id"));
                respond(exchange, 200, jsonObject(
                        "token", token,
                        "name", result.getString("first_name") + " " + result.getString("last_name")));
            }
        }
    }

    private static void dashboard(HttpExchange exchange, Map<String, String> data)
            throws Exception {
        int studentId = sessionStudentId(data);
        Map<String, String> student = new LinkedHashMap<>();
        List<Map<String, String>> applications = new ArrayList<>();
        String studentSql = "SELECT first_name, last_name, email, mobile, qualification, "
                + "course FROM students WHERE student_id = ?";
        try (Connection connection = connect();
             PreparedStatement studentStatement = connection.prepareStatement(studentSql)) {
            studentStatement.setInt(1, studentId);
            try (ResultSet result = studentStatement.executeQuery()) {
                if (!result.next()) {
                    SESSIONS.values().remove(studentId);
                    respond(exchange, 401, error("Please sign in again."));
                    return;
                }
                student.put("name", result.getString("first_name") + " " + result.getString("last_name"));
                student.put("email", result.getString("email"));
                student.put("mobile", result.getString("mobile"));
                student.put("qualification", result.getString("qualification"));
                student.put("course", result.getString("course"));
            }

            String applicationSql = "SELECT a.application_number, e.exam_name, a.exam_level, "
                    + "a.exam_center, a.application_date, a.status FROM applications a "
                    + "JOIN competitive_exams e ON e.exam_id = a.exam_id "
                    + "WHERE a.student_id = ? ORDER BY a.application_date DESC";
            try (PreparedStatement applicationStatement =
                    connection.prepareStatement(applicationSql)) {
                applicationStatement.setInt(1, studentId);
                try (ResultSet result = applicationStatement.executeQuery()) {
                    while (result.next()) {
                        Map<String, String> application = new LinkedHashMap<>();
                        application.put("applicationNumber", result.getString("application_number"));
                        application.put("examName", result.getString("exam_name"));
                        application.put("examLevel", result.getString("exam_level"));
                        application.put("examCenter", result.getString("exam_center"));
                        application.put("applicationDate", result.getString("application_date"));
                        application.put("status", result.getString("status"));
                        applications.add(application);
                    }
                }
            }
        }
        respond(exchange, 200, "{\"student\":" + json(student)
                + ",\"applications\":" + jsonList(applications) + "}");
    }

    private static void logout(HttpExchange exchange, Map<String, String> data) {
        String token = optional(data, "token");
        if (!token.isEmpty()) {
            SESSIONS.remove(token);
        }
        respond(exchange, 200, jsonObject("message", "Signed out."));
    }

    private static void status(HttpExchange exchange, Map<String, String> data)
            throws SQLException {
        String applicationNumber = required(data, "applicationNumber", "Application number");
        LocalDate dob = parseDate(required(data, "dob", "Date of birth"), "Date of birth");
        String sql = "SELECT a.application_number, CONCAT(s.first_name, ' ', s.last_name) AS name, "
                + "s.dob, e.exam_name, a.exam_level, a.exam_center, a.application_date, a.status "
                + "FROM applications a JOIN students s ON s.student_id = a.student_id "
                + "JOIN competitive_exams e ON e.exam_id = a.exam_id "
                + "WHERE a.application_number = ? AND s.dob = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, applicationNumber);
            statement.setDate(2, Date.valueOf(dob));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    respond(exchange, 404, error("No application matches that number and date of birth."));
                    return;
                }
                respond(exchange, 200, jsonObject(
                        "applicationNumber", result.getString("application_number"),
                        "candidateName", result.getString("name"),
                        "dob", result.getString("dob"),
                        "examName", result.getString("exam_name"),
                        "examLevel", result.getString("exam_level"),
                        "examCenter", result.getString("exam_center"),
                        "applicationDate", result.getString("application_date"),
                        "status", result.getString("status")));
            }
        }
    }

    private static int sessionStudentId(Map<String, String> data) {
        String token = required(data, "token", "Session");
        Integer studentId = SESSIONS.get(token);
        if (studentId == null) {
            throw new IllegalArgumentException("Your session has expired. Please sign in again.");
        }
        return studentId;
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        byte[] body;
        try (InputStream input = exchange.getRequestBody()) {
            body = input.readNBytes(65_537);
        }
        if (body.length > 65_536) {
            throw new IllegalArgumentException("Request is too large.");
        }
        Map<String, String> values = new LinkedHashMap<>();
        String encoded = new String(body, StandardCharsets.UTF_8);
        for (String pair : encoded.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(parts.length == 2 ? parts[1] : "",
                    StandardCharsets.UTF_8);
            values.put(key, value);
        }
        return values;
    }

    private static String required(Map<String, String> data, String key, String label) {
        String value = optional(data, key);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value;
    }

    private static String optional(Map<String, String> data, String key) {
        String value = data.get(key);
        return value == null ? "" : value.trim();
    }

    private static LocalDate parseDate(String value, String label) {
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(label + " must be a valid date.");
        }
    }

    private static Integer parseOptionalYear(String value) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            int year = Integer.parseInt(value);
            if (year < 1900 || year > LocalDate.now().getYear() + 1) {
                throw new NumberFormatException();
            }
            return year;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid passing year.");
        }
    }

    private static String hashPassword(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
                PASSWORD_ITERATIONS, PASSWORD_BYTES * 8);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean verifyPassword(String password, String saltText, String hashText)
            throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltText);
        byte[] expected = Base64.getDecoder().decode(hashText);
        byte[] actual = Base64.getDecoder().decode(hashPassword(password, salt));
        return MessageDigest.isEqual(expected, actual);
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void respond(HttpExchange exchange, int status, String response) {
        try {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        } catch (IOException e) {
            System.err.println("Could not send API response: " + e.getMessage());
        }
    }

    private static String error(String message) {
        return jsonObject("error", message);
    }

    private static String jsonObject(String... pairs) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            values.put(pairs[i], pairs[i + 1]);
        }
        return json(values);
    }

    private static String json(Map<String, String> values) {
        StringBuilder result = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!first) {
                result.append(',');
            }
            first = false;
            result.append(quote(entry.getKey())).append(':')
                    .append(quote(entry.getValue()));
        }
        return result.append('}').toString();
    }

    private static String jsonList(List<Map<String, String>> values) {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                result.append(',');
            }
            result.append(json(values.get(i)));
        }
        return result.append(']').toString();
    }

    private static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\b': result.append("\\b"); break;
                case '\f': result.append("\\f"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                default:
                    if (character < 0x20) {
                        result.append(String.format("\\u%04x", (int) character));
                    } else {
                        result.append(character);
                    }
            }
        }
        return result.append('"').toString();
    }
}
