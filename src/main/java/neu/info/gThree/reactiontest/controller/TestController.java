package neu.info.gThree.reactiontest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import neu.info.gThree.reactiontest.dto.request.CompleteTestRequest;
import neu.info.gThree.reactiontest.dto.request.StartTestRequest;
import neu.info.gThree.reactiontest.dto.response.MessageResponse;
import neu.info.gThree.reactiontest.dto.response.StartTestResponse;
import neu.info.gThree.reactiontest.dto.response.TestHistoryResponse;
import neu.info.gThree.reactiontest.dto.response.TestResultResponse;
import neu.info.gThree.reactiontest.entity.Test;
import neu.info.gThree.reactiontest.entity.User;
import neu.info.gThree.reactiontest.service.TestService;
import neu.info.gThree.reactiontest.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final UserService userService;

    /**
     * 获取支持的测试类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getTestTypes() {
        List<Map<String, Object>> types = Arrays.stream(Test.TestType.values())
                .map(type -> Map.<String, Object>of(
                        "type", type.name(),
                        "name", type.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    /**
     * 开始新测试
     */
    @PostMapping("/start")
    public ResponseEntity<?> startTest(
            @Valid @RequestBody StartTestRequest request,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Test test = testService.startTest(user, request.getTestType());
            
            StartTestResponse response = new StartTestResponse(
                    test.getId(),
                    test.getTestType(),
                    test.getTestType().getDisplayName(),
                    "测试已创建，请开始测试"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("创建测试失败: " + e.getMessage()));
        }
    }

    /**
     * 完成测试 - 提交所有测试数据
     */
    @PostMapping("/{testId}/complete")
    public ResponseEntity<?> completeTest(
            @PathVariable Long testId,
            @Valid @RequestBody CompleteTestRequest request,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            TestResultResponse result = testService.completeTest(testId, user, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("提交测试失败: " + e.getMessage()));
        }
    }

    /**
     * 获取测试结果详情
     */
    @GetMapping("/{testId}")
    public ResponseEntity<?> getTestResult(
            @PathVariable Long testId,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            TestResultResponse result = testService.getTestResult(testId, user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("获取测试结果失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户测试历史列表
     */
    @GetMapping("/history")
    public ResponseEntity<?> getTestHistory(
            @RequestParam(required = false) Test.TestType testType,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            List<TestHistoryResponse> history;
            
            if (testType != null) {
                history = testService.getTestHistoryByType(user, testType);
            } else {
                history = testService.getTestHistory(user);
            }
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("获取历史记录失败: " + e.getMessage()));
        }
    }

    /**
     * 取消测试
     */
    @PostMapping("/{testId}/cancel")
    public ResponseEntity<?> cancelTest(
            @PathVariable Long testId,
            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            testService.cancelTest(testId, user);
            return ResponseEntity.ok(new MessageResponse("测试已取消"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("取消测试失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
}