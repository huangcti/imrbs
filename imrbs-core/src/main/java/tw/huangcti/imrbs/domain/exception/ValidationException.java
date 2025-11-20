package tw.huangcti.imrbs.domain.exception;

/**
 * Validation Exception - 驗證失敗異常
 */
public class ValidationException extends DomainException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
