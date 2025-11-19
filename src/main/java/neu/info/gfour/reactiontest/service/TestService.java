package neu.info.gfour.reactiontest.service;

import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.entity.TestResult;
import neu.info.gfour.reactiontest.entity.User;
import neu.info.gfour.reactiontest.repository.TestRepository;
import neu.info.gfour.reactiontest.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {
    
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    
    @Transactional
    public Test startTest(User user, Test.TestType testType, int totalTrials) {
        Test test = new Test();
        test.setUser(user);
        test.setTestType(testType);
        test.setTotalTrials(totalTrials);
        test.setStatus(Test.TestStatus.IN_PROGRESS);
        
        return testRepository.save(test);
    }
    
    @Transactional
    public TestResult submitTrialResult(Long testId, int trialNumber, 
                                       String stimulus, String response, 
                                       int reactionTime, boolean isCorrect) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("测试不存在"));
        
        TestResult result = new TestResult();
        result.setTest(test);
        result.setTrialNumber(trialNumber);
        result.setStimulus(stimulus);
        result.setResponse(response);
        result.setReactionTime(reactionTime);
        result.setIsCorrect(isCorrect);
        
        // 更新测试完成的试次数
        test.setCompletedTrials(test.getCompletedTrials() + 1);
        
        // 如果所有试次完成，标记测试为完成
        if (test.getCompletedTrials().equals(test.getTotalTrials())) {
            test.setStatus(Test.TestStatus.COMPLETED);
            test.setEndTime(LocalDateTime.now());
        }
        
        testRepository.save(test);
        return testResultRepository.save(result);
    }
    
    public List<Test> getUserTests(User user) {
        return testRepository.findByUserOrderByStartTimeDesc(user);
    }
    
    public Test getTestById(Long testId) {
        return testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("测试不存在"));
    }
}