package neu.info.gfour.reactiontest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestTypeStatsResponse {
    private String testType;
    private String testName;
    private Integer totalTests;
    
    // 反应时间统计
    private Double avgReactionTime;
    private Double minReactionTime;
    private Double maxReactionTime;
    
    // 准确率统计
    private Double avgAccuracy;
    private Double minAccuracy;
    private Double maxAccuracy;
    
    // 分布数据（用于柱状图）
    private List<DistributionResponse.DistributionBucket> reactionTimeDistribution;
    private List<DistributionResponse.DistributionBucket> accuracyDistribution;
}