package club.jming.voiceprintrecognition.utils;

import lombok.Data;

/**
 * 数据点，存储了时间（片段index）和songID信息
 */
@Data
public class DataPoint {
    private int time;
    private int songID;
}
