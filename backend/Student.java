
public class Student {

    private String name;
    private String dob;
    private String email;
    private String mobile;
    private String college;
    private String branch;
    private String password;

    public Student(String name, String dob, String email,
                   String mobile, String college,
                   String branch, String password) {

        this.name = name;
        this.dob = dob;
        this.email = email;
        this.mobile = mobile;
        this.college = college;
        this.branch = branch;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getCollege() {
        return college;
    }

    public String getBranch() {
        return branch;
    }

    public String getPassword() {
        return password;
    }
}