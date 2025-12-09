package neu.info.gThree.reactiontest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tests")
@Data
@NoArgsConstructor
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private TestType testType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestStatus status = TestStatus.IN_PROGRESS;

    @Column(name = "total_trials")
    private Integer totalTrials;

    @Column(name = "correct_trials")
    private Integer correctTrials;

    @Column(name = "total_time_ms")
    private Long totalTimeMs;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TestResult> results = new ArrayList<>();

    @OneToOne(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private TestStatistics statistics;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }

    // ===== Test Type Enum (5 types) =====
    public enum TestType {
        SIMPLE_REACTION("Simple Reaction"),
        CHOICE_REACTION("Choice Reaction"),
        WORKING_MEMORY("Working Memory"),
        GO_NO_GO("Go / No-Go"),
        STROOP("Stroop");

        private final String displayName;

        TestType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ===== Test Status Enum =====
    public enum TestStatus {
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}