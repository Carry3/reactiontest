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
     * 开始测试 - 创建测试记录
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
     * 完成测试 - 保存所有数据并计算统计
     */
    @Transactional
    public TestResultResponse completeTest(Long testId, User user, CompleteTestRequest request) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("测试不存在"));

        // 验证权限
        if (!test.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此测试");
        }

        // 验证状态
        if (test.getStatus() == Test.TestStatus.COMPLETED) {
            throw new RuntimeException("测试已完成，不能重复提交");
        }

        // 更新测试基本信息
        test.setEndTime(LocalDateTime.now());
        test.setStatus(Test.TestStatus.COMPLETED);
        test.setTotalTrials(request.getTotalTrials());
        test.setCorrectTrials(request.getCorrectTrials());
        test.setTotalTimeMs(request.getTotalTimeMs());

        // 保存每轮结果
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

        // 计算并保存统计数据
        TestStatistics statistics = calculateStatistics(test, request);
        testStatisticsRepository.save(statistics);

        testRepository.save(test);

        // 构建响应
        return buildTestResultResponse(test, statistics, results, true);
    }

    /**
     * 获取测试结果详情
     */
    public TestResultResponse getTestResult(Long testId, User user) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("测试不存在"));

        // 验证权限
        if (!test.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权查看此测试");
        }

        TestStatistics statistics = testStatisticsRepository.findByTest(test)
                .orElse(null);

        List<TestResult> results = testResultRepository.findByTestOrderByTrialNumber(test);

        return buildTestResultResponse(test, statistics, results, true);
    }

    /**
     * 获取用户测试历史列表
     */
    public List<TestHistoryResponse> getTestHistory(User user) {
        List<Test> tests = testRepository.findByUserOrderByStartTimeDesc(user);

        return tests.stream()
                .map(this::buildTestHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户特定类型的测试历史
     */
    public List<TestHistoryResponse> getTestHistoryByType(User user, Test.TestType testType) {
        List<Test> tests = testRepository.findByUserAndTestTypeOrderByStartTimeDesc(user, testType);

        return tests.stream()
                .map(this::buildTestHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 取消测试
     */
    @Transactional
    public void cancelTest(Long testId, User user) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("测试不存在"));

        if (!test.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作此测试");
        }

        if (test.getStatus() == Test.TestStatus.COMPLETED) {
            throw new RuntimeException("已完成的测试不能取消");
        }

        test.setStatus(Test.TestStatus.CANCELLED);
        test.setEndTime(LocalDateTime.now());
        testRepository.save(test);
    }

    // ==================== 私有方法 ====================

    /**
     * 计算统计数据
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

        // 准确率
        double accuracy = request.getTotalTrials() > 0
                ? (double) request.getCorrectTrials() / request.getTotalTrials()
                : 0.0;
        stats.setAccuracyRate(accuracy);

        if (!reactionTimes.isEmpty()) {
            // 平均反应时间
            double avg = reactionTimes.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            stats.setAvgReactionTime(avg);

            // 标准差
            double variance = reactionTimes.stream()
                    .mapToDouble(rt -> Math.pow(rt - avg, 2))
                    .average()
                    .orElse(0.0);
            stats.setStdDeviation(Math.sqrt(variance));

            // 中位数
            int size = reactionTimes.size();
            double median = size % 2 == 0
                    ? (reactionTimes.get(size / 2 - 1) + reactionTimes.get(size / 2)) / 2.0
                    : reactionTimes.get(size / 2);
            stats.setMedianReactionTime(median);

            // 最快和最慢
            stats.setFastestReactionTime(reactionTimes.get(0));
            stats.setSlowestReactionTime(reactionTimes.get(reactionTimes.size() - 1));

            // 百分位排名
            double percentile = calculatePercentileRank(test.getTestType(), avg);
            stats.setPercentileRank(percentile);
        }

        return stats;
    }

    /**
     * 计算百分位排名
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
     * 构建测试结果响应
     */
    private TestResultResponse buildTestResultResponse(Test test, TestStatistics stats,
            List<TestResult> results, boolean includeRounds) {
        TestResultResponse response = new TestResultResponse();

        // 基本信息
        response.setTestId(test.getId());
        response.setTestType(test.getTestType().name());
        response.setTestName(test.getTestType().getDisplayName());
        response.setStatus(test.getStatus().name());
        response.setStartTime(test.getStartTime());
        response.setEndTime(test.getEndTime());
        response.setTotalTimeMs(test.getTotalTimeMs());

        // 统计数据
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

            // 排名信息
            TestResultResponse.RankInfo rankInfo = new TestResultResponse.RankInfo();
            rankInfo.setPercentile(stats.getPercentileRank());
            rankInfo.setDescription(getRankDescription(stats.getPercentileRank()));
            response.setRank(rankInfo);
        }

        // 大脑区域
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

        // 每轮详情
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
     * 构建历史记录响应
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

        // 获取统计数据
        testStatisticsRepository.findByTest(test).ifPresent(stats -> {
            response.setAccuracyRate(stats.getAccuracyRate());
            response.setAvgReactionTime(stats.getAvgReactionTime());
            response.setPercentileRank(stats.getPercentileRank());
        });

        return response;
    }

    private String getRankDescription(Double percentile) {
        if (percentile == null)
            return "暂无排名";
        if (percentile >= 90)
            return "优秀！超过了 " + String.format("%.1f", percentile) + "% 的用户";
        if (percentile >= 70)
            return "良好！超过了 " + String.format("%.1f", percentile) + "% 的用户";
        if (percentile >= 50)
            return "中等水平，超过了 " + String.format("%.1f", percentile) + "% 的用户";
        if (percentile >= 30)
            return "还有提升空间，超过了 " + String.format("%.1f", percentile) + "% 的用户";
        return "需要多加练习，超过了 " + String.format("%.1f", percentile) + "% 的用户";
    }
}