import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        String password = "password123";
        String dbPassword = "$2a$10$GZOQ0g9UfYfsjK7EpOi9Ee53IRYuEGTx/J10YoXRwkG4JYAFqldze";
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("密码: " + password);
        System.out.println("数据库密码哈希: " + dbPassword);
        System.out.println("密码匹配: " + encoder.matches(password, dbPassword));
        System.out.println("新生成的哈希: " + encoder.encode(password));
    }
}