package tw.huangcti.imrbs.exception;

/**
 * 驗證失敗例外
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
