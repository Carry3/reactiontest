package neu.info.gfour.reactiontest.repository;

import java.util.List;
import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

  // 查询某个用户的所有测试
  List<Test> findByUserOrderByStartTimeDesc(User user);

  // 查询某个用户特定类型的测试
  List<Test> findByUserAndTestType(User user, Test.TestType testType);

  // 自定义查询：统计用户完成的测试数量
  @Query("SELECT COUNT(t) FROM Test t WHERE t.user = ?1 AND t.status = "
         + "'COMPLETED'")
  long
  countCompletedTestsByUser(User user);
}
