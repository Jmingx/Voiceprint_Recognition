package club.jming.voiceprintrecognition.utils;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Map;

public class AudioRecognizerTest {

    @Test
    public void createDP() throws UnsupportedAudioFileException, IOException {
        AudioRecognizer audioRecognizer = new AudioRecognizer();
        Map<Integer, Long> dp = audioRecognizer.createDP("song/1616589001661林俊杰 - 可惜没如果 (Live).mp3");
        for (Integer integer : dp.keySet()) {
            System.out.println(dp.get(integer));
        }
    }
}
