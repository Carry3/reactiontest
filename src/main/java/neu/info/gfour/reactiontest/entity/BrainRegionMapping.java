package neu.info.gfour.reactiontest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brain_region_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrainRegionMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private Test.TestType testType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "brain_region", nullable = false)
    private BrainRegion brainRegion;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // 大脑区域枚举
    public enum BrainRegion {
        PRIMARY_MOTOR_CORTEX("初级运动皮层", "M1"),
        PREFRONTAL_CORTEX("前额叶皮层", "PFC"),
        DORSOLATERAL_PFC("背外侧前额叶皮层", "DLPFC"),
        ANTERIOR_CINGULATE("前扣带回皮层", "ACC"),
        PARIETAL_CORTEX("顶叶皮层", "PC"),
        HIPPOCAMPUS("海马体", "HPC"),
        BASAL_GANGLIA("基底神经节", "BG"),
        THALAMUS("丘脑", "THL"),
        BRAINSTEM("脑干", "BS"),
        PREMOTOR_CORTEX("运动前区", "PMC"),
        LOCUS_COERULEUS("蓝斑", "LC");
        
        private final String chineseName;
        private final String abbreviation;
        
        BrainRegion(String chineseName, String abbreviation) {
            this.chineseName = chineseName;
            this.abbreviation = abbreviation;
        }
        
        public String getChineseName() {
            return chineseName;
        }
        
        public String getAbbreviation() {
            return abbreviation;
        }
    }
}