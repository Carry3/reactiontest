package neu.info.gfour.reactiontest.controller;

import lombok.RequiredArgsConstructor;
import neu.info.gfour.reactiontest.entity.BrainRegionMapping;
import neu.info.gfour.reactiontest.entity.Test;
import neu.info.gfour.reactiontest.repository.BrainRegionMappingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final BrainRegionMappingRepository brainRegionMappingRepository;

    /**
     * 获取所有测试类型及其对应的大脑区域
     */
    @GetMapping("/test-types")
    public ResponseEntity<List<Map<String, Object>>> getAllTestTypesWithBrainRegions() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Test.TestType type : Test.TestType.values()) {
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("type", type.name());
            typeInfo.put("name", type.getDisplayName());
            
            // 获取对应的大脑区域
            List<BrainRegionMapping> regions = brainRegionMappingRepository.findByTestType(type);
            List<Map<String, String>> brainRegions = regions.stream()
                    .map(r -> Map.of(
                            "region", r.getBrainRegion().name(),
                            "regionName", r.getBrainRegion().getChineseName(),
                            "abbreviation", r.getBrainRegion().getAbbreviation(),
                            "description", r.getDescription() != null ? r.getDescription() : ""
                    ))
                    .collect(Collectors.toList());
            
            typeInfo.put("brainRegions", brainRegions);
            result.add(typeInfo);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取特定测试类型的大脑区域信息
     */
    @GetMapping("/brain-regions/{testType}")
    public ResponseEntity<?> getBrainRegionsForTestType(@PathVariable Test.TestType testType) {
        List<BrainRegionMapping> regions = brainRegionMappingRepository.findByTestType(testType);
        
        List<Map<String, String>> result = regions.stream()
                .map(r -> Map.of(
                        "region", r.getBrainRegion().name(),
                        "regionName", r.getBrainRegion().getChineseName(),
                        "abbreviation", r.getBrainRegion().getAbbreviation(),
                        "description", r.getDescription() != null ? r.getDescription() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "testType", testType.name(),
                "testName", testType.getDisplayName(),
                "brainRegions", result
        ));
    }

    /**
     * 获取所有大脑区域列表
     */
    @GetMapping("/brain-regions")
    public ResponseEntity<List<Map<String, String>>> getAllBrainRegions() {
        List<Map<String, String>> regions = Arrays.stream(BrainRegionMapping.BrainRegion.values())
                .map(r -> Map.of(
                        "region", r.name(),
                        "regionName", r.getChineseName(),
                        "abbreviation", r.getAbbreviation()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(regions);
    }
}