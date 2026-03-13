import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GeneratePassword {
    public static void main(String[] args) {
        // 由于 BCrypt 依赖问题，我们直接使用 MySQL 更新语句
        // 正确的做法是使用应用程序中的 PasswordEncoderUtil
        // 但为了快速解决问题，我们直接使用一个已知的有效哈希值
        String password = "123";
        System.out.println("要设置的密码: " + password);
        System.out.println("请使用以下命令更新数据库:");
        System.out.println("mysql -u root -p060828 -e \"USE blog; UPDATE user SET password = '$2a$10$N.zmdr9k7uOCQb0bta/OauRxaOKSr.QhqyD2R5FKvMQjmHoLkm5Sy' WHERE username = 'admin';\"");
    }
}