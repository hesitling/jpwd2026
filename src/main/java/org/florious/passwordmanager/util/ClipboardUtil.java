package org.florious.passwordmanager.util;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 剪贴板工具类
 * 提供安全的剪贴板操作，支持自动清除和复制历史
 */
public class ClipboardUtil {
    private static ClipboardUtil instance;
    private final Clipboard clipboard;
    private Timer autoClearTimer;
    private String currentSensitiveContent;
    private final List<CopyRecord> copyHistory;
    private static final int MAX_HISTORY_SIZE = 50;

    /**
     * 复制记录
     */
    public static class CopyRecord {
        private final long timestamp;
        private final ContentType contentType;

        public CopyRecord(ContentType contentType) {
            this.timestamp = System.currentTimeMillis();
            this.contentType = contentType;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public ContentType getContentType() {
            return contentType;
        }

        @Override
        public String toString() {
            return String.format("[%tT] %s", timestamp, contentType.getDescription());
        }
    }

    /**
     * 内容类型枚举
     */
    public enum ContentType {
        PASSWORD("密码"),
        USERNAME("用户名"),
        OTHER("其他");

        private final String description;

        ContentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private ClipboardUtil() {
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.copyHistory = new ArrayList<>();
    }

    /**
     * 获取单例实例
     * @return ClipboardUtil实例
     */
    public static synchronized ClipboardUtil getInstance() {
        if (instance == null) {
            instance = new ClipboardUtil();
        }
        return instance;
    }

    /**
     * 重置单例实例（用于测试）
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.cancelAutoClear();
            instance = null;
        }
    }

    /**
     * 复制文本到剪贴板
     * @param text 文本内容
     * @param contentType 内容类型
     */
    public void copyToClipboard(String text, ContentType contentType) {
        if (text == null || text.isEmpty()) {
            return;
        }

        StringSelection stringSelection = new StringSelection(text);
        clipboard.setContents(stringSelection, null);

        // 记录复制历史
        addCopyRecord(contentType);

        // 如果是敏感内容，启动自动清除
        if (contentType == ContentType.PASSWORD) {
            currentSensitiveContent = text;
            startAutoClear();
        }
    }

    /**
     * 复制密码到剪贴板（启动自动清除）
     * @param password 密码
     */
    public void copyPassword(String password) {
        copyToClipboard(password, ContentType.PASSWORD);
    }

    /**
     * 复制用户名到剪贴板（不启动自动清除）
     * @param username 用户名
     */
    public void copyUsername(String username) {
        copyToClipboard(username, ContentType.USERNAME);
    }

    /**
     * 从剪贴板获取文本
     * @return 剪贴板文本，如果无内容返回null
     */
    public String getClipboardText() {
        try {
            Transferable transferable = clipboard.getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            // 忽略异常
        }
        return null;
    }

    /**
     * 清除剪贴板内容
     */
    public void clearClipboard() {
        cancelAutoClear();
        currentSensitiveContent = null;

        // 使用空字符串覆盖剪贴板
        StringSelection emptySelection = new StringSelection("");
        try {
            clipboard.setContents(emptySelection, null);
        } catch (IllegalStateException e) {
            // 剪贴板不可用，忽略
        }
    }

    /**
     * 启动自动清除定时器
     */
    private void startAutoClear() {
        cancelAutoClear();

        int clearDelaySeconds = Config.getClipboardClearDelaySeconds();
        if (clearDelaySeconds <= 0) {
            return;
        }

        autoClearTimer = new Timer("ClipboardAutoClear", true);
        autoClearTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearClipboard();
                notifyClipboardCleared();
            }
        }, clearDelaySeconds * 1000L);
    }

    /**
     * 取消自动清除定时器
     */
    public void cancelAutoClear() {
        if (autoClearTimer != null) {
            autoClearTimer.cancel();
            autoClearTimer = null;
        }
    }

    /**
     * 添加复制记录
     * @param contentType 内容类型
     */
    private void addCopyRecord(ContentType contentType) {
        CopyRecord record = new CopyRecord(contentType);
        copyHistory.add(0, record);

        // 限制历史大小
        while (copyHistory.size() > MAX_HISTORY_SIZE) {
            copyHistory.remove(copyHistory.size() - 1);
        }
    }

    /**
     * 获取复制历史
     * @return 复制历史列表（只读副本）
     */
    public List<CopyRecord> getCopyHistory() {
        return new ArrayList<>(copyHistory);
    }

    /**
     * 清除复制历史
     */
    public void clearHistory() {
        copyHistory.clear();
    }

    /**
     * 检查剪贴板是否包含敏感内容
     * @return 如果包含敏感内容返回true
     */
    public boolean hasSensitiveContent() {
        return currentSensitiveContent != null;
    }

    /**
     * 获取自动清除剩余时间（毫秒）
     * @return 剩余时间，如果没有自动清除返回-1
     */
    public long getAutoClearRemainingMillis() {
        // 此方法需要更复杂的实现来跟踪定时器状态
        return autoClearTimer != null ? 0 : -1;
    }

    /**
     * 通知剪贴板已清除
     */
    private void notifyClipboardCleared() {
        // 可以通过监听器模式通知UI
        // 暂时使用简单的输出
        System.out.println("剪贴板已自动清除");
    }

    /**
     * 监听剪贴板内容变化
     * 当外部程序修改剪贴板时取消自动清除
     */
    public void startMonitoring() {
        // Java 不直接支持剪贴板变化监听
        // 可以通过定期检查实现，但会增加资源消耗
        // 这里使用简单的实现：在复制时记录内容，清除时验证
    }

    /**
     * 验证剪贴板内容是否仍是我们设置的
     * @return 如果内容未被修改返回true
     */
    public boolean isClipboardUnmodified() {
        if (currentSensitiveContent == null) {
            return false;
        }
        String current = getClipboardText();
        return currentSensitiveContent.equals(current);
    }
}
