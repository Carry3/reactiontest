package neu.info.gThree.reactiontest.controller;

import lombok.RequiredArgsConstructor;
import neu.info.gThree.reactiontest.dto.response.*;
import neu.info.gThree.reactiontest.entity.Test;
import neu.info.gThree.reactiontest.entity.User;
import neu.info.gThree.reactiontest.service.StatisticsService;
import neu.info.gThree.reactiontest.service.UserService;

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
     * Get global statistics
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
        return ResponseEntity.ok(statisticsService.getGlobalStats());
    }

    /**
     * Get test count leaderboard
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
     * Get complete statistics for single test type (including distribution data)
     */
    @GetMapping("/test-type/{testType}")
    public ResponseEntity<TestTypeStatsResponse> getTestTypeStats(
            @PathVariable Test.TestType testType) {

        return ResponseEntity.ok(statisticsService.getTestTypeStats(testType));
    }

    /**
     * Get reaction time distribution for single test type (bar chart data)
     */
    @GetMapping("/distribution/reaction-time/{testType}")
    public ResponseEntity<DistributionResponse> getReactionTimeDistribution(
            @PathVariable Test.TestType testType) {

        return ResponseEntity.ok(statisticsService.getReactionTimeDistribution(testType));
    }

    /**
     * Get accuracy distribution for single test type (bar chart data)
     */
    @GetMapping("/distribution/accuracy/{testType}")
    public ResponseEntity<DistributionResponse> getAccuracyDistribution(
            @PathVariable Test.TestType testType) {

        return ResponseEntity.ok(statisticsService.getAccuracyDistribution(testType));
    }

    /**
     * Get overview of all test types
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getAllTypesOverview() {
        var overview = java.util.Arrays.stream(Test.TestType.values())
                .map(statisticsService::getTestTypeStats)
                .toList();
        return ResponseEntity.ok(overview);
    }
}