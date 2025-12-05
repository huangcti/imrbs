package tw.huangcti.imrbs.domain.exception;

/**
 * ForbiddenException - 權限不足異常
 * 
 * 當使用者試圖訪問或操作沒有權限的資源時拋出
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
