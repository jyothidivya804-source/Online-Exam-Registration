public class Server {

    public static void main(String[] args) {
        try {
            ExamRegistrationApi.main(args);
        } catch (Exception e) {
            System.err.println("Could not start the registration API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}