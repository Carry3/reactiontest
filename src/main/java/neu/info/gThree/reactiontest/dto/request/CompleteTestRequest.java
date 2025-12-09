package neu.info.gThree.reactiontest.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.List;

@Data
public class CompleteTestRequest {
    
    @NotNull(message = "总时间不能为空")
    @Min(value = 0, message = "总时间不能为负数")
    private Long totalTimeMs;  // 总耗时（毫秒）
    
    @NotNull(message = "总轮数不能为空")
    @Min(value = 1, message = "至少需要1轮")
    private Integer totalTrials;
    
    @NotNull(message = "正确轮数不能为空")
    @Min(value = 0, message = "正确轮数不能为负数")
    private Integer correctTrials;
    
    @NotEmpty(message = "测试轮次数据不能为空")
    private List<RoundResult> rounds;
    
    @Data
    public static class RoundResult {
        private Integer trialNumber;      // 轮次编号
        private String stimulus;          // 刺激类型
        private String response;          // 用户响应
        private Integer reactionTime;     // 反应时间(ms)
        private Boolean isCorrect;        // 是否正确
    }
}