package neu.info.gfour.reactiontest.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.entity.User;
import neu.info.gfour.reactiontest.service.AnalyticsService;
import neu.info.gfour.reactiontest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsService analyticsService;
  private final UserService userService;

  /**
   * 获取单次测试的详细分析
   */
  @GetMapping("/test/{testId}")
  public ResponseEntity<Map<String, Object>>
  getTestAnalysis(@PathVariable Long testId, Authentication authentication) {

    // TODO: 验证用户权限
    Map<String, Object> analysis = analyticsService.getTestAnalysis(testId);
    return ResponseEntity.ok(analysis);
  }

  /**
   * 获取测试类型信息（包含激活的大脑区域）
   */
  @GetMapping("/test-type/{testType}")
  public ResponseEntity<Map<String, Object>>
  getTestTypeInfo(@PathVariable Test.TestType testType) {

    Map<String, Object> info = analyticsService.getTestTypeInfo(testType);
    return ResponseEntity.ok(info);
  }

  /**
   * 获取所有测试类型及其对应的大脑区域
   */
  @GetMapping("/test-types")
  public ResponseEntity<List<Map<String, Object>>> getAllTestTypes() {
    List<Map<String, Object>> testTypes = new ArrayList<>();

    for (Test.TestType type : Test.TestType.values()) {
      testTypes.add(analyticsService.getTestTypeInfo(type));
    }

    return ResponseEntity.ok(testTypes);
  }
}