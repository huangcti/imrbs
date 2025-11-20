package tw.huangcti.imrbs.web.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 審計日誌註解
 * 標記需要記錄操作日誌的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    
    /**
     * 操作類型 (CREATE, UPDATE, DELETE, APPROVE, REJECT, etc.)
     */
    String action();
    
    /**
     * 操作的資源類型 (Reservation, Room, User, etc.)
     */
    String resource() default "";
    
    /**
     * 額外描述
     */
    String description() default "";
}
