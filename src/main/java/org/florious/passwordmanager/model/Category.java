package org.florious.passwordmanager.model;

/**
 * 分类模型类
 * 存储密码条目的分类信息
 */
public class Category {
    private int id;
    private int userId;
    private String name;
    private String color;

    /**
     * 默认构造函数
     */
    public Category() {
    }

    /**
     * 创建新分类的构造函数
     * @param userId 用户ID
     * @param name 分类名称
     */
    public Category(int userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    /**
     * 创建带颜色的分类构造函数
     * @param userId 用户ID
     * @param name 分类名称
     * @param color 分类颜色
     */
    public Category(int userId, String name, String color) {
        this.userId = userId;
        this.name = name;
        this.color = color;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id && userId == category.userId && 
               name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, userId, name);
    }
}