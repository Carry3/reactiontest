package neu.info.gThree.reactiontest.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import neu.info.gThree.reactiontest.entity.Test;
import neu.info.gThree.reactiontest.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    // ========== 基础查询 ==========
    
    List<Test> findByUserOrderByStartTimeDesc(User user);

    List<Test> findByUserAndTestTypeOrderByStartTimeDesc(User user, Test.TestType testType);

    List<Test> findByUserAndStatusOrderByStartTimeDesc(User user, Test.TestStatus status);

    // ========== 统计查询 ==========

    // 用户完成的测试数量
    @Query("SELECT COUNT(t) FROM Test t WHERE t.user = :user AND t.status = 'COMPLETED'")
    long countCompletedTestsByUser(@Param("user") User user);

    // 用户特定类型完成的测试数量
    @Query("SELECT COUNT(t) FROM Test t WHERE t.user = :user AND t.testType = :type AND t.status = 'COMPLETED'")
    long countCompletedTestsByUserAndType(@Param("user") User user, @Param("type") Test.TestType testType);

    // 总完成测试数
    @Query("SELECT COUNT(t) FROM Test t WHERE t.status = 'COMPLETED'")
    long countAllCompletedTests();

    // 今日完成测试数
    @Query("SELECT COUNT(t) FROM Test t WHERE t.status = 'COMPLETED' AND t.startTime >= :startOfDay")
    long countTodayCompletedTests(@Param("startOfDay") LocalDateTime startOfDay);

    // 各类型测试数量统计
    @Query("SELECT t.testType, COUNT(t) FROM Test t WHERE t.status = 'COMPLETED' GROUP BY t.testType")
    List<Object[]> countByTestType();

    // ========== 排行榜查询 ==========

    // 测试次数排行榜（返回userId和count）
    @Query("SELECT t.user.id, t.user.username, COUNT(t) as cnt " +
           "FROM Test t WHERE t.status = 'COMPLETED' " +
           "GROUP BY t.user.id, t.user.username " +
           "ORDER BY cnt DESC")
    List<Object[]> getTestCountLeaderboard(Pageable pageable);

    // 获取用户测试次数排名
    @Query("SELECT COUNT(DISTINCT u.id) FROM Test t JOIN t.user u " +
           "WHERE t.status = 'COMPLETED' " +
           "GROUP BY u.id " +
           "HAVING COUNT(t) > (SELECT COUNT(t2) FROM Test t2 WHERE t2.user.id = :userId AND t2.status = 'COMPLETED')")
    Long getUserTestCountRank(@Param("userId") Long userId);

    // 总参与用户数
    @Query("SELECT COUNT(DISTINCT t.user.id) FROM Test t WHERE t.status = 'COMPLETED'")
    long countDistinctUsers();
}