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
     * 检查密码强度（评分制）
     * <p>
     * 评分规则：
     * - 长度分：8以下=0, 8-11=1, 12-15=2, 16+=3
     * - 类型分：每种字符类型+1（最多4分）
     * - 扣分：有重复字符-1，有连续字符-1
     * <p>
     * 强度映射：
     * - 单一类型或总分 0-1: WEAK
     * - 总分 2-3: MEDIUM
     * - 总分 4-5: STRONG
     * - 总分 6+: VERY_STRONG
     */
    public PasswordStrength checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }

        int score = 0;
        int length = password.length();

        // 长度分
        if (length < 8) {
            score += 0;
        } else if (length <= 11) {
            score += 1;
        } else if (length <= 15) {
            score += 2;
        } else {
            score += 3;
        }

        // 字符类型分
        boolean hasUppercase = false, hasLowercase = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSymbol = true;
        }
        int typeCount = 0;
        if (hasUppercase) { score++; typeCount++; }
        if (hasLowercase) { score++; typeCount++; }
        if (hasDigit) { score++; typeCount++; }
        if (hasSymbol) { score++; typeCount++; }

        // 单一类型直接判定为WEAK
        if (typeCount <= 1) {
            return PasswordStrength.WEAK;
        }

        // 扣分：连续重复字符
        for (int i = 0; i < length - 1; i++) {
            if (password.charAt(i) == password.charAt(i + 1)) {
                score--;
                break;
            }
        }

        // 扣分：连续序列（abc, 123）
        for (int i = 0; i < length - 2; i++) {
            char c1 = password.charAt(i), c2 = password.charAt(i + 1), c3 = password.charAt(i + 2);
            if ((c1 + 1 == c2 && c2 + 1 == c3) || (c1 - 1 == c2 && c2 - 1 == c3)) {
                score--;
                break;
            }
        }

        // 映射到强度等级
        if (score <= 1) return PasswordStrength.WEAK;
        if (score <= 3) return PasswordStrength.MEDIUM;
        if (score <= 5) return PasswordStrength.STRONG;
        return PasswordStrength.VERY_STRONG;
    }
}