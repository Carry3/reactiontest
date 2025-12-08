package neu.info.gfour.reactiontest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalStatsResponse {
    private Long totalUsers;
    private Long totalTests;
    private Long totalTestsToday;
    private List<TestTypeCount> testTypeCounts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestTypeCount {
        private String testType;
        private String testName;
        private Long count;
    }
}