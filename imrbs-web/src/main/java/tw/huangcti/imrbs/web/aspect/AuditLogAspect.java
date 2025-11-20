package tw.huangcti.imrbs.web.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * AuditLogAspect - 審計日誌 AOP
 * 
 * 功能:
 * - 記錄重要操作 (新增、修改、刪除)
 * - 記錄操作者、操作時間、操作結果
 * - 記錄異常操作
 * 
 * 使用方式:
 * - 在方法上加上 @Audited 註解
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {
    
    /**
     * 方法執行前記錄
     */
    @Before("@annotation(audited)")
    public void logBefore(JoinPoint joinPoint, Audited audited) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        log.info("[AUDIT] User: {}, Action: {}, Method: {}, Args: {}, Time: {}",
                username,
                audited.action(),
                methodName,
                Arrays.toString(args),
                LocalDateTime.now()
        );
    }
    
    /**
     * 方法執行成功後記錄
     */
    @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Audited audited, Object result) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().toShortString();
        
        log.info("[AUDIT] User: {}, Action: {}, Method: {}, Result: SUCCESS, Return: {}, Time: {}",
                username,
                audited.action(),
                methodName,
                result != null ? result.getClass().getSimpleName() : "void",
                LocalDateTime.now()
        );
    }
    
    /**
     * 方法執行異常後記錄
     */
    @AfterThrowing(pointcut = "@annotation(audited)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Audited audited, Exception ex) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().toShortString();
        
        log.error("[AUDIT] User: {}, Action: {}, Method: {}, Result: FAILED, Error: {}, Time: {}",
                username,
                audited.action(),
                methodName,
                ex.getMessage(),
                LocalDateTime.now()
        );
    }
    
    /**
     * 取得當前使用者名稱
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        
        return "anonymous";
    }
}

/**
 * 審計日誌註解
 * 
 * 使用範例:
 * @Audited(action = "CREATE_RESERVATION", resource = "Reservation")
 * public Reservation createReservation(ReservationRequest request) { ... }
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
