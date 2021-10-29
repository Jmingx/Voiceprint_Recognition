package club.jming.voiceprintrecognition.service.impl;

import club.jming.voiceprintrecognition.service.RedisService;
import club.jming.voiceprintrecognition.utils.Const;
import club.jming.voiceprintrecognition.utils.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public void setCoreMap(int songID, Map<Integer, Long> songDP) {
        for (Integer integer : songDP.keySet()) {
            int timeIndex = integer;
            String  hash = String.valueOf(songDP.get(timeIndex));
            DataPoint dataPoint = new DataPoint();
            dataPoint.setTime(timeIndex);
            dataPoint.setSongID(songID);

            List<DataPoint> list = (List<DataPoint>) redisTemplate.opsForHash().get(Const.COREMAP, hash);
            if (list == null){
//                log.info("can't not find songID[{}] matched dataPoint, creating new list to coreMap...dataPoint: {}",songID,dataPoint);
                redisTemplate.opsForHash().put(Const.COREMAP,hash,new LinkedList<DataPoint>());
                list = (List<DataPoint>) redisTemplate.opsForHash().get(Const.COREMAP, hash);
            }
            list.add(dataPoint);
            redisTemplate.opsForHash().put(Const.COREMAP,hash,list);
        }
    }

    @Override
    public List<DataPoint> getFromCoreMap(Long hash) {
        return (List<DataPoint>) redisTemplate.opsForHash().get(Const.COREMAP,String.valueOf(hash));
    }
}
