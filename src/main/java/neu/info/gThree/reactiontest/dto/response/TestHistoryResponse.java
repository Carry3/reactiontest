package neu.info.gThree.reactiontest.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestHistoryResponse {
    private Long testId;
    private String testType;
    private String testName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalTimeMs;
    
    // 简要统计
    private Integer totalTrials;
    private Integer correctTrials;
    private Double accuracyRate;
    private Double avgReactionTime;
    private Double percentileRank;
}