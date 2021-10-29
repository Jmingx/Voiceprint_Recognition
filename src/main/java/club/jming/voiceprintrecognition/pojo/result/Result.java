package club.jming.voiceprintrecognition.pojo.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;
}