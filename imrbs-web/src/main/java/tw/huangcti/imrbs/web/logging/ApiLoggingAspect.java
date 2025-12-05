package tw.huangcti.imrbs.web.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;

/**
 * API 請求日誌切面.
 * 
 * <p>自動記錄 Controller 層的請求/回應日誌，包含：</p>
 * <ul>
 *   <li>請求追蹤 ID (Request ID)</li>
 *   <li>請求方法和路徑</li>
 *   <li>執行時間</li>
 *   <li>異常資訊</li>
 * </ul>
 */
@Aspect
@Component
public class ApiLoggingAspect {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String REQUEST_PATH_KEY = "requestPath";

    /**
     * Controller 層切點.
     */
    @Pointcut("within(tw.huangcti.imrbs.web.controller..*)")
    public void controllerPointcut() {
        // Pointcut for all controllers
    }

    /**
     * Service 層切點.
     */
    @Pointcut("within(tw.huangcti.imrbs.core.application.usecase..*)")
    public void useCasePointcut() {
        // Pointcut for all use cases
    }

    /**
     * 記錄 Controller 請求日誌.
     */
    @Around("controllerPointcut()")
    public Object logControllerRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        // 設定 MDC 上下文
        setupMdcContext();
        
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 記錄請求
            if (logger.isDebugEnabled()) {
                logger.debug("API Request: {}() with arguments: {}", 
                        methodName, 
                        sanitizeArgs(args));
            }
            
            // 執行方法
            Object result = joinPoint.proceed();
            
            // 記錄回應
            long duration = System.currentTimeMillis() - startTime;
            logger.info("API Response: {}() completed in {} ms", methodName, duration);
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("API Error: {}() failed after {} ms - {}: {}", 
                    methodName, duration, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } finally {
            clearMdcContext();
        }
    }

    /**
     * 記錄 Use Case 執行日誌.
     */
    @Around("useCasePointcut()")
    public Object logUseCaseExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("UseCase Start: {}.{}()", className, methodName);
            }
            
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            if (logger.isDebugEnabled()) {
                logger.debug("UseCase Complete: {}.{}() in {} ms", className, methodName, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.warn("UseCase Failed: {}.{}() after {} ms - {}", 
                    className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * 記錄未捕獲的異常.
     */
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        logger.error("Unhandled exception in {}.{}(): {}", 
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                exception.getMessage(),
                exception);
    }

    /**
     * 設定 MDC 上下文.
     */
    private void setupMdcContext() {
        // 生成請求 ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID_KEY, requestId);
        
        // 取得請求資訊
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            MDC.put(REQUEST_PATH_KEY, request.getMethod() + " " + request.getRequestURI());
            
            // 從 Principal 取得用戶 ID (若有)
            if (request.getUserPrincipal() != null) {
                MDC.put(USER_ID_KEY, request.getUserPrincipal().getName());
            }
        }
    }

    /**
     * 清除 MDC 上下文.
     */
    private void clearMdcContext() {
        MDC.remove(REQUEST_ID_KEY);
        MDC.remove(USER_ID_KEY);
        MDC.remove(REQUEST_PATH_KEY);
    }

    /**
     * 清理敏感參數.
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    }
                    String argStr = arg.toString();
                    // 隱藏可能的敏感資訊
                    if (argStr.toLowerCase().contains("password") 
                            || argStr.toLowerCase().contains("secret")
                            || argStr.toLowerCase().contains("token")) {
                        return "[REDACTED]";
                    }
                    // 限制長度
                    if (argStr.length() > 200) {
                        return argStr.substring(0, 200) + "...";
                    }
                    return argStr;
                })
                .toList()
                .toString();
    }
}
