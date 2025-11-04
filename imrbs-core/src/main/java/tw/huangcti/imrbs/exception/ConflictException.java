package tw.huangcti.imrbs.exception;

/**
 * 衝突例外（如預約時間衝突）
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
