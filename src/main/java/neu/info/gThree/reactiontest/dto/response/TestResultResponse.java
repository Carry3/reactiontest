package neu.info.gThree.reactiontest.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestResultResponse {
    
    // 测试基本信息
    private Long testId;
    private String testType;
    private String testName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalTimeMs;
    
    // 统计数据
    private Statistics statistics;
    
    // 大脑区域信息
    private List<BrainRegionInfo> brainRegions;
    
    // 排名信息
    private RankInfo rank;
    
    // 每轮详情（可选，查询详情时返回）
    private List<RoundDetail> rounds;
    
    @Data
    public static class Statistics {
        private Integer totalTrials;        // 总轮数
        private Integer correctTrials;      // 正确轮数
        private Double accuracyRate;        // 准确率 (0-1)
        private Double avgReactionTime;     // 平均反应时间
        private Double medianReactionTime;  // 中位数
        private Double stdDeviation;        // 标准差
        private Integer fastestTime;        // 最快
        private Integer slowestTime;        // 最慢
    }
    
    @Data
    public static class BrainRegionInfo {
        private String region;              // 区域代码
        private String regionName;          // 区域中文名
        private String abbreviation;        // 缩写
        private String description;         // 描述
    }
    
    @Data
    public static class RankInfo {
        private Double percentile;          // 百分位排名
        private String description;         // 排名描述
    }
    
    @Data
    public static class RoundDetail {
        private Integer trialNumber;
        private String stimulus;
        private String response;
        private Integer reactionTime;
        private Boolean isCorrect;
    }
}