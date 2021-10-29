package club.jming.voiceprintrecognition;

import club.jming.voiceprintrecognition.utils.AudioRecognizer;
import club.jming.voiceprintrecognition.utils.DataPoint;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Demo {

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        AudioRecognizer audioRecognizer = new AudioRecognizer();
        List<Map<Integer,Long>> list = new LinkedList<>();

        Long startTime = System.currentTimeMillis();
        list.add(audioRecognizer.createDP("song/1616589001661林俊杰 - 可惜没如果 (Live).mp3"));
        list.add(audioRecognizer.createDP("song/1616589116969G_E_M_ 邓紫棋 - 差不多姑娘.mp3"));
        list.add(audioRecognizer.createDP("song/1616589187845G_E_M_ 邓紫棋 - 倒数.mp3"));
        list.add(audioRecognizer.createDP("song/1616589238275G_E_M_ 邓紫棋 - 再见.mp3"));
        Long endTime = System.currentTimeMillis();

        System.out.println("加载歌曲用时："+(endTime-startTime));

        Map<Long,List<DataPoint>> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            int songID = i;
            Map<Integer, Long> integerLongMap = list.get(songID);
            for (int time = 0; time < integerLongMap.keySet().size(); time++) {
                DataPoint dataPoint = new DataPoint();
                dataPoint.setSongID(songID);
                dataPoint.setTime(time);
                if (!map.containsKey(integerLongMap.get(time))){
                    map.put(integerLongMap.get(time),new LinkedList<>());
                }
                List<DataPoint> dataPoints = map.get(integerLongMap.get(time));
                dataPoints.add(dataPoint);
            }
        }

        Map<Integer, Long> target = audioRecognizer.createDP("song/1616589001661林俊杰 - 可惜没如果 (Live).mp3");

        List<Double> result = new LinkedList<>();

        Map<Integer,Map<Integer,Integer>> complexResult = new HashMap<>();

        startTime = System.currentTimeMillis();

        for (int targetIndex = 0; targetIndex < target.keySet().size(); targetIndex++) {
            if (!map.containsKey(target.get(targetIndex))){
                continue;
            }
            List<DataPoint> dataPoints = map.get(target.get(targetIndex));
            for (DataPoint dataPoint : dataPoints) {
                if (!complexResult.containsKey(dataPoint.getSongID())){
                    complexResult.put(dataPoint.getSongID(),new HashMap<>());
                }
                Map<Integer, Integer> map1 = complexResult.get(dataPoint.getSongID());
                int distance = Math.abs(dataPoint.getTime() - targetIndex);
                map1.put(distance,map1.getOrDefault(distance,0) + 1);
            }
        }

        for (int i = 0; i < map.size(); i++) {
            Map<Integer, Integer> map1 = complexResult.get(i);
            if (map1 == null){
                continue;
            }
            int maxOccurrences = 0;
            for (Integer integer : map1.keySet()) {
                maxOccurrences = Math.max(maxOccurrences,map1.get(integer));
            }
            System.out.println("song "+i+" maxOccurrences is : "+maxOccurrences);
            result.add(maxOccurrences/target.size()*100.0d);
        }

        endTime = System.currentTimeMillis();

        for (Double aDouble : result) {
            System.out.println(aDouble+"%");
        }

        System.out.println("匹配用时："+(endTime-startTime));

    }
}
