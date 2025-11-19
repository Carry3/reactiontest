package neu.info.gfour.reactiontest.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import neu.info.gfour.reactiontest.entity.BrainRegionMapping;
import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.repository.BrainRegionMappingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrainRegionDataInitializer implements CommandLineRunner {

  private final BrainRegionMappingRepository repository;

  @Override
  public void run(String... args) throws Exception {
    // 检查是否已有数据
    if (repository.count() > 0) {
      return;
    }

    List<BrainRegionMapping> mappings = new ArrayList<>();

    // ========== 简单反应时测试 ==========
    mappings.add(
        createMapping(Test.TestType.SIMPLE_REACTION,
                      BrainRegionMapping.BrainRegion.PRIMARY_MOTOR_CORTEX,
                      "负责执行运动指令，控制手指按键动作"));

    mappings.add(createMapping(Test.TestType.SIMPLE_REACTION,
                               BrainRegionMapping.BrainRegion.BRAINSTEM,
                               "维持警觉状态，处理基本的感觉输入"));

    mappings.add(createMapping(Test.TestType.SIMPLE_REACTION,
                               BrainRegionMapping.BrainRegion.THALAMUS,
                               "视觉信息的中继站，传递视觉刺激信号"));

    mappings.add(createMapping(Test.TestType.SIMPLE_REACTION,
                               BrainRegionMapping.BrainRegion.BASAL_GANGLIA,
                               "运动启动和控制，协调快速反应动作"));

    // ========== 选择反应时测试 ==========
    mappings.add(createMapping(Test.TestType.CHOICE_REACTION,
                               BrainRegionMapping.BrainRegion.DORSOLATERAL_PFC,
                               "决策制定中心，选择正确的反应方式"));

    mappings.add(
        createMapping(Test.TestType.CHOICE_REACTION,
                      BrainRegionMapping.BrainRegion.ANTERIOR_CINGULATE,
                      "冲突监测和错误检测，帮助你做出正确选择"));

    mappings.add(createMapping(Test.TestType.CHOICE_REACTION,
                               BrainRegionMapping.BrainRegion.PARIETAL_CORTEX,
                               "注意力定向，将注意力集中在不同的刺激上"));

    mappings.add(createMapping(Test.TestType.CHOICE_REACTION,
                               BrainRegionMapping.BrainRegion.PREMOTOR_CORTEX,
                               "运动计划和选择，准备不同的按键动作"));

    // ========== 连续性注意力测试 ==========
    mappings.add(createMapping(Test.TestType.CONTINUOUS_ATTENTION,
                               BrainRegionMapping.BrainRegion.PREFRONTAL_CORTEX,
                               "持续注意力维持，长时间保持专注状态"));

    mappings.add(createMapping(Test.TestType.CONTINUOUS_ATTENTION,
                               BrainRegionMapping.BrainRegion.PARIETAL_CORTEX,
                               "视空间注意网络，监控屏幕上的目标"));

    mappings.add(createMapping(Test.TestType.CONTINUOUS_ATTENTION,
                               BrainRegionMapping.BrainRegion.LOCUS_COERULEUS,
                               "警觉性调节中心，保持大脑清醒和专注"));

    mappings.add(
        createMapping(Test.TestType.CONTINUOUS_ATTENTION,
                      BrainRegionMapping.BrainRegion.ANTERIOR_CINGULATE,
                      "抑制冲动反应，避免对非目标刺激做出反应"));

    // ========== 工作记忆测试 ==========
    mappings.add(createMapping(Test.TestType.WORKING_MEMORY,
                               BrainRegionMapping.BrainRegion.DORSOLATERAL_PFC,
                               "工作记忆的核心，暂时存储和操作信息"));

    mappings.add(createMapping(Test.TestType.WORKING_MEMORY,
                               BrainRegionMapping.BrainRegion.PARIETAL_CORTEX,
                               "记忆信息的存储区域，保持信息在脑中"));

    mappings.add(createMapping(Test.TestType.WORKING_MEMORY,
                               BrainRegionMapping.BrainRegion.HIPPOCAMPUS,
                               "记忆编码和提取，帮助形成新的记忆"));

    // 批量保存
    repository.saveAll(mappings);

    System.out.println("✅ 大脑区域映射数据初始化完成！共 " + mappings.size() +
                       " 条记录。");
  }

  private BrainRegionMapping
  createMapping(Test.TestType testType,
                BrainRegionMapping.BrainRegion brainRegion,
                String description) {

    BrainRegionMapping mapping = new BrainRegionMapping();
    mapping.setTestType(testType);
    mapping.setBrainRegion(brainRegion);
    mapping.setDescription(description);

    return mapping;
  }
}