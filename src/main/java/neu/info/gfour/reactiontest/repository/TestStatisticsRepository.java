package neu.info.gfour.reactiontest.repository;

import neu.info.gfour.reactiontest.entity.TestStatistics;
import neu.info.gfour.reactiontest.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TestStatisticsRepository extends JpaRepository<TestStatistics, Long> {
    
    Optional<TestStatistics> findByTest(Test test);
    
    // 计算在相同测试类型中比这个测试慢的测试数量
    @Query("SELECT COUNT(ts) FROM TestStatistics ts " +
           "WHERE ts.test.testType = ?1 " +
           "AND ts.avgReactionTime > ?2")
    long countSlowerTests(Test.TestType testType, Double avgReactionTime);
    
    // 计算相同测试类型的总测试数
    @Query("SELECT COUNT(ts) FROM TestStatistics ts " +
           "WHERE ts.test.testType = ?1")
    long countTotalTests(Test.TestType testType);
}