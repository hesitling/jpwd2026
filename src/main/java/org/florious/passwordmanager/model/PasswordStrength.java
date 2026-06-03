package org.florious.passwordmanager.model;

/**
 * 密码强度枚举
 */
public enum PasswordStrength {
    WEAK("弱", 0, "#FF0000"),      // 红色
    MEDIUM("中", 1, "#FFFF00"),    // 黄色
    STRONG("强", 2, "#0000FF"),    // 蓝色
    VERY_STRONG("非常强", 3, "#00FF00"); // 绿色

    private final String displayName;
    private final int level;
    private final String colorCode;

    PasswordStrength(String displayName, int level, String colorCode) {
        this.displayName = displayName;
        this.level = level;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getColorCode() {
        return colorCode;
    }

    /**
     * 根据等级获取强度
     */
    public static PasswordStrength fromLevel(int level) {
        for (PasswordStrength strength : values()) {
            if (strength.level == level) {
                return strength;
            }
        }
        return WEAK;
    }

    @Override
    public String toString() {
        return displayName;
    }
}