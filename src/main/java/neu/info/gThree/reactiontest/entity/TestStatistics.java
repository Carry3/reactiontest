package neu.info.gThree.reactiontest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStatistics {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @OneToOne
  @JoinColumn(name = "test_id", unique = true, nullable = false)
  private Test test;

  // 平均反应时间（毫秒）
  @Column(name = "avg_reaction_time") private Double avgReactionTime;

  // 标准差
  @Column(name = "std_deviation") private Double stdDeviation;

  // 准确率（0.0-1.0）
  @Column(name = "accuracy_rate") private Double accuracyRate;

  // 中位数反应时间
  @Column(name = "median_reaction_time") private Double medianReactionTime;

  // 最快反应时间
  @Column(name = "fastest_reaction_time") private Integer fastestReactionTime;

  // 最慢反应时间
  @Column(name = "slowest_reaction_time") private Integer slowestReactionTime;

  // 总试次数
  @Column(name = "total_trials") private Integer totalTrials;

  // 正确试次数
  @Column(name = "correct_trials") private Integer correctTrials;

  // 排名百分位（0-100，越高越好）
  @Column(name = "percentile_rank") private Double percentileRank;
}