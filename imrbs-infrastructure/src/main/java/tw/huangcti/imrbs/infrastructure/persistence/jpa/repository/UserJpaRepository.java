package tw.huangcti.imrbs.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.UserJpaEntity;

import java.util.List;
import java.util.Optional;

/**
 * UserJpaRepository - Spring Data JPA Repository
 * 
 * 描述: User 的 Spring Data JPA 實作
 * 
 * 設計: Infrastructure Layer - 實作 Domain Layer 的 UserRepository 介面
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    
    /**
     * 根據員工編號查詢使用者
     * 
     * @param employeeId 員工編號
     * @return Optional<UserJpaEntity>
     */
    Optional<UserJpaEntity> findByEmployeeId(String employeeId);
    
    /**
     * 根據 Email 查詢使用者
     * 
     * @param email Email
     * @return Optional<UserJpaEntity>
     */
    Optional<UserJpaEntity> findByEmail(String email);
    
    /**
     * 根據角色查詢使用者清單
     * 
     * @param role 角色
     * @return List<UserJpaEntity>
     */
    List<UserJpaEntity> findByRole(User.UserRole role);
    
    /**
     * 查詢所有啟用中的使用者
     * 
     * @return List<UserJpaEntity>
     */
    List<UserJpaEntity> findByIsActiveTrue();
    
    /**
     * 查詢所有管理員 (ROOM_ADMIN + SYSTEM_ADMIN)
     * 
     * @return List<UserJpaEntity>
     */
    @Query("SELECT u FROM UserJpaEntity u WHERE u.role IN ('ROOM_ADMIN', 'SYSTEM_ADMIN')")
    List<UserJpaEntity> findAllAdmins();
    
    /**
     * 檢查員工編號是否已存在
     * 
     * @param employeeId 員工編號
     * @return true 如果已存在
     */
    boolean existsByEmployeeId(String employeeId);
    
    /**
     * 檢查 Email 是否已存在
     * 
     * @param email Email
     * @return true 如果已存在
     */
    boolean existsByEmail(String email);
}
