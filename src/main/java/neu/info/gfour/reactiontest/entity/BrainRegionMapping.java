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
        PRIMARY_MOTOR_CORTEX("Primary Motor Cortex", "M1"),
        PREFRONTAL_CORTEX("Prefrontal Cortex", "PFC"),
        DORSOLATERAL_PFC("Dorsolateral Prefrontal Cortex", "DLPFC"),
        ANTERIOR_CINGULATE("Anterior Cingulate", "ACC"),
        PARIETAL_CORTEX("Parietal Cortex", "PC"),
        HIPPOCAMPUS("Hippocampus", "HPC"),
        BASAL_GANGLIA("Basal Ganglia", "BG"),
        THALAMUS("Thalamus", "THL"),
        BRAINSTEM("Brainstem", "BS"),
        PREMOTOR_CORTEX("Premotor Cortex", "PMC"),
        LOCUS_COERULEUS("Locus Coeruleus", "LC");
        
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