package neu.info.gThree.reactiontest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import neu.info.gThree.reactiontest.entity.Test;
import neu.info.gThree.reactiontest.entity.TestResult;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {

  // 查询某个测试的所有结果
  List<TestResult> findByTestOrderByTrialNumber(Test test);

  // 统计某个测试的平均反应时间
  @Query("SELECT AVG(tr.reactionTime) FROM TestResult tr WHERE tr.test = ?1")
  Double calculateAverageReactionTime(Test test);

  // 计算准确率
  @Query("SELECT COUNT(tr) * 100.0 / (SELECT COUNT(*) FROM TestResult WHERE " +
         "test = ?1) "
         + "FROM TestResult tr WHERE tr.test = ?1 AND tr.isCorrect = true")
  Double
  calculateAccuracyRate(Test test);
}