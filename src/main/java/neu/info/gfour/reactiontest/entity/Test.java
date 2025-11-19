package neu.info.gfour.reactiontest.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tests")
@Data
@NoArgsConstructor
public class Test {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "test_type", nullable = false)
  private TestType testType;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time") private LocalDateTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TestStatus status = TestStatus.IN_PROGRESS;

  @Column(name = "total_trials") private Integer totalTrials;

  @Column(name = "completed_trials") private Integer completedTrials = 0;

  // 一个测试有多个结果记录
  @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TestResult> results = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    if (startTime == null) {
      startTime = LocalDateTime.now();
    }
  }

  // 测试类型枚举
  public enum TestType {
    SIMPLE_REACTION,
    CHOICE_REACTION,
    CONTINUOUS_ATTENTION,
    WORKING_MEMORY
  }

  // 测试状态枚举
  public enum TestStatus { IN_PROGRESS, COMPLETED, CANCELLED }
}
