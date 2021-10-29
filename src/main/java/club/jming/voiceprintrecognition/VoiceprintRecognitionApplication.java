package club.jming.voiceprintrecognition;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
//@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@MapperScan("club.jming.voiceprintrecognition.dao")
public class VoiceprintRecognitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceprintRecognitionApplication.class, args);
    }

}
