package neu.info.gfour.reactiontest.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import neu.info.gfour.reactiontest.entity.BrainRegionMapping;
import neu.info.gfour.reactiontest.entity.BrainRegionMapping.BrainRegion;
import neu.info.gfour.reactiontest.entity.Test.TestType;
import neu.info.gfour.reactiontest.repository.BrainRegionMappingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrainRegionDataInitializer implements CommandLineRunner {

    private final BrainRegionMappingRepository repository;

    @Override
    public void run(String... args) {
        // 检查是否已有数据
        if (repository.count() > 0) {
            return;
        }

        List<BrainRegionMapping> mappings = new ArrayList<>();

        // ========== 1. 简单反应时测试 ==========
        mappings.add(createMapping(
                TestType.SIMPLE_REACTION,
                BrainRegion.PRIMARY_MOTOR_CORTEX,
                "负责执行运动指令，控制手指按键动作"));
        mappings.add(createMapping(
                TestType.SIMPLE_REACTION,
                BrainRegion.BRAINSTEM,
                "维持警觉状态，处理基本的感觉输入"));
        mappings.add(createMapping(
                TestType.SIMPLE_REACTION,
                BrainRegion.THALAMUS,
                "视觉信息的中继站，传递视觉刺激信号"));
        mappings.add(createMapping(
                TestType.SIMPLE_REACTION,
                BrainRegion.BASAL_GANGLIA,
                "运动启动和控制，协调快速反应动作"));

        // ========== 2. 选择反应时测试 ==========
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.DORSOLATERAL_PFC,
                "决策制定中心，选择正确的反应方式"));
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.ANTERIOR_CINGULATE,
                "冲突监测和错误检测，帮助做出正确选择"));
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.PARIETAL_CORTEX,
                "注意力定向，将注意力集中在不同的刺激上"));
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.PREMOTOR_CORTEX,
                "运动计划和选择，准备不同的按键动作"));

        // ========== 3. 工作记忆测试 (N-Back) ==========
        mappings.add(createMapping(
                TestType.WORKING_MEMORY,
                BrainRegion.DORSOLATERAL_PFC,
                "工作记忆的核心，暂时存储和操作信息"));
        mappings.add(createMapping(
                TestType.WORKING_MEMORY,
                BrainRegion.PARIETAL_CORTEX,
                "记忆信息的存储区域，保持信息在脑中"));
        mappings.add(createMapping(
                TestType.WORKING_MEMORY,
                BrainRegion.HIPPOCAMPUS,
                "记忆编码和提取，帮助形成新的记忆"));

        // ========== 4. Go/No-Go测试 ==========
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.PREFRONTAL_CORTEX,
                "抑制控制中心，阻止不必要的反应"));
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.ANTERIOR_CINGULATE,
                "冲突监测和反应抑制，检测Go和No-Go信号"));
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.BASAL_GANGLIA,
                "运动启动和抑制的协调，控制冲动行为"));
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.DORSOLATERAL_PFC,
                "执行功能和认知控制，维持任务规则"));

        // ========== 5. Stroop测试 ==========
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.ANTERIOR_CINGULATE,
                "冲突检测和解决，处理颜色与文字的矛盾信息"));
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.DORSOLATERAL_PFC,
                "认知控制和选择性注意，抑制自动化阅读反应"));
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.PARIETAL_CORTEX,
                "注意力分配和视觉处理，同时处理颜色和文字"));
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.PREFRONTAL_CORTEX,
                "执行控制和任务切换，灵活应对冲突刺激"));

        // 批量保存
        repository.saveAll(mappings);

        System.out.println("✅ 大脑区域映射数据初始化完成！共 " + mappings.size() + " 条记录");
        System.out.println("   - SIMPLE_REACTION: 4 个区域");
        System.out.println("   - CHOICE_REACTION: 4 个区域");
        System.out.println("   - WORKING_MEMORY: 3 个区域");
        System.out.println("   - GO_NO_GO: 4 个区域");
        System.out.println("   - STROOP: 4 个区域");
    }

    private BrainRegionMapping createMapping(TestType testType, BrainRegion brainRegion, String description) {
        BrainRegionMapping mapping = new BrainRegionMapping();
        mapping.setTestType(testType);
        mapping.setBrainRegion(brainRegion);
        mapping.setDescription(description);
        return mapping;
    }
}