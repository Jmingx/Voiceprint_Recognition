package club.jming.voiceprintrecognition.pojo.result;

import lombok.Data;

public enum ResultEnum {
    UNKNOW_ERROR(-1,"未知错误"),
    SUCCESS(10000,"成功"),
    ;

    private int code;
    private String msg;

    ResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode(){
        return code;
    }

    public String getMsg(){
        return msg;
    }
}
