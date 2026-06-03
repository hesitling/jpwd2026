package org.florious.passwordmanager.model;

/**
 * 密码生成策略配置
 */
public class PasswordPolicy {
    private int length = 16;
    private boolean includeUppercase = true;
    private boolean includeLowercase = true;
    private boolean includeDigits = true;
    private boolean includeSymbols = true;

    public PasswordPolicy() {
    }

    public PasswordPolicy(int length, boolean includeUppercase, boolean includeLowercase, 
                         boolean includeDigits, boolean includeSymbols) {
        this.length = length;
        this.includeUppercase = includeUppercase;
        this.includeLowercase = includeLowercase;
        this.includeDigits = includeDigits;
        this.includeSymbols = includeSymbols;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("密码长度必须大于0");
        }
        this.length = length;
    }

    public boolean isIncludeUppercase() {
        return includeUppercase;
    }

    public void setIncludeUppercase(boolean includeUppercase) {
        this.includeUppercase = includeUppercase;
    }

    public boolean isIncludeLowercase() {
        return includeLowercase;
    }

    public void setIncludeLowercase(boolean includeLowercase) {
        this.includeLowercase = includeLowercase;
    }

    public boolean isIncludeDigits() {
        return includeDigits;
    }

    public void setIncludeDigits(boolean includeDigits) {
        this.includeDigits = includeDigits;
    }

    public boolean isIncludeSymbols() {
        return includeSymbols;
    }

    public void setIncludeSymbols(boolean includeSymbols) {
        this.includeSymbols = includeSymbols;
    }

    /**
     * 检查策略是否有效（至少选择一种字符类型）
     */
    public boolean isValid() {
        return includeUppercase || includeLowercase || includeDigits || includeSymbols;
    }

    /**
     * 获取所有选中的字符类型
     */
    public String getSelectedCharTypes() {
        StringBuilder sb = new StringBuilder();
        if (includeUppercase) sb.append("大写字母 ");
        if (includeLowercase) sb.append("小写字母 ");
        if (includeDigits) sb.append("数字 ");
        if (includeSymbols) sb.append("符号 ");
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return String.format("PasswordPolicy{length=%d, uppercase=%b, lowercase=%b, digits=%b, symbols=%b}",
                length, includeUppercase, includeLowercase, includeDigits, includeSymbols);
    }
}