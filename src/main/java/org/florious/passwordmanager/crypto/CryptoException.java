package org.florious.passwordmanager.crypto;

/**
 * 加密异常类
 * 用于处理加密、解密、哈希等操作中的异常
 */
public class CryptoException extends RuntimeException {

    /**
     * 创建加密异常
     * @param message 错误消息
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * 创建加密异常
     * @param message 错误消息
     * @param cause 原因异常
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建加密异常
     * @param cause 原因异常
     */
    public CryptoException(Throwable cause) {
        super(cause);
    }
}