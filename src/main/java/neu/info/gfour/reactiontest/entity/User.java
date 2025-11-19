package neu.info.gfour.reactiontest.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data // Lombok注解，自动生成getter/setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(unique = true, nullable = false, length = 50) private String username;

  @Column(unique = true, nullable = false, length = 100) private String email;

  @Column(nullable = false, name = "password_hash") private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role = UserRole.USER;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_login_at") private LocalDateTime lastLoginAt;

  @Column(name = "is_active") private Boolean isActive = true;

  // 在保存前自动设置创建时间
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  // 用户角色枚举
  public enum UserRole { USER, ADMIN, RESEARCHER }
}