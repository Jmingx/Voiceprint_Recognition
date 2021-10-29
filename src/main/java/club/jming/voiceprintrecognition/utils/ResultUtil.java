package club.jming.voiceprintrecognition.utils;

import club.jming.voiceprintrecognition.pojo.result.Result;
import club.jming.voiceprintrecognition.pojo.result.ResultEnum;

/**
 * 结果工具类
 */
public class ResultUtil {

    /**
     * 成功并返回数据
     * @param object
     * @return
     */
    public static Result success(Object object){
        Result result = new Result();
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMsg(ResultEnum.SUCCESS.getMsg());
        result.setData(object);
        return result;
    }

    /**
     * 成功不返回数据
     * @return
     */
    public static Result success(){
        return success(null);
    }

    /**
     * 错误不返回数据
     * @param code
     * @param msg
     * @return
     */
    public static Result error(int code,String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
