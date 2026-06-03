package org.florious.passwordmanager.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CryptoService单元测试
 */
@DisplayName("CryptoService 测试")
class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
    }

    @Nested
    @DisplayName("Argon2id 哈希测试")
    class Argon2idTests {

        @Test
        @DisplayName("应该成功哈希主密码")
        void shouldHashMasterPassword() {
            // Given
            String password = "MySecurePassword123!";
            String salt = cryptoService.generateSalt();

            // When
            String hash = cryptoService.hashMasterPassword(password, salt);

            // Then
            assertNotNull(hash);
            assertFalse(hash.isEmpty());
            assertNotEquals(password, hash);
        }

        @Test
        @DisplayName("相同密码和盐值应该产生相同哈希")
        void shouldProduceSameHashForSamePasswordAndSalt() {
            // Given
            String password = "MySecurePassword123!";
            String salt = cryptoService.generateSalt();

            // When
            String hash1 = cryptoService.hashMasterPassword(password, salt);
            String hash2 = cryptoService.hashMasterPassword(password, salt);

            // Then
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("不同盐值应该产生不同哈希")
        void shouldProduceDifferentHashForDifferentSalt() {
            // Given
            String password = "MySecurePassword123!";
            String salt1 = cryptoService.generateSalt();
            String salt2 = cryptoService.generateSalt();

            // When
            String hash1 = cryptoService.hashMasterPassword(password, salt1);
            String hash2 = cryptoService.hashMasterPassword(password, salt2);

            // Then
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("应该成功验证正确密码")
        void shouldVerifyCorrectPassword() {
            // Given
            String password = "MySecurePassword123!";
            String salt = cryptoService.generateSalt();
            String hash = cryptoService.hashMasterPassword(password, salt);

            // When
            boolean result = cryptoService.verifyMasterPassword(password, hash, salt);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("应该拒绝错误密码")
        void shouldRejectIncorrectPassword() {
            // Given
            String password = "MySecurePassword123!";
            String wrongPassword = "WrongPassword456!";
            String salt = cryptoService.generateSalt();
            String hash = cryptoService.hashMasterPassword(password, salt);

            // When
            boolean result = cryptoService.verifyMasterPassword(wrongPassword, hash, salt);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("应该抛出异常当密码为空")
        void shouldThrowExceptionWhenPasswordIsEmpty() {
            // Given
            String salt = cryptoService.generateSalt();

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.hashMasterPassword("", salt);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.hashMasterPassword(null, salt);
            });
        }

        @Test
        @DisplayName("应该抛出异常当盐值为空")
        void shouldThrowExceptionWhenSaltIsEmpty() {
            // Given
            String password = "MySecurePassword123!";

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.hashMasterPassword(password, "");
            });

            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.hashMasterPassword(password, null);
            });
        }
    }

    @Nested
    @DisplayName("AES-GCM-256 加密测试")
    class AesGcm256Tests {

        @Test
        @DisplayName("应该成功加密和解密密码")
        void shouldEncryptAndDecryptPassword() {
            // Given
            String plainPassword = "MySecretPassword123!";
            String salt = cryptoService.generateSalt();
            String masterPassword = "MasterPassword456!";
            byte[] key = cryptoService.deriveKey(masterPassword, salt);

            // When
            String encrypted = cryptoService.encryptPassword(plainPassword, key);
            String decrypted = cryptoService.decryptPassword(encrypted, key);

            // Then
            assertNotNull(encrypted);
            assertFalse(encrypted.isEmpty());
            assertNotEquals(plainPassword, encrypted);
            assertEquals(plainPassword, decrypted);
        }

        @Test
        @DisplayName("相同密码应该产生不同密文（由于随机IV）")
        void shouldProduceDifferentCiphertextForSamePassword() {
            // Given
            String plainPassword = "MySecretPassword123!";
            String salt = cryptoService.generateSalt();
            String masterPassword = "MasterPassword456!";
            byte[] key = cryptoService.deriveKey(masterPassword, salt);

            // When
            String encrypted1 = cryptoService.encryptPassword(plainPassword, key);
            String encrypted2 = cryptoService.encryptPassword(plainPassword, key);

            // Then
            assertNotEquals(encrypted1, encrypted2);

            // 但两者都应该能解密为相同明文
            String decrypted1 = cryptoService.decryptPassword(encrypted1, key);
            String decrypted2 = cryptoService.decryptPassword(encrypted2, key);
            assertEquals(plainPassword, decrypted1);
            assertEquals(plainPassword, decrypted2);
        }

        @Test
        @DisplayName("应该抛出异常当使用错误密钥解密")
        void shouldThrowExceptionWhenDecryptingWithWrongKey() {
            // Given
            String plainPassword = "MySecretPassword123!";
            String salt1 = cryptoService.generateSalt();
            String salt2 = cryptoService.generateSalt();
            String masterPassword = "MasterPassword456!";
            byte[] key1 = cryptoService.deriveKey(masterPassword, salt1);
            byte[] key2 = cryptoService.deriveKey(masterPassword, salt2);

            String encrypted = cryptoService.encryptPassword(plainPassword, key1);

            // When & Then
            assertThrows(CryptoException.class, () -> {
                cryptoService.decryptPassword(encrypted, key2);
            });
        }

        @Test
        @DisplayName("应该抛出异常当密码为空")
        void shouldThrowExceptionWhenPasswordIsEmptyForEncryption() {
            // Given
            String salt = cryptoService.generateSalt();
            String masterPassword = "MasterPassword456!";
            byte[] key = cryptoService.deriveKey(masterPassword, salt);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.encryptPassword("", key);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.encryptPassword(null, key);
            });
        }

        @Test
        @DisplayName("应该抛出异常当密钥长度不正确")
        void shouldThrowExceptionWhenKeyLengthIsIncorrect() {
            // Given
            String plainPassword = "MySecretPassword123!";
            byte[] shortKey = new byte[16]; // 太短
            byte[] longKey = new byte[64];  // 太长

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.encryptPassword(plainPassword, shortKey);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.encryptPassword(plainPassword, longKey);
            });
        }
    }

    @Nested
    @DisplayName("密钥派生测试")
    class KeyDerivationTests {

        @Test
        @DisplayName("应该派生正确长度的密钥")
        void shouldDeriveKeyWithCorrectLength() {
            // Given
            String masterPassword = "MasterPassword456!";
            String salt = cryptoService.generateSalt();

            // When
            byte[] key = cryptoService.deriveKey(masterPassword, salt);

            // Then
            assertNotNull(key);
            assertEquals(32, key.length); // 256位 = 32字节
        }

        @Test
        @DisplayName("相同密码和盐值应该派生相同密钥")
        void shouldDeriveSameKeyForSamePasswordAndSalt() {
            // Given
            String masterPassword = "MasterPassword456!";
            String salt = cryptoService.generateSalt();

            // When
            byte[] key1 = cryptoService.deriveKey(masterPassword, salt);
            byte[] key2 = cryptoService.deriveKey(masterPassword, salt);

            // Then
            assertArrayEquals(key1, key2);
        }

        @Test
        @DisplayName("不同盐值应该派生不同密钥")
        void shouldDeriveDifferentKeyForDifferentSalt() {
            // Given
            String masterPassword = "MasterPassword456!";
            String salt1 = cryptoService.generateSalt();
            String salt2 = cryptoService.generateSalt();

            // When
            byte[] key1 = cryptoService.deriveKey(masterPassword, salt1);
            byte[] key2 = cryptoService.deriveKey(masterPassword, salt2);

            // Then
            assertFalse(java.util.Arrays.equals(key1, key2));
        }

        @Test
        @DisplayName("应该抛出异常当主密码为空")
        void shouldThrowExceptionWhenMasterPasswordIsEmpty() {
            // Given
            String salt = cryptoService.generateSalt();

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.deriveKey("", salt);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.deriveKey(null, salt);
            });
        }

        @Test
        @DisplayName("应该抛出异常当盐值为空")
        void shouldThrowExceptionWhenSaltIsEmpty() {
            // Given
            String masterPassword = "MasterPassword456!";

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.deriveKey(masterPassword, "");
            });

            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.deriveKey(masterPassword, null);
            });
        }
    }

    @Nested
    @DisplayName("随机数生成测试")
    class RandomGenerationTests {

        @Test
        @DisplayName("应该生成正确长度的盐值")
        void shouldGenerateSaltWithCorrectLength() {
            // When
            String salt = cryptoService.generateSalt();

            // Then
            assertNotNull(salt);
            assertFalse(salt.isEmpty());
            // Base64编码的16字节应该是24个字符
            assertEquals(24, salt.length());
        }

        @Test
        @DisplayName("应该生成唯一盐值")
        void shouldGenerateUniqueSalts() {
            // When
            String salt1 = cryptoService.generateSalt();
            String salt2 = cryptoService.generateSalt();

            // Then
            assertNotEquals(salt1, salt2);
        }

        @Test
        @DisplayName("应该生成正确长度的IV")
        void shouldGenerateIVWithCorrectLength() {
            // When
            byte[] iv = cryptoService.generateIV();

            // Then
            assertNotNull(iv);
            assertEquals(12, iv.length); // GCM标准IV长度
        }

        @Test
        @DisplayName("应该生成唯一IV")
        void shouldGenerateUniqueIVs() {
            // When
            byte[] iv1 = cryptoService.generateIV();
            byte[] iv2 = cryptoService.generateIV();

            // Then
            assertFalse(java.util.Arrays.equals(iv1, iv2));
        }

        @Test
        @DisplayName("应该生成指定长度的随机字节")
        void shouldGenerateRandomBytesWithCorrectLength() {
            // Given
            int length = 32;

            // When
            byte[] bytes = cryptoService.generateRandomBytes(length);

            // Then
            assertNotNull(bytes);
            assertEquals(length, bytes.length);
        }

        @Test
        @DisplayName("应该抛出异常当长度无效")
        void shouldThrowExceptionWhenLengthIsInvalid() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.generateRandomBytes(0);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                cryptoService.generateRandomBytes(-1);
            });
        }
    }

    @Nested
    @DisplayName("辅助方法测试")
    class UtilityMethodTests {

        @Test
        @DisplayName("应该正确转换字节数组到十六进制字符串")
        void shouldConvertBytesToHex() {
            // Given
            byte[] bytes = {0x01, 0x02, 0x0A, 0x0F, 0x10, 0x1F, (byte) 0xFF};

            // When
            String hex = CryptoService.bytesToHex(bytes);

            // Then
            assertEquals("01020a0f101fff", hex);
        }

        @Test
        @DisplayName("应该正确转换十六进制字符串到字节数组")
        void shouldConvertHexToBytes() {
            // Given
            String hex = "01020a0f101fff";

            // When
            byte[] bytes = CryptoService.hexToBytes(hex);

            // Then
            assertNotNull(bytes);
            assertEquals(7, bytes.length);
            assertEquals(0x01, bytes[0]);
            assertEquals(0x02, bytes[1]);
            assertEquals(0x0A, bytes[2]);
            assertEquals(0x0F, bytes[3]);
            assertEquals(0x10, bytes[4]);
            assertEquals(0x1F, bytes[5]);
            // 0xFF 在byte中表示为-1（有符号byte）
            assertEquals((byte) 0xFF, bytes[6]);
        }

        @Test
        @DisplayName("应该正确清除字节数组敏感数据")
        void shouldClearByteArraySensitiveData() {
            // Given
            byte[] data = {1, 2, 3, 4, 5};

            // When
            CryptoService.clearSensitiveData(data);

            // Then
            for (byte b : data) {
                assertEquals(0, b);
            }
        }

        @Test
        @DisplayName("应该正确清除字符数组敏感数据")
        void shouldClearCharArraySensitiveData() {
            // Given
            char[] data = {'a', 'b', 'c', 'd', 'e'};

            // When
            CryptoService.clearSensitiveData(data);

            // Then
            for (char c : data) {
                assertEquals('\0', c);
            }
        }

        @Test
        @DisplayName("应该处理null值当清除敏感数据")
        void shouldHandleNullWhenClearingSensitiveData() {
            // When & Then
            assertDoesNotThrow(() -> {
                CryptoService.clearSensitiveData((byte[]) null);
                CryptoService.clearSensitiveData((char[]) null);
            });
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整的加密解密流程")
        void shouldPerformCompleteEncryptionDecryptionFlow() {
            // Given
            String masterPassword = "MyMasterPassword123!";
            String salt = cryptoService.generateSalt();
            String plainPassword = "WebsitePassword456!";

            // When - 派生密钥
            byte[] key = cryptoService.deriveKey(masterPassword, salt);

            // When - 加密
            String encrypted = cryptoService.encryptPassword(plainPassword, key);

            // When - 解密
            String decrypted = cryptoService.decryptPassword(encrypted, key);

            // Then
            assertEquals(plainPassword, decrypted);

            // 清理
            CryptoService.clearSensitiveData(key);
        }

        @Test
        @DisplayName("完整的用户认证流程")
        void shouldPerformCompleteUserAuthenticationFlow() {
            // Given
            String masterPassword = "MyMasterPassword123!";
            String salt = cryptoService.generateSalt();

            // When - 注册（哈希密码）
            String hash = cryptoService.hashMasterPassword(masterPassword, salt);

            // When - 登录（验证密码）
            boolean isValid = cryptoService.verifyMasterPassword(masterPassword, hash, salt);

            // Then
            assertTrue(isValid);

            // When - 使用错误密码登录
            boolean isInvalid = cryptoService.verifyMasterPassword("WrongPassword", hash, salt);

            // Then
            assertFalse(isInvalid);
        }
    }
}