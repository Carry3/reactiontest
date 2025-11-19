package neu.info.gfour.reactiontest.controller;

import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.entity.TestResult;
import neu.info.gfour.reactiontest.entity.User;
import neu.info.gfour.reactiontest.service.AnalyticsService;
import neu.info.gfour.reactiontest.service.TestService;
import neu.info.gfour.reactiontest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    
    private final TestService testService;
    private final UserService userService;
    private final AnalyticsService analyticsService;
    
    /**
     * 开始新测试
     */
    @PostMapping("/start")
    public ResponseEntity<Test> startTest(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        // 从认证对象中获取当前用户
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        String testType = request.get("testType").toString();
        int totalTrials = Integer.parseInt(request.get("totalTrials").toString());
        
        Test test = testService.startTest(user, Test.TestType.valueOf(testType), totalTrials);
        return ResponseEntity.ok(test);
    }
    
    /**
     * 提交单次试验结果
     */
    @PostMapping("/{testId}/submit")
    public ResponseEntity<TestResult> submitTrial(
            @PathVariable Long testId,
            @RequestBody Map<String, Object> trialData,
            Authentication authentication) {
        
        // 验证测试是否属于当前用户
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        Test test = testService.getTestById(testId);
        
        if (!test.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build(); // 禁止访问
        }
        
        int trialNumber = Integer.parseInt(trialData.get("trialNumber").toString());
        String stimulus = trialData.get("stimulus").toString();
        String response = trialData.get("response").toString();
        int reactionTime = Integer.parseInt(trialData.get("reactionTime").toString());
        boolean isCorrect = Boolean.parseBoolean(trialData.get("isCorrect").toString());
        
        TestResult result = testService.submitTrialResult(
            testId, trialNumber, stimulus, response, reactionTime, isCorrect
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 完成测试并生成分析（新增）
     */
    @PostMapping("/{testId}/complete")
    public ResponseEntity<Map<String, Object>> completeTest(
            @PathVariable Long testId,
            Authentication authentication) {
        
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        Test test = testService.getTestById(testId);
        
        // 验证权限
        if (!test.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        // 生成统计和分析
        Map<String, Object> analysis = analyticsService.getTestAnalysis(testId);
        
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * 获取测试详情
     */
    @GetMapping("/{testId}")
    public ResponseEntity<Test> getTest(
            @PathVariable Long testId,
            Authentication authentication) {
        
        Test test = testService.getTestById(testId);
        
        // 验证权限
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (!test.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(test);
    }
    
    /**
     * 获取当前用户的测试历史
     */
    @GetMapping("/my-tests")
    public ResponseEntity<List<Test>> getMyTests(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        List<Test> tests = testService.getUserTests(user);
        return ResponseEntity.ok(tests);
    }
    
    /**
     * 获取支持的测试类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getTestTypes() {
        List<Map<String, Object>> testTypes = List.of(
                Map.of(
                        "type", "SIMPLE_REACTION",
                        "name", "简单反应时测试",
                        "description", "评估基础反应速度和警觉性",
                        "duration", "2-3分钟",
                        "brainRegions", List.of("初级运动皮层", "脑干", "丘脑")
                ),
                Map.of(
                        "type", "CHOICE_REACTION",
                        "name", "选择反应时测试",
                        "description", "评估决策速度和选择性注意力",
                        "duration", "3-5分钟",
                        "brainRegions", List.of("前额叶皮层", "顶叶皮层", "前扣带回")
                ),
                Map.of(
                        "type", "CONTINUOUS_ATTENTION",
                        "name", "连续性注意力测试",
                        "description", "评估持续注意力和抗干扰能力",
                        "duration", "10-15分钟",
                        "brainRegions", List.of("右额叶", "顶叶注意网络", "蓝斑")
                ),
                Map.of(
                        "type", "WORKING_MEMORY",
                        "name", "工作记忆测试",
                        "description", "评估短期记忆容量和信息处理能力",
                        "duration", "5-8分钟",
                        "brainRegions", List.of("背外侧前额叶", "顶叶皮层", "海马体")
                )
        );
        
        return ResponseEntity.ok(testTypes);
    }
}