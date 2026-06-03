package org.florious.passwordmanager.crypto;

import org.florious.passwordmanager.model.PasswordPolicy;
import org.florious.passwordmanager.model.PasswordStrength;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordGenerator单元测试
 */
class PasswordGeneratorTest {
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void testGenerateDefault() {
        String password = passwordGenerator.generateDefault();
        assertNotNull(password);
        assertEquals(16, password.length());
        
        // 检查是否包含所有字符类型
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSymbol = true;
        }
        
        assertTrue(hasUppercase, "默认密码应包含大写字母");
        assertTrue(hasLowercase, "默认密码应包含小写字母");
        assertTrue(hasDigit, "默认密码应包含数字");
        assertTrue(hasSymbol, "默认密码应包含符号");
    }

    @Test
    void testGenerateWithLength() {
        String password = passwordGenerator.generate(20);
        assertNotNull(password);
        assertEquals(20, password.length());
    }

    @Test
    void testGenerateWithPolicy() {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setLength(12);
        policy.setIncludeUppercase(true);
        policy.setIncludeLowercase(true);
        policy.setIncludeDigits(false);
        policy.setIncludeSymbols(false);
        
        String password = passwordGenerator.generate(policy);
        assertNotNull(password);
        assertEquals(12, password.length());
        
        // 检查是否只包含大写和小写字母
        for (char c : password.toCharArray()) {
            assertTrue(Character.isLetter(c), "密码应只包含字母");
        }
    }

    @Test
    void testGenerateOnlyDigits() {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setLength(8);
        policy.setIncludeUppercase(false);
        policy.setIncludeLowercase(false);
        policy.setIncludeDigits(true);
        policy.setIncludeSymbols(false);
        
        String password = passwordGenerator.generate(policy);
        assertNotNull(password);
        assertEquals(8, password.length());
        
        // 检查是否只包含数字
        for (char c : password.toCharArray()) {
            assertTrue(Character.isDigit(c), "密码应只包含数字");
        }
    }

    @Test
    void testGenerateWithInvalidPolicy() {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setIncludeUppercase(false);
        policy.setIncludeLowercase(false);
        policy.setIncludeDigits(false);
        policy.setIncludeSymbols(false);
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordGenerator.generate(policy);
        });
    }

    @Test
    void testGenerateWithNullPolicy() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordGenerator.generate(null);
        });
    }

    @Test
    void testGenerateWithLengthLessThanSelectedTypes() {
        // 长度=2，但选了4种类型，应抛出异常
        PasswordPolicy policy = new PasswordPolicy();
        policy.setLength(2);
        policy.setIncludeUppercase(true);
        policy.setIncludeLowercase(true);
        policy.setIncludeDigits(true);
        policy.setIncludeSymbols(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            passwordGenerator.generate(policy);
        });
        assertTrue(exception.getMessage().contains("不能小于已选字符类型数"));
    }

    @Test
    void testGenerateWithLengthEqualToSelectedTypes() {
        // 长度=3，选了3种类型，应正常生成
        PasswordPolicy policy = new PasswordPolicy();
        policy.setLength(3);
        policy.setIncludeUppercase(true);
        policy.setIncludeLowercase(true);
        policy.setIncludeDigits(true);
        policy.setIncludeSymbols(false);

        String password = passwordGenerator.generate(policy);
        assertNotNull(password);
        assertEquals(3, password.length());
    }

    @Test
    void testCheckStrengthWeak() {
        assertEquals(PasswordStrength.WEAK, passwordGenerator.checkStrength("123"));
        assertEquals(PasswordStrength.WEAK, passwordGenerator.checkStrength("abc"));
        assertEquals(PasswordStrength.WEAK, passwordGenerator.checkStrength("1234567"));
    }

    @Test
    void testCheckStrengthMedium() {
        // 规范：8-11个字符，包含两种字符类型 → 中
        assertEquals(PasswordStrength.MEDIUM, passwordGenerator.checkStrength("Abcdefgh")); // 8位，大小写
        assertEquals(PasswordStrength.MEDIUM, passwordGenerator.checkStrength("abcd1234")); // 8位，小写+数字
    }

    @Test
    void testCheckStrengthStrong() {
        assertEquals(PasswordStrength.STRONG, passwordGenerator.checkStrength("Abcdefghij12")); // 12位，包含大小写和数字
    }

    @Test
    void testCheckStrengthVeryStrong() {
        // 16位，包含所有字符类型（大小写、数字、符号），无重复和连续字符
        String password = "A1b2C3d4E5f6G7h!";
        assertEquals(PasswordStrength.VERY_STRONG, passwordGenerator.checkStrength(password));
    }

    @Test
    void testCheckStrengthWithNull() {
        assertEquals(PasswordStrength.WEAK, passwordGenerator.checkStrength(null));
    }

    @Test
    void testCheckStrengthWithEmpty() {
        assertEquals(PasswordStrength.WEAK, passwordGenerator.checkStrength(""));
    }

    @Test
    void testCheckStrengthWithRepeatingCharacters() {
        // 包含重复字符
        String password = "Aaaa1111bbbb2222";
        PasswordStrength strength = passwordGenerator.checkStrength(password);
        // 即使16位且包含所有类型，因为有重复字符，不应是VERY_STRONG
        assertNotEquals(PasswordStrength.VERY_STRONG, strength);
    }

    @Test
    void testCheckStrengthWithSequentialCharacters() {
        // 包含连续字符
        String password = "Abcdefghij123456";
        PasswordStrength strength = passwordGenerator.checkStrength(password);
        // 即使16位且包含所有类型，因为有连续字符，不应是VERY_STRONG
        assertNotEquals(PasswordStrength.VERY_STRONG, strength);
    }

    @Test
    void testGenerateMultiplePasswordsAreDifferent() {
        String password1 = passwordGenerator.generateDefault();
        String password2 = passwordGenerator.generateDefault();
        
        // 两次生成的密码应该不同（概率极低相同）
        assertNotEquals(password1, password2);
    }
}