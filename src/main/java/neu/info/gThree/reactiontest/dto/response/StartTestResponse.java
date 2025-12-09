package neu.info.gThree.reactiontest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import neu.info.gThree.reactiontest.entity.Test;

@Data
@AllArgsConstructor
public class StartTestResponse {
    private Long testId;
    private Test.TestType testType;
    private String testName;
    private String message;
}