package org.florious.passwordmanager.crypto;

import org.florious.passwordmanager.model.PasswordPolicy;
import org.florious.passwordmanager.model.PasswordStrength;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 密码生成器
 */
public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    private final SecureRandom secureRandom;

    public PasswordGenerator() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * 根据策略生成密码
     */
    public String generate(PasswordPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("密码策略不能为null");
        }
        
        if (!policy.isValid()) {
            throw new IllegalArgumentException("至少需要选择一种字符类型");
        }

        int selectedTypeCount = 0;
        if (policy.isIncludeUppercase()) selectedTypeCount++;
        if (policy.isIncludeLowercase()) selectedTypeCount++;
        if (policy.isIncludeDigits()) selectedTypeCount++;
        if (policy.isIncludeSymbols()) selectedTypeCount++;

        if (policy.getLength() < selectedTypeCount) {
            throw new IllegalArgumentException(
                    String.format("密码长度(%d)不能小于已选字符类型数(%d)", policy.getLength(), selectedTypeCount));
        }

        // 构建字符池
        StringBuilder charPool = new StringBuilder();
        if (policy.isIncludeUppercase()) {
            charPool.append(UPPERCASE);
        }
        if (policy.isIncludeLowercase()) {
            charPool.append(LOWERCASE);
        }
        if (policy.isIncludeDigits()) {
            charPool.append(DIGITS);
        }
        if (policy.isIncludeSymbols()) {
            charPool.append(SYMBOLS);
        }

        // 生成密码
        List<Character> passwordChars = new ArrayList<>();
        
        // 确保每种选中的字符类型至少出现一次
        if (policy.isIncludeUppercase()) {
            passwordChars.add(randomChar(UPPERCASE));
        }
        if (policy.isIncludeLowercase()) {
            passwordChars.add(randomChar(LOWERCASE));
        }
        if (policy.isIncludeDigits()) {
            passwordChars.add(randomChar(DIGITS));
        }
        if (policy.isIncludeSymbols()) {
            passwordChars.add(randomChar(SYMBOLS));
        }

        // 填充剩余长度
        int remaining = policy.getLength() - passwordChars.size();
        for (int i = 0; i < remaining; i++) {
            passwordChars.add(randomChar(charPool.toString()));
        }

        // 随机打乱顺序
        Collections.shuffle(passwordChars, secureRandom);

        // 转换为字符串
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    /**
     * 生成默认密码（16位，包含所有字符类型）
     */
    public String generateDefault() {
        return generate(new PasswordPolicy());
    }

    /**
     * 生成指定长度的密码
     */
    public String generate(int length) {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setLength(length);
        return generate(policy);
    }

    /**
     * 从字符串中随机选择一个字符
     */
    private char randomChar(String chars) {
        return chars.charAt(secureRandom.nextInt(chars.length()));
    }

    /**
     * 检查密码强度
     */
    public PasswordStrength checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }

        int length = password.length();
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        
        // 统计字符类型
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSymbol = true;
            }
        }

        int typeCount = 0;
        if (hasUppercase) typeCount++;
        if (hasLowercase) typeCount++;
        if (hasDigit) typeCount++;
        if (hasSymbol) typeCount++;

        // 检查重复字符
        boolean hasRepeating = false;
        for (int i = 0; i < password.length() - 1; i++) {
            if (password.charAt(i) == password.charAt(i + 1)) {
                hasRepeating = true;
                break;
            }
        }

        // 检查连续字符（如abc, 123）
        boolean hasSequential = false;
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            if ((c1 + 1 == c2 && c2 + 1 == c3) || (c1 - 1 == c2 && c2 - 1 == c3)) {
                hasSequential = true;
                break;
            }
        }

        // 根据规则判断强度
        if (length < 8) {
            return PasswordStrength.WEAK;
        } else if (length >= 8 && length <= 11 && typeCount >= 2) {
            return PasswordStrength.MEDIUM;
        } else if (length >= 12 && length <= 15 && typeCount >= 3) {
            return PasswordStrength.STRONG;
        } else if (length >= 16 && typeCount == 4 && !hasRepeating && !hasSequential) {
            return PasswordStrength.VERY_STRONG;
        } else if (length >= 12 && typeCount >= 2) {
            return PasswordStrength.MEDIUM;
        } else if (length >= 8) {
            return PasswordStrength.WEAK;
        }
        
        return PasswordStrength.WEAK;
    }
}