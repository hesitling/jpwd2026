package org.florious.passwordmanager.util;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.service.CategoryService;
import org.florious.passwordmanager.service.SessionManager;
import org.florious.passwordmanager.service.VaultService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV导入导出处理器
 * 负责密码条目的CSV格式导入导出
 */
public class CsvHandler {
    private final VaultService vaultService;
    private final CategoryService categoryService;
    private final SessionManager sessionManager;

    public CsvHandler() {
        this(false);
    }

    /**
     * 创建 CsvHandler
     * @param testMode 测试模式
     */
    public CsvHandler(boolean testMode) {
        this.vaultService = new VaultService(testMode);
        this.categoryService = new CategoryService(testMode);
        this.sessionManager = SessionManager.getInstance(testMode);
    }

    /**
     * 导出所有密码条目到CSV文件
     * @param filePath 文件路径
     * @return 导出的条目数量
     * @throws CsvException 如果导出失败
     */
    public int exportToCsv(String filePath) throws CsvException {
        // 验证用户已登录
        validateSession();

        try {
            // 获取所有密码条目
            List<PasswordEntry> entries = vaultService.getAllPasswords();
            
            // 解密所有密码
            List<DecryptedEntry> decryptedEntries = new ArrayList<>();
            for (PasswordEntry entry : entries) {
                VaultService.DecryptedPasswordEntry decrypted = vaultService.getPassword(entry.getId());
                decryptedEntries.add(new DecryptedEntry(decrypted, categoryService));
            }

            // 写入CSV文件
            writeCsvFile(filePath, decryptedEntries);
            
            return decryptedEntries.size();
        } catch (Exception e) {
            throw new CsvException("导出CSV失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从CSV文件导入密码条目
     * @param filePath 文件路径
     * @param duplicateHandling 重复条目处理方式
     * @return 导入结果
     * @throws CsvException 如果导入失败
     */
    public ImportResult importFromCsv(String filePath, DuplicateHandling duplicateHandling) throws CsvException {
        // 验证用户已登录
        validateSession();

        try {
            // 读取CSV文件
            List<CsvRecord> records = readCsvFile(filePath);
            
            // 验证CSV格式
            validateCsvRecords(records);
            
            // 处理导入
            int importedCount = 0;
            int skippedCount = 0;
            int overwrittenCount = 0;
            int renamedCount = 0;
            
            for (CsvRecord record : records) {
                // 检查是否已存在相同标题的条目
                PasswordEntry existingEntry = findExistingEntry(record.title());
                
                if (existingEntry != null) {
                    switch (duplicateHandling) {
                        case SKIP:
                            skippedCount++;
                            continue;
                        case OVERWRITE:
                            updateExistingEntry(existingEntry, record);
                            overwrittenCount++;
                            break;
                        case RENAME:
                            record = record.withTitle(generateUniqueTitle(record.title()));
                            importNewEntry(record);
                            renamedCount++;
                            break;
                    }
                } else {
                    importNewEntry(record);
                }
                importedCount++;
            }
            
            return new ImportResult(importedCount, skippedCount, overwrittenCount, renamedCount);
        } catch (CsvException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvException("导入CSV失败: " + e.getMessage(), e);
        }
    }

    /**
     * 写入CSV文件
     * @param filePath 文件路径
     * @param entries 解密后的条目列表
     * @throws IOException 如果写入失败
     */
    private void writeCsvFile(String filePath, List<DecryptedEntry> entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            
            // 写入BOM（UTF-8）
            writer.write('\uFEFF');
            
            // 写入标题行
            writer.write("title,username,password,url,notes,category");
            writer.newLine();
            
            // 写入数据行
            for (DecryptedEntry entry : entries) {
                List<String> fields = List.of(
                    escapeCsvField(entry.title()),
                    escapeCsvField(entry.username()),
                    escapeCsvField(entry.password()),
                    escapeCsvField(entry.url()),
                    escapeCsvField(entry.notes()),
                    escapeCsvField(entry.categoryName())
                );
                writer.write(String.join(",", fields));
                writer.newLine();
            }
        }
    }

    /**
     * 读取CSV文件
     * @param filePath 文件路径
     * @return CSV记录列表
     * @throws CsvException 如果读取失败
     */
    private List<CsvRecord> readCsvFile(String filePath) throws CsvException {
        List<CsvRecord> records = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            // 读取标题行
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new CsvException("CSV文件为空");
            }
            
            // 解析标题行
            String[] headers = parseCsvLine(headerLine);
            validateHeaders(headers);
            
            // 读取数据行
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] values = parseCsvLine(line);
                    if (values.length < headers.length) {
                        // 补充缺失的列
                        String[] paddedValues = new String[headers.length];
                        System.arraycopy(values, 0, paddedValues, 0, values.length);
                        for (int i = values.length; i < headers.length; i++) {
                            paddedValues[i] = "";
                        }
                        values = paddedValues;
                    }
                    
                    CsvRecord record = new CsvRecord(
                        values[0], // title
                        values[1], // username
                        values[2], // password
                        values[3], // url
                        values[4], // notes
                        values[5]  // category
                    );
                    records.add(record);
                } catch (Exception e) {
                    throw new CsvException("第" + lineNumber + "行格式错误: " + e.getMessage(), e);
                }
            }
        } catch (CsvException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvException("读取CSV文件失败: " + e.getMessage(), e);
        }
        
        return records;
    }

    /**
     * 验证CSV标题
     * @param headers 标题数组
     * @throws CsvException 如果验证失败
     */
    private void validateHeaders(String[] headers) throws CsvException {
        String[] requiredHeaders = {"title", "username", "password", "url", "notes", "category"};
        
        if (headers.length < requiredHeaders.length) {
            throw new CsvException("CSV文件缺少必需的列");
        }
        
        for (int i = 0; i < requiredHeaders.length; i++) {
            if (!requiredHeaders[i].equalsIgnoreCase(headers[i].trim())) {
                throw new CsvException("CSV文件第" + (i + 1) + "列应为'" + requiredHeaders[i] + "'，但找到了'" + headers[i] + "'");
            }
        }
    }

    /**
     * 验证CSV记录
     * @param records 记录列表
     * @throws CsvException 如果验证失败
     */
    private void validateCsvRecords(List<CsvRecord> records) throws CsvException {
        if (records.isEmpty()) {
            throw new CsvException("CSV文件没有数据行");
        }
        
        for (int i = 0; i < records.size(); i++) {
            CsvRecord record = records.get(i);
            if (record.title() == null || record.title().trim().isEmpty()) {
                throw new CsvException("第" + (i + 2) + "行标题不能为空");
            }
            if (record.password() == null || record.password().trim().isEmpty()) {
                throw new CsvException("第" + (i + 2) + "行密码不能为空");
            }
        }
    }

    /**
     * 解析CSV行
     * @param line CSV行
     * @return 字段数组
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (inQuotes) {
                if (c == '"') {
                    // 检查是否是转义的双引号
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++; // 跳过下一个双引号
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(currentField.toString());
                    currentField.setLength(0);
                } else {
                    currentField.append(c);
                }
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * 转义CSV字段
     * @param field 字段值
     * @return 转义后的字段
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        // 如果字段包含逗号、双引号或换行符，需要用双引号包围
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            // 转义双引号
            String escaped = field.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return field;
    }

    /**
     * 查找已存在的条目
     * @param title 标题
     * @return 已存在的条目，如果不存在返回null
     * @throws Exception 如果查询失败
     */
    private PasswordEntry findExistingEntry(String title) throws Exception {
        List<PasswordEntry> entries = vaultService.searchPasswords(title);
        for (PasswordEntry entry : entries) {
            if (entry.getTitle().equals(title)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * 更新已存在的条目
     * @param existingEntry 已存在的条目
     * @param record CSV记录
     * @throws Exception 如果更新失败
     */
    private void updateExistingEntry(PasswordEntry existingEntry, CsvRecord record) throws Exception {
        // 查找分类ID
        Integer categoryId = findCategoryIdByName(record.category());
        
        vaultService.updatePassword(
            existingEntry.getId(),
            record.title(),
            record.username(),
            record.password(), // 明文密码，会被加密
            record.url(),
            record.notes(),
            categoryId
        );
    }

    /**
     * 导入新条目
     * @param record CSV记录
     * @throws Exception 如果导入失败
     */
    private void importNewEntry(CsvRecord record) throws Exception {
        // 查找分类ID
        Integer categoryId = findCategoryIdByName(record.category());
        
        vaultService.addPassword(
            record.title(),
            record.username(),
            record.password(), // 明文密码，会被加密
            record.url(),
            record.notes(),
            categoryId
        );
    }

    /**
     * 根据分类名称查找分类ID
     * @param categoryName 分类名称
     * @return 分类ID，如果不存在返回null
     * @throws Exception 如果查询失败
     */
    private Integer findCategoryIdByName(String categoryName) throws Exception {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return null;
        }
        
        List<Category> categories = categoryService.getAllCategories();
        for (Category category : categories) {
            if (category.getName().equals(categoryName)) {
                return category.getId();
            }
        }
        return null;
    }

    /**
     * 生成唯一的标题
     * @param originalTitle 原始标题
     * @return 唯一的标题
     * @throws Exception 如果查询失败
     */
    private String generateUniqueTitle(String originalTitle) throws Exception {
        String baseTitle = originalTitle;
        int counter = 1;
        
        while (true) {
            String candidateTitle = baseTitle + " (" + counter + ")";
            PasswordEntry existing = findExistingEntry(candidateTitle);
            if (existing == null) {
                return candidateTitle;
            }
            counter++;
        }
    }

    /**
     * 验证会话
     * @throws CsvException 如果未登录
     */
    private void validateSession() throws CsvException {
        if (!sessionManager.hasActiveSession()) {
            throw new CsvException("用户未登录");
        }
    }

    /**
     * 解密后的条目内部类
     */
    private static class DecryptedEntry {
        private final String title;
        private final String username;
        private final String password;
        private final String url;
        private final String notes;
        private final String categoryName;

        public DecryptedEntry(VaultService.DecryptedPasswordEntry decrypted, CategoryService categoryService) throws Exception {
            this.title = decrypted.getTitle();
            this.username = decrypted.getUsername();
            this.password = decrypted.getDecryptedPassword();
            this.url = decrypted.getUrl();
            this.notes = decrypted.getNotes();
            
            // 获取分类名称
            Integer categoryId = decrypted.getCategoryId();
            if (categoryId != null) {
                try {
                    Category category = categoryService.getCategory(categoryId);
                    this.categoryName = category.getName();
                } catch (Exception e) {
                    this.categoryName = "";
                }
            } else {
                this.categoryName = "";
            }
        }

        public String title() { return title; }
        public String username() { return username; }
        public String password() { return password; }
        public String url() { return url; }
        public String notes() { return notes; }
        public String categoryName() { return categoryName; }
    }

    /**
     * CSV记录内部类
     */
    private static class CsvRecord {
        private final String title;
        private final String username;
        private final String password;
        private final String url;
        private final String notes;
        private final String category;

        public CsvRecord(String title, String username, String password, String url, String notes, String category) {
            this.title = title;
            this.username = username;
            this.password = password;
            this.url = url;
            this.notes = notes;
            this.category = category;
        }

        public String title() { return title; }
        public String username() { return username; }
        public String password() { return password; }
        public String url() { return url; }
        public String notes() { return notes; }
        public String category() { return category; }

        public CsvRecord withTitle(String newTitle) {
            return new CsvRecord(newTitle, username, password, url, notes, category);
        }
    }

    /**
     * 导入结果内部类
     */
    public static class ImportResult {
        private final int importedCount;
        private final int skippedCount;
        private final int overwrittenCount;
        private final int renamedCount;

        public ImportResult(int importedCount, int skippedCount, int overwrittenCount, int renamedCount) {
            this.importedCount = importedCount;
            this.skippedCount = skippedCount;
            this.overwrittenCount = overwrittenCount;
            this.renamedCount = renamedCount;
        }

        public int getImportedCount() { return importedCount; }
        public int getSkippedCount() { return skippedCount; }
        public int getOverwrittenCount() { return overwrittenCount; }
        public int getRenamedCount() { return renamedCount; }
        
        public int getTotalProcessed() {
            return importedCount + skippedCount + overwrittenCount + renamedCount;
        }
    }

    /**
     * 重复条目处理方式枚举
     */
    public enum DuplicateHandling {
        SKIP,
        OVERWRITE,
        RENAME
    }

    /**
     * CSV异常类
     */
    public static class CsvException extends Exception {
        public CsvException(String message) {
            super(message);
        }

        public CsvException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}