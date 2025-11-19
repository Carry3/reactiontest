package neu.info.gfour.reactiontest.service;

import neu.info.gfour.reactiontest.entity.*;
import neu.info.gfour.reactiontest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final TestStatisticsRepository testStatisticsRepository;
    private final BrainRegionMappingRepository brainRegionMappingRepository;
    
    /**
     * 计算测试统计数据
     */
    @Transactional
    public TestStatistics calculateTestStatistics(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("测试不存在"));
        
        List<TestResult> results = testResultRepository.findByTestOrderByTrialNumber(test);
        
        if (results.isEmpty()) {
            throw new RuntimeException("没有测试结果数据");
        }
        
        // 提取反应时间数据（只计算有反应时间的试次）
        List<Integer> reactionTimes = results.stream()
                .map(TestResult::getReactionTime)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        
        if (reactionTimes.isEmpty()) {
            throw new RuntimeException("没有有效的反应时间数据");
        }
        
        // 创建统计对象
        TestStatistics stats = new TestStatistics();
        stats.setTest(test);
        stats.setTotalTrials(results.size());
        
        // 计算正确试次数
        int correctTrials = (int) results.stream()
                .filter(r -> r.getIsCorrect() != null && r.getIsCorrect())
                .count();
        stats.setCorrectTrials(correctTrials);
        
        // 准确率
        double accuracy = (double) correctTrials / stats.getTotalTrials();
        stats.setAccuracyRate(accuracy);
        
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
                ? (reactionTimes.get(size/2 - 1) + reactionTimes.get(size/2)) / 2.0
                : reactionTimes.get(size/2);
        stats.setMedianReactionTime(median);
        
        // 最快和最慢
        stats.setFastestReactionTime(reactionTimes.get(0));
        stats.setSlowestReactionTime(reactionTimes.get(reactionTimes.size() - 1));
        
        // 计算排名
        double percentile = calculatePercentileRank(test.getTestType(), avg);
        stats.setPercentileRank(percentile);
        
        return testStatisticsRepository.save(stats);
    }
    
    /**
     * 计算百分位排名
     * 返回值：在相同测试类型中，比你慢的人的百分比
     * 例如：90 表示你比90%的人快
     */
    private double calculatePercentileRank(Test.TestType testType, Double avgReactionTime) {
        long totalTests = testStatisticsRepository.countTotalTests(testType);
        
        if (totalTests == 0) {
            return 50.0; // 如果是第一个测试，默认50%
        }
        
        long slowerTests = testStatisticsRepository.countSlowerTests(testType, avgReactionTime);
        
        // 计算百分位：比你慢的人的比例
        return (slowerTests * 100.0) / totalTests;
    }
    
    /**
     * 获取测试类型对应的大脑区域
     */
    public List<BrainRegionMapping> getBrainRegionsForTest(Test.TestType testType) {
        return brainRegionMappingRepository.findByTestType(testType);
    }
    
    /**
     * 获取测试详细分析（简化版）
     */
    public Map<String, Object> getTestAnalysis(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("测试不存在"));
        
        // 获取或计算统计数据
        TestStatistics stats = testStatisticsRepository.findByTest(test)
                .orElseGet(() -> calculateTestStatistics(testId));
        
        // 获取对应的大脑区域
        List<BrainRegionMapping> brainRegions = getBrainRegionsForTest(test.getTestType());
        
        // 组装返回数据
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("test", test);
        analysis.put("statistics", stats);
        analysis.put("brainRegions", brainRegions);
        analysis.put("rankDescription", getRankDescription(stats.getPercentileRank()));
        
        return analysis;
    }
    
    /**
     * 根据百分位生成排名描述
     */
    private String getRankDescription(Double percentile) {
        if (percentile == null) {
            return "暂无排名";
        }
        
        if (percentile >= 90) {
            return "优秀！你的反应速度超过了 " + String.format("%.1f", percentile) + "% 的用户";
        } else if (percentile >= 70) {
            return "良好！你的反应速度超过了 " + String.format("%.1f", percentile) + "% 的用户";
        } else if (percentile >= 50) {
            return "中等水平，你的反应速度超过了 " + String.format("%.1f", percentile) + "% 的用户";
        } else if (percentile >= 30) {
            return "还有提升空间，你的反应速度超过了 " + String.format("%.1f", percentile) + "% 的用户";
        } else {
            return "需要多加练习，你的反应速度超过了 " + String.format("%.1f", percentile) + "% 的用户";
        }
    }
    
    /**
     * 获取测试类型的详细信息（包含激活的大脑区域）
     */
    public Map<String, Object> getTestTypeInfo(Test.TestType testType) {
        List<BrainRegionMapping> brainRegions = getBrainRegionsForTest(testType);
        
        Map<String, Object> info = new HashMap<>();
        info.put("testType", testType);
        info.put("testName", getTestTypeName(testType));
        info.put("description", getTestTypeDescription(testType));
        info.put("activatedBrainRegions", brainRegions);
        
        return info;
    }
    
    private String getTestTypeName(Test.TestType testType) {
        switch (testType) {
            case SIMPLE_REACTION: return "简单反应时测试";
            case CHOICE_REACTION: return "选择反应时测试";
            case CONTINUOUS_ATTENTION: return "连续性注意力测试";
            case WORKING_MEMORY: return "工作记忆测试";
            default: return "未知测试";
        }
    }
    
    private String getTestTypeDescription(Test.TestType testType) {
        switch (testType) {
            case SIMPLE_REACTION: 
                return "评估基础反应速度和警觉性";
            case CHOICE_REACTION: 
                return "评估决策速度和选择性注意力";
            case CONTINUOUS_ATTENTION: 
                return "评估持续注意力和抗干扰能力";
            case WORKING_MEMORY: 
                return "评估短期记忆容量和信息处理能力";
            default: 
                return "";
        }
    }
}