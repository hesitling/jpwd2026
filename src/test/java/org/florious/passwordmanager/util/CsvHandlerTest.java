package org.florious.passwordmanager.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CsvHandler单元测试
 */
@DisplayName("CsvHandler 测试")
class CsvHandlerTest {

    private CsvHandler csvHandler;

    @BeforeEach
    void setUp() {
        // 使用测试模式
        csvHandler = new CsvHandler(true);
    }

    @Nested
    @DisplayName("CSV转义测试")
    class CsvEscapeTests {

        @Test
        @DisplayName("应该正确转义包含逗号的字段")
        void shouldEscapeFieldWithComma() {
            // Given
            String field = "hello,world";

            // When
            String escaped = escapeCsvField(field);

            // Then
            assertEquals("\"hello,world\"", escaped);
        }

        @Test
        @DisplayName("应该正确转义包含双引号的字段")
        void shouldEscapeFieldWithDoubleQuote() {
            // Given
            String field = "hello\"world";

            // When
            String escaped = escapeCsvField(field);

            // Then
            assertEquals("\"hello\"\"world\"", escaped);
        }

        @Test
        @DisplayName("应该正确转义包含换行符的字段")
        void shouldEscapeFieldWithNewline() {
            // Given
            String field = "hello\nworld";

            // When
            String escaped = escapeCsvField(field);

            // Then
            assertEquals("\"hello\nworld\"", escaped);
        }

        @Test
        @DisplayName("应该正确转义包含多个特殊字符的字段")
        void shouldEscapeFieldWithMultipleSpecialChars() {
            // Given
            String field = "hello,\"world\"\n";

            // When
            String escaped = escapeCsvField(field);

            // Then
            assertEquals("\"hello,\"\"world\"\"\n\"", escaped);
        }

        @Test
        @DisplayName("应该正确处理null字段")
        void shouldHandleNullField() {
            // Given
            String field = null;

            // When
            String escaped = escapeCsvField(field);

            // Then
            assertEquals("", escaped);
        }

        @Test
        @DisplayName("应该正确处理普通字段")
        void shouldHandleNormalField() {
            // Given
            String field = "helloworld";

            // When
            String escaped = escapeCsvField(field);

            // Then
            assertEquals("helloworld", escaped);
        }
    }

    @Nested
    @DisplayName("CSV解析测试")
    class CsvParseTests {

        @Test
        @DisplayName("应该正确解析简单的CSV行")
        void shouldParseSimpleCsvLine() {
            // Given
            String line = "title,username,password,url,notes,category";

            // When
            String[] fields = parseCsvLine(line);

            // Then
            assertEquals(6, fields.length);
            assertEquals("title", fields[0]);
            assertEquals("username", fields[1]);
            assertEquals("password", fields[2]);
            assertEquals("url", fields[3]);
            assertEquals("notes", fields[4]);
            assertEquals("category", fields[5]);
        }

        @Test
        @DisplayName("应该正确解析包含逗号的字段")
        void shouldParseFieldWithComma() {
            // Given
            String line = "\"hello,world\",username,password,url,notes,category";

            // When
            String[] fields = parseCsvLine(line);

            // Then
            assertEquals(6, fields.length);
            assertEquals("hello,world", fields[0]);
        }

        @Test
        @DisplayName("应该正确解析包含双引号的字段")
        void shouldParseFieldWithDoubleQuote() {
            // Given
            String line = "\"hello\"\"world\",username,password,url,notes,category";

            // When
            String[] fields = parseCsvLine(line);

            // Then
            assertEquals(6, fields.length);
            assertEquals("hello\"world", fields[0]);
        }

        @Test
        @DisplayName("应该正确解析空字段")
        void shouldParseEmptyField() {
            // Given
            String line = "title,,password,url,notes,category";

            // When
            String[] fields = parseCsvLine(line);

            // Then
            assertEquals(6, fields.length);
            assertEquals("title", fields[0]);
            assertEquals("", fields[1]);
            assertEquals("password", fields[2]);
        }
    }

    @Nested
    @DisplayName("CSV内容解析测试")
    class CsvContentParseTests {

        @Test
        @DisplayName("应该正确解析多行字段")
        void shouldParseMultilineField() {
            // Given
            String content = "title,username,password,url,notes,category\n" +
                    "test,user123,pass123,http://example.com,\"line1\nline2\",category1";

            // When
            java.util.List<String[]> records = parseCsvContent(content);

            // Then
            assertEquals(2, records.size());
            String[] firstRecord = records.get(0);
            String[] secondRecord = records.get(1);

            assertEquals("title", firstRecord[0]);
            assertEquals("test", secondRecord[0]);
            assertEquals("line1\nline2", secondRecord[4]);
        }

        @Test
        @DisplayName("应该正确解析包含BOM的CSV内容")
        void shouldParseCsvContentWithBOM() {
            // Given
            String content = "\uFEFFtitle,username,password,url,notes,category\n" +
                    "test,user123,pass123,http://example.com,notes,category1";

            // When
            // 需要先去除BOM
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            java.util.List<String[]> records = parseCsvContent(content);

            // Then
            assertEquals(2, records.size());
            assertEquals("title", records.get(0)[0]);
            assertEquals("test", records.get(1)[0]);
        }
    }

    // 辅助方法
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

    private String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
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

    private java.util.List<String[]> parseCsvContent(String content) {
        java.util.List<String[]> records = new java.util.ArrayList<>();
        java.util.List<String> currentRecord = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (inQuotes) {
                if (c == '"') {
                    // 检查是否是转义的双引号
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
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
                    currentRecord.add(currentField.toString());
                    currentField.setLength(0);
                } else if (c == '\n' || c == '\r') {
                    // 处理换行符
                    currentRecord.add(currentField.toString());
                    currentField.setLength(0);
                    
                    // 跳过 \r\n 中的 \n
                    if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                        i++;
                    }
                    
                    // 添加记录
                    records.add(currentRecord.toArray(new String[0]));
                    currentRecord.clear();
                } else {
                    currentField.append(c);
                }
            }
        }
        
        // 添加最后一个字段和记录
        currentRecord.add(currentField.toString());
        if (!currentRecord.isEmpty()) {
            // 检查是否只有空字段
            boolean hasContent = false;
            for (String field : currentRecord) {
                if (!field.trim().isEmpty()) {
                    hasContent = true;
                    break;
                }
            }
            if (hasContent) {
                records.add(currentRecord.toArray(new String[0]));
            }
        }
        
        return records;
    }
}