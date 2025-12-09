package neu.info.gThree.reactiontest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributionResponse {
    private String testType;
    private String testName;
    private Integer totalTests;
    private List<DistributionBucket> buckets;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DistributionBucket {
        private String range;      // 例如 "200-250ms" 或 "80%-90%"
        private Integer count;     // 该区间的测试数量
        private Double percentage; // 占比
    }
}