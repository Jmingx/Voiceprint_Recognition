package club.jming.voiceprintrecognition.service;

import club.jming.voiceprintrecognition.VoiceprintRecognitionApplication;
import club.jming.voiceprintrecognition.utils.AudioRecognizer;
import club.jming.voiceprintrecognition.utils.DataPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
/**
 * 需要添加启动类才能注入成功
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = VoiceprintRecognitionApplication.class)
@DirtiesContext
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class RedisServiceImplTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void setCoreMap() throws UnsupportedAudioFileException, IOException {
        int songID = 0;
        AudioRecognizer audioRecognizer = new AudioRecognizer();
        Map<Integer, Long> dp = audioRecognizer.createDP("song/1616589001661林俊杰 - 可惜没如果 (Live).mp3");
        redisService.setCoreMap(songID,dp);
    }

    @Test
    public void getFromCoreMap() throws UnsupportedAudioFileException, IOException {
        AudioRecognizer audioRecognizer = new AudioRecognizer();
        Map<Integer, Long> dp = audioRecognizer.createDP("song/1616589001661林俊杰 - 可惜没如果 (Live).mp3");
        //<songID,<relativeIndex,times>>
        Map<Integer,Map<Integer,Integer>> compareResult = new HashMap<>();
        for (Integer integer : dp.keySet()) {
            long hash = dp.get(integer);
            int timeIndex = integer;
            List<DataPoint> fromCoreMap = redisService.getFromCoreMap(hash);
            if (fromCoreMap == null){
                continue;
            }
            for (DataPoint dataPoint : fromCoreMap) {
                int songID = dataPoint.getSongID();
                int songTimeIndex = dataPoint.getTime();
                if (!compareResult.containsKey(songID)){
                    compareResult.put(songID,new HashMap<>());
                }
                Map<Integer, Integer> map = compareResult.get(songID);
                int relativeIndex = Math.abs(timeIndex - songTimeIndex);
                map.put(relativeIndex,map.getOrDefault(relativeIndex,0)+1);
            }
        }
        for (Integer integer : compareResult.keySet()) {
            for (Integer integer1 : compareResult.get(integer).keySet()) {
                System.out.println("songID : "+integer+" relativeIndex : "+integer1+" times : "+compareResult.get(integer).get(integer1));
            }
        }
    }
}
