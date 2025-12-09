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
                "Responsible for executing motor instructions, controlling finger button presses"));
        mappings.add(createMapping(
                TestType.SIMPLE_REACTION,
                BrainRegion.BRAINSTEM,
                "Maintains alert state, processing basic sensory inputs"));
        mappings.add(createMapping(
                TestType.SIMPLE_REACTION,
                BrainRegion.THALAMUS,
                "The relay station of visual information transmits visual stimulation signals"));
        mappings.add(createMapping(
                TestType.SIMPLE_REACTION,
                BrainRegion.BASAL_GANGLIA,
                "Motion start-up and control, and coordinate rapid response actions"));

        // ========== 2. 选择反应时测试 ==========
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.DORSOLATERAL_PFC,
                "Decision-making center, choose the right way to respond"));
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.ANTERIOR_CINGULATE,
                "Conflict monitoring and error detection, helping to make correct choices"));
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.PARIETAL_CORTEX,
                "Attention direction, focusing attention on different stimuli"));
        mappings.add(createMapping(
                TestType.CHOICE_REACTION,
                BrainRegion.PREMOTOR_CORTEX,
                "Exercise plan and selection, prepare different key movements"));

        // ========== 3. 工作记忆测试 (N-Back) ==========
        mappings.add(createMapping(
                TestType.WORKING_MEMORY,
                BrainRegion.DORSOLATERAL_PFC,
                "The core of working memory, temporarily storing and operating information"));
        mappings.add(createMapping(
                TestType.WORKING_MEMORY,
                BrainRegion.PARIETAL_CORTEX,
                "Memory storage area, maintaining information in the brain"));
        mappings.add(createMapping(
                TestType.WORKING_MEMORY,
                BrainRegion.HIPPOCAMPUS,
                "Memory encoding and extraction, helping to form new memories"));

        // ========== 4. Go/No-Go测试 ==========
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.PREFRONTAL_CORTEX,
                "Inhibition control center, preventing unnecessary reactions"));
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.ANTERIOR_CINGULATE,
                "Conflict monitoring and reaction inhibition, detecting Go and No-Go signals"));
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.BASAL_GANGLIA,
                "Coordination of movement and inhibition, controlling impulsive behavior"));
        mappings.add(createMapping(
                TestType.GO_NO_GO,
                BrainRegion.DORSOLATERAL_PFC,
                "Execution function and cognitive control, maintaining task rules"));

        // ========== 5. Stroop测试 ==========
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.ANTERIOR_CINGULATE,
                "Conflict detection and resolution, processing color and text contradictions"));
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.DORSOLATERAL_PFC,
                "Cognitive control and selective attention, suppressing automatic reading reactions"));
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.PARIETAL_CORTEX,
                "Attention allocation and visual processing, processing color and text contradictions"));
        mappings.add(createMapping(
                TestType.STROOP,
                BrainRegion.PREFRONTAL_CORTEX,
                "Execution control and task switching, flexibly responding to conflicting stimuli"));

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