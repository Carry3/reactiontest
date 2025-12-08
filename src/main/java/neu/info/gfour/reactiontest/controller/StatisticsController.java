package neu.info.gfour.reactiontest.controller;

import lombok.RequiredArgsConstructor;
import neu.info.gfour.reactiontest.dto.response.*;
import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.entity.User;
import neu.info.gfour.reactiontest.service.StatisticsService;
import neu.info.gfour.reactiontest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    /**
     * 获取全局统计数据
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
        return ResponseEntity.ok(statisticsService.getGlobalStats());
    }

    /**
     * 获取测试次数排行榜
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        Long currentUserId = null;
        if (authentication != null) {
            User user = userService.findByUsername(authentication.getName());
            currentUserId = user.getId();
        }
        
        return ResponseEntity.ok(statisticsService.getTestCountLeaderboard(limit, currentUserId));
    }

    /**
     * 获取单类型测试的完整统计（含分布图数据）
     */
    @GetMapping("/test-type/{testType}")
    public ResponseEntity<TestTypeStatsResponse> getTestTypeStats(
            @PathVariable Test.TestType testType) {
        
        return ResponseEntity.ok(statisticsService.getTestTypeStats(testType));
    }

    /**
     * 获取单类型测试的反应时间分布（柱状图数据）
     */
    @GetMapping("/distribution/reaction-time/{testType}")
    public ResponseEntity<DistributionResponse> getReactionTimeDistribution(
            @PathVariable Test.TestType testType) {
        
        return ResponseEntity.ok(statisticsService.getReactionTimeDistribution(testType));
    }

    /**
     * 获取单类型测试的准确率分布（柱状图数据）
     */
    @GetMapping("/distribution/accuracy/{testType}")
    public ResponseEntity<DistributionResponse> getAccuracyDistribution(
            @PathVariable Test.TestType testType) {
        
        return ResponseEntity.ok(statisticsService.getAccuracyDistribution(testType));
    }

    /**
     * 获取所有类型的统计概览
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getAllTypesOverview() {
        var overview = java.util.Arrays.stream(Test.TestType.values())
                .map(statisticsService::getTestTypeStats)
                .toList();
        return ResponseEntity.ok(overview);
    }
}