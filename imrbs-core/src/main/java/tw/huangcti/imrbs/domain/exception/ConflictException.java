package tw.huangcti.imrbs.domain.exception;

/**
 * Conflict Exception - 資源衝突異常
 */
public class ConflictException extends DomainException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
