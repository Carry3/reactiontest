package neu.info.gfour.reactiontest.service;

import neu.info.gfour.reactiontest.dto.request.CompleteTestRequest;
import neu.info.gfour.reactiontest.dto.response.TestHistoryResponse;
import neu.info.gfour.reactiontest.dto.response.TestResultResponse;
import neu.info.gfour.reactiontest.entity.*;
import neu.info.gfour.reactiontest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final TestStatisticsRepository testStatisticsRepository;
    private final BrainRegionMappingRepository brainRegionMappingRepository;

    /**
     * Start test - create test record
     */
    @Transactional
    public Test startTest(User user, Test.TestType testType) {
        Test test = new Test();
        test.setUser(user);
        test.setTestType(testType);
        test.setStatus(Test.TestStatus.IN_PROGRESS);
        return testRepository.save(test);
    }

    /**
     * Complete test - save all data and calculate statistics
     */
    @Transactional
    public TestResultResponse completeTest(Long testId, User user, CompleteTestRequest request) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test does not exist"));

        // Verify permission
        if (!test.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No permission to operate this test");
        }

        // Verify status
        if (test.getStatus() == Test.TestStatus.COMPLETED) {
            throw new RuntimeException("Test already completed, cannot resubmit");
        }

        // Update test basic information
        test.setEndTime(LocalDateTime.now());
        test.setStatus(Test.TestStatus.COMPLETED);
        test.setTotalTrials(request.getTotalTrials());
        test.setCorrectTrials(request.getCorrectTrials());
        test.setTotalTimeMs(request.getTotalTimeMs());

        // Save each round result
        List<TestResult> results = new ArrayList<>();
        for (CompleteTestRequest.RoundResult round : request.getRounds()) {
            TestResult result = new TestResult();
            result.setTest(test);
            result.setTrialNumber(round.getTrialNumber());
            result.setStimulus(round.getStimulus());
            result.setResponse(round.getResponse());
            result.setReactionTime(round.getReactionTime());
            result.setIsCorrect(round.getIsCorrect());
            results.add(result);
        }
        testResultRepository.saveAll(results);

        // Calculate and save statistics
        TestStatistics statistics = calculateStatistics(test, request);
        testStatisticsRepository.save(statistics);

        // Recalculate percentile ranks for all tests of the same type
        recalculatePercentileRanksForType(test.getTestType());

        testRepository.save(test);

        // Build response
        return buildTestResultResponse(test, statistics, results, true);
    }

    /**
     * Get test result details
     */
    public TestResultResponse getTestResult(Long testId, User user) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test does not exist"));

        // Verify permission
        if (!test.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No permission to view this test");
        }

        TestStatistics statistics = testStatisticsRepository.findByTest(test)
                .orElse(null);

        List<TestResult> results = testResultRepository.findByTestOrderByTrialNumber(test);

        return buildTestResultResponse(test, statistics, results, true);
    }

    /**
     * Get user test history list
     */
    public List<TestHistoryResponse> getTestHistory(User user) {
        List<Test> tests = testRepository.findByUserOrderByStartTimeDesc(user);

        return tests.stream()
                .map(this::buildTestHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user test history by specific type
     */
    public List<TestHistoryResponse> getTestHistoryByType(User user, Test.TestType testType) {
        List<Test> tests = testRepository.findByUserAndTestTypeOrderByStartTimeDesc(user, testType);

        return tests.stream()
                .map(this::buildTestHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel test
     */
    @Transactional
    public void cancelTest(Long testId, User user) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test does not exist"));

        if (!test.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No permission to operate this test");
        }

        if (test.getStatus() == Test.TestStatus.COMPLETED) {
            throw new RuntimeException("Completed tests cannot be cancelled");
        }

        test.setStatus(Test.TestStatus.CANCELLED);
        test.setEndTime(LocalDateTime.now());
        testRepository.save(test);
    }

    // ==================== Private Methods ====================

    /**
     * Calculate statistics
     */
    private TestStatistics calculateStatistics(Test test, CompleteTestRequest request) {
        List<Integer> reactionTimes = request.getRounds().stream()
                .map(CompleteTestRequest.RoundResult::getReactionTime)
                .filter(Objects::nonNull)
                .filter(rt -> rt > 0)
                .sorted()
                .collect(Collectors.toList());

        TestStatistics stats = new TestStatistics();
        stats.setTest(test);
        stats.setTotalTrials(request.getTotalTrials());
        stats.setCorrectTrials(request.getCorrectTrials());

        // Accuracy rate
        double accuracy = request.getTotalTrials() > 0
                ? (double) request.getCorrectTrials() / request.getTotalTrials()
                : 0.0;
        stats.setAccuracyRate(accuracy);

        if (!reactionTimes.isEmpty()) {
            // Average reaction time
            double avg = reactionTimes.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            stats.setAvgReactionTime(avg);

            // Standard deviation
            double variance = reactionTimes.stream()
                    .mapToDouble(rt -> Math.pow(rt - avg, 2))
                    .average()
                    .orElse(0.0);
            stats.setStdDeviation(Math.sqrt(variance));

            // Median
            int size = reactionTimes.size();
            double median = size % 2 == 0
                    ? (reactionTimes.get(size / 2 - 1) + reactionTimes.get(size / 2)) / 2.0
                    : reactionTimes.get(size / 2);
            stats.setMedianReactionTime(median);

            // Fastest and slowest
            stats.setFastestReactionTime(reactionTimes.get(0));
            stats.setSlowestReactionTime(reactionTimes.get(reactionTimes.size() - 1));

            // Percentile rank
            double percentile = calculatePercentileRank(test.getTestType(), avg);
            stats.setPercentileRank(percentile);
        }

        return stats;
    }

    /**
     * Calculate percentile rank
     */
    private double calculatePercentileRank(Test.TestType testType, Double avgReactionTime) {
        long totalTests = testStatisticsRepository.countTotalTests(testType);
        if (totalTests == 0) {
            return 50.0;
        }
        long slowerTests = testStatisticsRepository.countSlowerTests(testType, avgReactionTime);
        return (slowerTests * 100.0) / totalTests;
    }

    /**
     * Recalculate percentile ranks for all tests of a certain test type
     * Called when a new test is completed to ensure all ranks of the same type are
     * up to date
     */
    @Transactional
    private void recalculatePercentileRanksForType(Test.TestType testType) {
        // Get all test statistics for this type
        List<TestStatistics> allStats = testStatisticsRepository.findAllByTestType(testType);

        if (allStats.isEmpty()) {
            return;
        }

        // Recalculate percentile rank for each test
        for (TestStatistics stats : allStats) {
            if (stats.getAvgReactionTime() != null) {
                double newPercentile = calculatePercentileRank(testType, stats.getAvgReactionTime());
                stats.setPercentileRank(newPercentile);
            }
        }

        // Batch save updates
        testStatisticsRepository.saveAll(allStats);
    }

    /**
     * Build test result response
     */
    private TestResultResponse buildTestResultResponse(Test test, TestStatistics stats,
            List<TestResult> results, boolean includeRounds) {
        TestResultResponse response = new TestResultResponse();

        // Basic information
        response.setTestId(test.getId());
        response.setTestType(test.getTestType().name());
        response.setTestName(test.getTestType().getDisplayName());
        response.setStatus(test.getStatus().name());
        response.setStartTime(test.getStartTime());
        response.setEndTime(test.getEndTime());
        response.setTotalTimeMs(test.getTotalTimeMs());

        // Statistics
        if (stats != null) {
            TestResultResponse.Statistics statistics = new TestResultResponse.Statistics();
            statistics.setTotalTrials(stats.getTotalTrials());
            statistics.setCorrectTrials(stats.getCorrectTrials());
            statistics.setAccuracyRate(stats.getAccuracyRate());
            statistics.setAvgReactionTime(stats.getAvgReactionTime());
            statistics.setMedianReactionTime(stats.getMedianReactionTime());
            statistics.setStdDeviation(stats.getStdDeviation());
            statistics.setFastestTime(stats.getFastestReactionTime());
            statistics.setSlowestTime(stats.getSlowestReactionTime());
            response.setStatistics(statistics);

            // Rank information
            TestResultResponse.RankInfo rankInfo = new TestResultResponse.RankInfo();
            rankInfo.setPercentile(stats.getPercentileRank());
            rankInfo.setDescription(getRankDescription(stats.getPercentileRank()));
            response.setRank(rankInfo);
        }

        // Brain regions
        List<BrainRegionMapping> brainRegions = brainRegionMappingRepository.findByTestType(test.getTestType());
        List<TestResultResponse.BrainRegionInfo> brainRegionInfos = brainRegions.stream()
                .map(br -> {
                    TestResultResponse.BrainRegionInfo info = new TestResultResponse.BrainRegionInfo();
                    info.setRegion(br.getBrainRegion().name());
                    info.setRegionName(br.getBrainRegion().getChineseName());
                    info.setAbbreviation(br.getBrainRegion().getAbbreviation());
                    info.setDescription(br.getDescription());
                    return info;
                })
                .collect(Collectors.toList());
        response.setBrainRegions(brainRegionInfos);

        // Round details
        if (includeRounds && results != null) {
            List<TestResultResponse.RoundDetail> roundDetails = results.stream()
                    .map(r -> {
                        TestResultResponse.RoundDetail detail = new TestResultResponse.RoundDetail();
                        detail.setTrialNumber(r.getTrialNumber());
                        detail.setStimulus(r.getStimulus());
                        detail.setResponse(r.getResponse());
                        detail.setReactionTime(r.getReactionTime());
                        detail.setIsCorrect(r.getIsCorrect());
                        return detail;
                    })
                    .collect(Collectors.toList());
            response.setRounds(roundDetails);
        }

        return response;
    }

    /**
     * Build history record response
     */
    private TestHistoryResponse buildTestHistoryResponse(Test test) {
        TestHistoryResponse response = new TestHistoryResponse();
        response.setTestId(test.getId());
        response.setTestType(test.getTestType().name());
        response.setTestName(test.getTestType().getDisplayName());
        response.setStatus(test.getStatus().name());
        response.setStartTime(test.getStartTime());
        response.setEndTime(test.getEndTime());
        response.setTotalTimeMs(test.getTotalTimeMs());
        response.setTotalTrials(test.getTotalTrials());
        response.setCorrectTrials(test.getCorrectTrials());

        // Get statistics
        testStatisticsRepository.findByTest(test).ifPresent(stats -> {
            response.setAccuracyRate(stats.getAccuracyRate());
            response.setAvgReactionTime(stats.getAvgReactionTime());
            response.setPercentileRank(stats.getPercentileRank());
        });

        return response;
    }

    private String getRankDescription(Double percentile) {
        if (percentile == null)
            return "No rank yet";
        if (percentile >= 90)
            return "Excellent! Better than " + String.format("%.1f", percentile) + "% of users";
        if (percentile >= 70)
            return "Good! Better than " + String.format("%.1f", percentile) + "% of users";
        if (percentile >= 50)
            return "Average, better than " + String.format("%.1f", percentile) + "% of users";
        if (percentile >= 30)
            return "Room for improvement, better than " + String.format("%.1f", percentile) + "% of users";
        return "Needs more practice, better than " + String.format("%.1f", percentile) + "% of users";
    }
}