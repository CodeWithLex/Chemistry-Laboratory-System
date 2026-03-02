public class TestMail {
    public static void main(String[] args) {
        try {
            Class.forName("javax.mail.Session");
            System.out.println("JavaMail is PRESENT on the classpath!");
        } catch (ClassNotFoundException e) {
            System.out.println("JavaMail is MISSING from the classpath!");
        }
    }
}
