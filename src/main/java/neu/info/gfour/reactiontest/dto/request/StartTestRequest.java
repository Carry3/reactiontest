package neu.info.gfour.reactiontest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import neu.info.gfour.reactiontest.entity.Test;

@Data
public class StartTestRequest {
    
    @NotNull(message = "测试类型不能为空")
    private Test.TestType testType;
}