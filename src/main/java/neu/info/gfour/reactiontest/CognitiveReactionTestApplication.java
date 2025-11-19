package neu.info.gfour.reactiontest;

import neu.info.gfour.reactiontest.repository.BrainRegionMappingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CognitiveReactionTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(CognitiveReactionTestApplication.class, args);
  }

  // 可选：启动时验证大脑区域数据是否初始化成功
  @Bean
  CommandLineRunner verifyData(BrainRegionMappingRepository brainRegionRepo) {
    return args -> {
      long count = brainRegionRepo.count();
      System.out.println("====================================");
      System.out.println("✅ 系统启动成功！");
      System.out.println("📊 大脑区域映射数据: " + count + " 条");
      System.out.println("====================================");
    };
  }
}