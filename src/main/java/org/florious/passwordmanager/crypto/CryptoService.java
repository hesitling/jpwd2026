package org.florious.passwordmanager.crypto;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密服务类
 * 提供密码哈希、加密解密、密钥派生等功能
 */
public class CryptoService {
    // AES-GCM-256 配置
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_LENGTH = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 16;

    // Argon2id 配置
    private static final int ARGON2_MEMORY = 65536; // 64 MB
    private static final int ARGON2_ITERATIONS = 3;
    private static final int ARGON2_PARALLELISM = 1;
    private static final int ARGON2_HASH_LENGTH = 32;

    private final SecureRandom secureRandom;

    public CryptoService() {
        this.secureRandom = new SecureRandom();
    }

    // ==================== Argon2id 哈希方法 ====================

    /**
     * 使用Argon2id算法对主密码进行哈希
     * @param password 明文密码
     * @param salt 盐值
     * @return 哈希后的密码（包含参数的完整哈希字符串）
     */
    public String hashMasterPassword(String password, String salt) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("盐值不能为空");
        }

        try {
            // 创建Argon2id函数
            Argon2Function argon2 = Argon2Function.getInstance(
                    ARGON2_MEMORY,
                    ARGON2_ITERATIONS,
                    ARGON2_PARALLELISM,
                    ARGON2_HASH_LENGTH,
                    Argon2.ID,  // Argon2id
                    19          // 版本19
            );

            Hash hash = Password.hash(password)
                    .addSalt(salt)
                    .with(argon2);

            return hash.getResult();
        } catch (Exception e) {
            throw new CryptoException("Argon2id哈希失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证主密码是否匹配
     * @param password 明文密码
     * @param hash 存储的哈希值（包含参数的完整哈希字符串）
     * @param salt 盐值
     * @return 如果密码匹配返回true
     */
    public boolean verifyMasterPassword(String password, String hash, String salt) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (hash == null || hash.isEmpty()) {
            throw new IllegalArgumentException("哈希值不能为空");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("盐值不能为空");
        }

        try {
            // 从哈希中提取参数
            Argon2Function argon2 = Argon2Function.getInstanceFromHash(hash);

            return Password.check(password, hash)
                    .addSalt(salt)
                    .with(argon2);
        } catch (Exception e) {
            throw new CryptoException("密码验证失败: " + e.getMessage(), e);
        }
    }

    // ==================== AES-GCM-256 加密方法 ====================

    /**
     * 使用AES-GCM-256加密密码
     * @param plainPassword 明文密码
     * @param key 加密密钥（32字节）
     * @return 加密结果，格式：IV(12字节) + 密文，Base64编码
     */
    public String encryptPassword(String plainPassword, byte[] key) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("密钥必须为32字节");
        }

        try {
            // 生成随机IV
            byte[] iv = generateIV();

            // 创建密钥
            SecretKey secretKey = new SecretKeySpec(key, "AES");

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // 加密
            byte[] encrypted = cipher.doFinal(plainPassword.getBytes());

            // 组合IV和密文
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new CryptoException("AES-GCM-256加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用AES-GCM-256解密密码
     * @param encryptedPassword 加密的密码（Base64编码）
     * @param key 解密密钥（32字节）
     * @return 解密后的明文密码
     */
    public String decryptPassword(String encryptedPassword, byte[] key) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            throw new IllegalArgumentException("加密密码不能为空");
        }
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("密钥必须为32字节");
        }

        try {
            // 解码Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedPassword);

            // 提取IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);

            // 提取密文
            byte[] encrypted = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.length);

            // 创建密钥
            SecretKey secretKey = new SecretKeySpec(key, "AES");

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // 解密
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted);
        } catch (Exception e) {
            throw new CryptoException("AES-GCM-256解密失败: " + e.getMessage(), e);
        }
    }

    // ==================== 密钥派生方法 ====================

    /**
     * 从主密码派生加密密钥
     * @param masterPassword 主密码
     * @param salt 盐值
     * @return 派生的32字节密钥
     */
    public byte[] deriveKey(String masterPassword, String salt) {
        if (masterPassword == null || masterPassword.isEmpty()) {
            throw new IllegalArgumentException("主密码不能为空");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("盐值不能为空");
        }

        try {
            // 创建Argon2id函数用于密钥派生
            Argon2Function argon2 = Argon2Function.getInstance(
                    ARGON2_MEMORY,
                    ARGON2_ITERATIONS,
                    ARGON2_PARALLELISM,
                    AES_KEY_LENGTH / 8,  // 32字节
                    Argon2.ID,
                    19
            );

            Hash hash = Password.hash(masterPassword)
                    .addSalt(salt)
                    .with(argon2);

            // 获取哈希结果的字节数组
            String hashResult = hash.getResult();
            byte[] hashBytes = hashResult.getBytes();

            // 确保密钥长度为32字节
            byte[] key = new byte[32];
            System.arraycopy(hashBytes, 0, key, 0, Math.min(hashBytes.length, 32));

            return key;
        } catch (Exception e) {
            throw new CryptoException("密钥派生失败: " + e.getMessage(), e);
        }
    }

    // ==================== 安全随机数生成方法 ====================

    /**
     * 生成随机盐值
     * @return Base64编码的盐值
     */
    public String generateSalt() {
        byte[] saltBytes = new byte[SALT_LENGTH];
        secureRandom.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * 生成随机IV
     * @return 12字节的IV
     */
    public byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * 生成指定长度的随机字节数组
     * @param length 字节数组长度
     * @return 随机字节数组
     */
    public byte[] generateRandomBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }

        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    // ==================== 辅助方法 ====================

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }

        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 清除敏感数据
     * @param data 敏感数据数组
     */
    public static void clearSensitiveData(byte[] data) {
        if (data != null) {
            java.util.Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * 清除敏感数据
     * @param data 敏感数据字符串
     */
    public static void clearSensitiveData(char[] data) {
        if (data != null) {
            java.util.Arrays.fill(data, '\0');
        }
    }
}