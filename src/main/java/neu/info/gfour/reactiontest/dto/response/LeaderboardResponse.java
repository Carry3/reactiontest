package neu.info.gfour.reactiontest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardResponse {
    private List<LeaderboardEntry> rankings;
    private Integer totalUsers;
    private UserRankInfo currentUser;  // 当前用户排名（如果已登录）

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LeaderboardEntry {
        private Integer rank;
        private String username;
        private Long testCount;
        private Double avgAccuracy;     // 平均准确率
        private Double avgReactionTime; // 平均反应时间
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRankInfo {
        private Integer rank;
        private Long testCount;
    }
}
