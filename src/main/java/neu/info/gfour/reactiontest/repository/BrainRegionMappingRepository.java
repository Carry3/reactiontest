package neu.info.gfour.reactiontest.repository;

import neu.info.gfour.reactiontest.entity.BrainRegionMapping;
import neu.info.gfour.reactiontest.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BrainRegionMappingRepository extends JpaRepository<BrainRegionMapping, Long> {
    
    /**
     * 根据测试类型查找对应的大脑区域映射
     */
    List<BrainRegionMapping> findByTestType(Test.TestType testType);
}