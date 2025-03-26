package utils;

import cn.hutool.core.util.RandomUtil;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 *
 * 主要作用是对输入的密码进行加密处理。在加密过程中，
 * 会先生成一个随机的盐值，然后调用另一个重载的 encode 方法，
 * 将密码和生成的盐值作为参数传入进行加密，并返回加密后的结果
 */

public class PasswordEncoder {

    public static String encode(String password) {
        //生成盐
        String salt = RandomUtil.randomNumbers(20);
        //加密
        return encode(password,salt);
    }
    private static String encode(String password, String salt) {
        // 加密
        return salt + "@" + DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
    }
    public static Boolean match(String rawpassword, String encodedPassword) {
        if(encodedPassword == null || rawpassword == null) {
            return false;
        }
        if(!encodedPassword.contains("@")){
            throw new RuntimeException("密码格式不正确！");
        }
        String[] arr = encodedPassword.split("@");
        // 获取盐
        String salt = arr[0];
        // 比较
        return encodedPassword.equals(encode(salt,rawpassword));
    }

}
