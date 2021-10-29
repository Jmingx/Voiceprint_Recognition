package club.jming.voiceprintrecognition.service;

import club.jming.voiceprintrecognition.utils.DataPoint;

import java.util.List;
import java.util.Map;

/**
 * redis中数据的保存形式：
 * coreMap<hash,List<DataPoint>>
 *
 */
public interface RedisService {

    /**
     * 保存音乐指纹
     * @param songID 音乐ID
     * @param songDP 音乐指纹
     */
    void setCoreMap(int songID, Map<Integer,Long> songDP);

    /**
     * 获取对应hash的list
     * @param hash
     * @return
     */
    List<DataPoint> getFromCoreMap(Long hash);
}
