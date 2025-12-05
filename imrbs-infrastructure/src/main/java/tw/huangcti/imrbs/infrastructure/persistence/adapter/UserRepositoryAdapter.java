package tw.huangcti.imrbs.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tw.huangcti.imrbs.domain.model.User;
import tw.huangcti.imrbs.domain.repository.UserRepository;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.entity.UserJpaEntity;
import tw.huangcti.imrbs.infrastructure.persistence.jpa.repository.UserJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserRepositoryAdapter - UserRepository 的實作
 * 
 * 描述: 連接 Domain Layer 與 Infrastructure Layer 的適配器
 * 
 * 設計: Clean Architecture - Adapter Pattern
 * 職責: 
 * - 實作 Domain Layer 的 UserRepository 介面
 * - 委派給 Spring Data JPA Repository
 * - 轉換 JPA Entity <-> Domain Model
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    
    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByEmployeeId(String employeeId) {
        return jpaRepository.findByEmployeeId(employeeId)
                .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public List<User> findByRole(User.UserRole role) {
        return jpaRepository.findByRole(role).stream()
                .map(UserJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> findAllActive() {
        return jpaRepository.findByIsActiveTrue().stream()
                .map(UserJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> findAllAdmins() {
        return jpaRepository.findAllAdmins().stream()
                .map(UserJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public User save(User user) {
        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public void deleteById(Long id) {
        // 軟刪除: 設置 isActive = false
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setIsActive(false);
            jpaRepository.save(entity);
        });
    }
    
    @Override
    public boolean existsByEmployeeId(String employeeId) {
        return jpaRepository.existsByEmployeeId(employeeId);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        // 使用 employeeId 作為 username (對應 SSO 登入)
        return jpaRepository.findByEmployeeId(username)
                .map(UserJpaEntity::toDomain);
    }
}
