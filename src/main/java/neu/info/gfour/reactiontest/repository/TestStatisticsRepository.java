package neu.info.gfour.reactiontest.repository;

import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.entity.TestStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestStatisticsRepository extends JpaRepository<TestStatistics, Long> {

    Optional<TestStatistics> findByTest(Test test);

    // ========== 排名相关 ==========

    // 计算比当前测试慢的测试数量
    @Query("SELECT COUNT(ts) FROM TestStatistics ts " +
           "WHERE ts.test.testType = :testType AND ts.avgReactionTime > :avgTime")
    long countSlowerTests(@Param("testType") Test.TestType testType, 
                          @Param("avgTime") Double avgReactionTime);

    // 计算相同类型的总测试数
    @Query("SELECT COUNT(ts) FROM TestStatistics ts WHERE ts.test.testType = :testType")
    long countTotalTests(@Param("testType") Test.TestType testType);

    // ========== 分布统计 ==========

    // 获取某类型所有测试的统计数据（用于计算分布）
    @Query("SELECT ts FROM TestStatistics ts WHERE ts.test.testType = :testType")
    List<TestStatistics> findAllByTestType(@Param("testType") Test.TestType testType);

    // 获取某类型的平均反应时间
    @Query("SELECT AVG(ts.avgReactionTime) FROM TestStatistics ts " +
           "WHERE ts.test.testType = :testType")
    Double getAverageReactionTimeByType(@Param("testType") Test.TestType testType);

    // 获取某类型的平均准确率
    @Query("SELECT AVG(ts.accuracyRate) FROM TestStatistics ts " +
           "WHERE ts.test.testType = :testType")
    Double getAverageAccuracyByType(@Param("testType") Test.TestType testType);

    // 获取某类型反应时间范围
    @Query("SELECT MIN(ts.avgReactionTime), MAX(ts.avgReactionTime) FROM TestStatistics ts " +
           "WHERE ts.test.testType = :testType")
    Object[] getReactionTimeRangeByType(@Param("testType") Test.TestType testType);

    // 获取某类型准确率范围
    @Query("SELECT MIN(ts.accuracyRate), MAX(ts.accuracyRate) FROM TestStatistics ts " +
           "WHERE ts.test.testType = :testType")
    Object[] getAccuracyRangeByType(@Param("testType") Test.TestType testType);
}