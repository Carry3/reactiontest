package neu.info.gfour.reactiontest.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
public class TestResult {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_id", nullable = false)
  private Test test;

  @Column(name = "trial_number", nullable = false) private Integer trialNumber;

  @Column(length = 100) private String stimulus;

  @Column(length = 100) private String response;

  @Column(name = "reaction_time") private Integer reactionTime; // 毫秒

  @Column(name = "is_correct") private Boolean isCorrect;

  @Column(nullable = false) private LocalDateTime timestamp;

  @PrePersist
  protected void onCreate() {
    timestamp = LocalDateTime.now();
  }
}