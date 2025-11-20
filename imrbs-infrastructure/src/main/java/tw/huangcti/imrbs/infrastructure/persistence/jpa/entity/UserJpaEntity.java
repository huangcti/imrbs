package tw.huangcti.imrbs.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.huangcti.imrbs.domain.model.User;

import java.time.LocalDateTime;

/**
 * UserJpaEntity - User 的 JPA 實體
 * 
 * 描述: 對應資料庫 users 表
 * 
 * 設計: Clean Architecture - Infrastructure Layer 實作
 * 職責: 資料庫持久化，與 Domain Model (User) 分離
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_employee_id", columnList = "employee_id"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_is_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false, unique = true, length = 50)
    private String employeeId;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private User.UserRole role;
    
    @Column(name = "language_preference", nullable = false, length = 5)
    private String languagePreference;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (languagePreference == null) {
            languagePreference = "zh-TW";
        }
        if (role == null) {
            role = User.UserRole.EMPLOYEE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 轉換為 Domain Model
     * 
     * @return User domain model
     */
    public User toDomain() {
        return User.builder()
                .id(id)
                .employeeId(employeeId)
                .email(email)
                .fullName(fullName)
                .department(department)
                .role(role)
                .languagePreference(languagePreference)
                .phoneNumber(phoneNumber)
                .isActive(isActive)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
    
    /**
     * 從 Domain Model 轉換
     * 
     * @param user domain model
     * @return UserJpaEntity
     */
    public static UserJpaEntity fromDomain(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .role(user.getRole())
                .languagePreference(user.getLanguagePreference())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
