import java.sql.Connection;
import java.sql.PreparedStatement;

public class StudentDAO {

    public static boolean registerStudent(Student student) {

        String sql = "INSERT INTO students " +
                     "(name, dob, email, mobile, college, branch, password) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, student.getName());
            ps.setString(2, student.getDob());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getMobile());
            ps.setString(5, student.getCollege());
            ps.setString(6, student.getBranch());
            ps.setString(7, student.getPassword());

            ps.executeUpdate();

            ps.close();
            con.close();

            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }
}