package club.jming.voiceprintrecognition.service.impl;

import club.jming.voiceprintrecognition.dao.SongMapper;
import club.jming.voiceprintrecognition.pojo.Song;
import club.jming.voiceprintrecognition.service.RedisService;
import club.jming.voiceprintrecognition.service.SongService;
import club.jming.voiceprintrecognition.utils.AudioRecognizer;
import club.jming.voiceprintrecognition.utils.Const;
import club.jming.voiceprintrecognition.utils.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SongServiceImpl implements SongService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public int insertSong(Song song) {
        return songMapper.insert(song);
    }

    @Override
    public Integer getID(String songName) {
        return songMapper.getID(songName);
    }

    @Override
    public void createSongPrint(Song song) {
        AudioRecognizer audioRecognizer = new AudioRecognizer();
        Map<Integer, Long> dp = null;
        try {
            dp = audioRecognizer.createDP(song.getPath());
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        redisService.setCoreMap(song.getId(),dp);
    }

    @Override
    public Map<Integer, Float> match(String filePath) {
        AudioRecognizer audioRecognizer = new AudioRecognizer();
        Map<Integer, Long> dp = null;
        try {
            dp = audioRecognizer.createDP(filePath);
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        Map<Integer,Map<Integer,Integer>> compareResult = new HashMap<>();
        for (Integer integer : dp.keySet()) {
            int timeIndex = integer;
            long hash = dp.get(integer);
            List<DataPoint> dataPoints = redisService.getFromCoreMap(hash);
            if (dataPoints == null){
                continue;
            }
            for (DataPoint dataPoint : dataPoints) {
                int songID = dataPoint.getSongID();
                int absTimeIndex = dataPoint.getTime();
                if (!compareResult.containsKey(songID)){
                    compareResult.put(songID,new HashMap<>());
                }
                Map<Integer, Integer> map = compareResult.get(songID);
                int relativeDistance = Math.abs(absTimeIndex - timeIndex);
                map.put(relativeDistance,map.getOrDefault(relativeDistance,0)+1);
            }
        }

        for (Integer songID : compareResult.keySet()) {
            log.info("for song [{}] match msg as followed:",songID);
            log.info("{}",compareResult.get(songID).toString());
        }

        Map<Integer,Float> result = new HashMap<>();

        for (Integer songID : compareResult.keySet()) {
            Map<Integer, Integer> map = compareResult.get(songID);
            int maxMatchTimes = 0;
            //提供一定的模糊识别，将识别窗口提供一点误差接受范围
            int maxMatchTimeIndex = 0;
            for (Integer integer : map.keySet()) {
                if (maxMatchTimes < map.get(integer)){
                    maxMatchTimes = map.get(integer);
                    maxMatchTimeIndex = integer;
                }
            }
            int totalMatch = 0;

            int vague = Math.min(Const.VAGUE, dp.size());

            for (int i = maxMatchTimeIndex - vague; i <= maxMatchTimeIndex + vague; i++) {
                totalMatch += map.getOrDefault(i,0);
            }

            log.info("{}",totalMatch);

            result.put(songID,Math.min((totalMatch*1.0f/dp.size()),1.0f));
        }
        return result;
    }

    @Override
    public Song getSongByID(int songID) {
        return songMapper.getSongByID(songID);
    }

    @Override
    public List<Song> getAllSongs() {
        return songMapper.getAllSongs();
    }
}
