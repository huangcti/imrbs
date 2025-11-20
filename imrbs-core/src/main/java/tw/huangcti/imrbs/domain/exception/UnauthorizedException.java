package tw.huangcti.imrbs.domain.exception;

/**
 * 未授權異常
 * 當使用者無權限執行操作時拋出
 */
public class UnauthorizedException extends DomainException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
