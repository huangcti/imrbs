package tw.huangcti.imrbs.exception;

/**
 * 資源未找到例外
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
