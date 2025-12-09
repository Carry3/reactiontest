package neu.info.gThree.reactiontest.service;

import lombok.RequiredArgsConstructor;
import neu.info.gThree.reactiontest.dto.response.*;
import neu.info.gThree.reactiontest.dto.response.DistributionResponse.DistributionBucket;
import neu.info.gThree.reactiontest.dto.response.LeaderboardResponse.*;
import neu.info.gThree.reactiontest.entity.Test;
import neu.info.gThree.reactiontest.entity.TestStatistics;
import neu.info.gThree.reactiontest.repository.TestRepository;
import neu.info.gThree.reactiontest.repository.TestStatisticsRepository;
import neu.info.gThree.reactiontest.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TestRepository testRepository;
    private final TestStatisticsRepository testStatisticsRepository;
    private final UserRepository userRepository;

    /**
     * Get test count leaderboard
     */
    public LeaderboardResponse getTestCountLeaderboard(int limit, Long currentUserId) {
        // Get leaderboard data
        List<Object[]> rawData = testRepository.getTestCountLeaderboard(PageRequest.of(0, limit));

        List<LeaderboardEntry> rankings = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rawData) {
            Long userId = (Long) row[0];
            String username = (String) row[1];
            Long testCount = (Long) row[2];

            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setRank(rank++);
            entry.setUsername(username);
            entry.setTestCount(testCount);

            rankings.add(entry);
        }

        // Get total user count
        int totalUsers = (int) testRepository.countDistinctUsers();

        // Get current user ranking
        UserRankInfo currentUser = null;
        if (currentUserId != null) {
            Long userTestCount = testRepository.countCompletedTestsByUser(
                    userRepository.findById(currentUserId).orElse(null));
            Long betterUsers = testRepository.getUserTestCountRank(currentUserId);

            currentUser = new UserRankInfo();
            currentUser.setRank(betterUsers == null ? 1 : betterUsers.intValue() + 1);
            currentUser.setTestCount(userTestCount);
        }

        return new LeaderboardResponse(rankings, totalUsers, currentUser);
    }

    /**
     * Get statistics for single test type (including distribution data)
     */
    public TestTypeStatsResponse getTestTypeStats(Test.TestType testType) {
        List<TestStatistics> allStats = testStatisticsRepository.findAllByTestType(testType);

        if (allStats.isEmpty()) {
            TestTypeStatsResponse response = new TestTypeStatsResponse();
            response.setTestType(testType.name());
            response.setTestName(testType.getDisplayName());
            response.setTotalTests(0);
            return response;
        }

        TestTypeStatsResponse response = new TestTypeStatsResponse();
        response.setTestType(testType.name());
        response.setTestName(testType.getDisplayName());
        response.setTotalTests(allStats.size());

        // Calculate reaction time statistics
        DoubleSummaryStatistics rtStats = allStats.stream()
                .filter(s -> s.getAvgReactionTime() != null)
                .mapToDouble(TestStatistics::getAvgReactionTime)
                .summaryStatistics();

        response.setAvgReactionTime(rtStats.getAverage());
        response.setMinReactionTime(rtStats.getMin());
        response.setMaxReactionTime(rtStats.getMax());

        // Calculate accuracy statistics
        DoubleSummaryStatistics accStats = allStats.stream()
                .filter(s -> s.getAccuracyRate() != null)
                .mapToDouble(TestStatistics::getAccuracyRate)
                .summaryStatistics();

        response.setAvgAccuracy(accStats.getAverage());
        response.setMinAccuracy(accStats.getMin());
        response.setMaxAccuracy(accStats.getMax());

        // Generate reaction time distribution (bar chart data)
        response.setReactionTimeDistribution(
                buildReactionTimeDistribution(allStats));

        // Generate accuracy distribution (bar chart data)
        response.setAccuracyDistribution(
                buildAccuracyDistribution(allStats));

        return response;
    }

    /**
     * Get global statistics
     */
    public GlobalStatsResponse getGlobalStats() {
        GlobalStatsResponse response = new GlobalStatsResponse();

        response.setTotalUsers(testRepository.countDistinctUsers());
        response.setTotalTests(testRepository.countAllCompletedTests());

        // Today's test count
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        response.setTotalTestsToday(testRepository.countTodayCompletedTests(startOfDay));

        // Test count by type
        List<Object[]> typeCounts = testRepository.countByTestType();
        List<GlobalStatsResponse.TestTypeCount> testTypeCounts = typeCounts.stream()
                .map(row -> {
                    Test.TestType type = (Test.TestType) row[0];
                    Long count = (Long) row[1];
                    return new GlobalStatsResponse.TestTypeCount(
                            type.name(),
                            type.getDisplayName(),
                            count);
                })
                .collect(Collectors.toList());
        response.setTestTypeCounts(testTypeCounts);

        return response;
    }

    /**
     * Get reaction time distribution (for bar chart)
     */
    public DistributionResponse getReactionTimeDistribution(Test.TestType testType) {
        List<TestStatistics> allStats = testStatisticsRepository.findAllByTestType(testType);

        DistributionResponse response = new DistributionResponse();
        response.setTestType(testType.name());
        response.setTestName(testType.getDisplayName());
        response.setTotalTests(allStats.size());
        response.setBuckets(buildReactionTimeDistribution(allStats));

        return response;
    }

    /**
     * Get accuracy distribution (for bar chart)
     */
    public DistributionResponse getAccuracyDistribution(Test.TestType testType) {
        List<TestStatistics> allStats = testStatisticsRepository.findAllByTestType(testType);

        DistributionResponse response = new DistributionResponse();
        response.setTestType(testType.name());
        response.setTestName(testType.getDisplayName());
        response.setTotalTests(allStats.size());
        response.setBuckets(buildAccuracyDistribution(allStats));

        return response;
    }

    // ==================== Private Methods ====================

    /**
     * Build reaction time distribution buckets
     * Intervals: 0-200, 200-250, 250-300, 300-350, 350-400, 400-500, 500+
     */
    private List<DistributionBucket> buildReactionTimeDistribution(List<TestStatistics> stats) {
        // Define interval boundaries (milliseconds)
        int[] boundaries = { 0, 200, 250, 300, 350, 400, 500, Integer.MAX_VALUE };
        String[] labels = { "<200ms", "200-250ms", "250-300ms", "300-350ms",
                "350-400ms", "400-500ms", ">500ms" };

        int[] counts = new int[labels.length];
        int total = 0;

        for (TestStatistics stat : stats) {
            if (stat.getAvgReactionTime() == null)
                continue;
            double rt = stat.getAvgReactionTime();
            total++;

            for (int i = 0; i < boundaries.length - 1; i++) {
                if (rt >= boundaries[i] && rt < boundaries[i + 1]) {
                    counts[i]++;
                    break;
                }
            }
        }

        List<DistributionBucket> buckets = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            double percentage = total > 0 ? (counts[i] * 100.0 / total) : 0;
            buckets.add(new DistributionBucket(labels[i], counts[i],
                    Math.round(percentage * 10) / 10.0));
        }

        return buckets;
    }

    /**
     * Build accuracy distribution buckets
     * Intervals: 0-50%, 50-60%, 60-70%, 70-80%, 80-90%, 90-100%
     */
    private List<DistributionBucket> buildAccuracyDistribution(List<TestStatistics> stats) {
        // Define interval boundaries
        double[] boundaries = { 0, 0.5, 0.6, 0.7, 0.8, 0.9, 1.01 };
        String[] labels = { "<50%", "50-60%", "60-70%", "70-80%", "80-90%", "90-100%" };

        int[] counts = new int[labels.length];
        int total = 0;

        for (TestStatistics stat : stats) {
            if (stat.getAccuracyRate() == null)
                continue;
            double acc = stat.getAccuracyRate();
            total++;

            for (int i = 0; i < boundaries.length - 1; i++) {
                if (acc >= boundaries[i] && acc < boundaries[i + 1]) {
                    counts[i]++;
                    break;
                }
            }
        }

        List<DistributionBucket> buckets = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            double percentage = total > 0 ? (counts[i] * 100.0 / total) : 0;
            buckets.add(new DistributionBucket(labels[i], counts[i],
                    Math.round(percentage * 10) / 10.0));
        }

        return buckets;
    }
}