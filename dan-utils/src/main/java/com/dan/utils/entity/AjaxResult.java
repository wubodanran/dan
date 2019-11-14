package com.dan.utils.entity;

import com.dan.utils.JsonUtil;

/**
 * Created by Bo on 2019/1/26.
 */
public class AjaxResult extends BaseSerializable {

    public static final Integer SUCCESS = 1;
    public static final Integer PARAM_ERROR = -1;
    public static final Integer SERVER_ERROR = -500;
    private Integer code;
    private String msg;
    private Object data;
    private Boolean sNulls = true;


    public AjaxResult() {
        code = SUCCESS;
    }

    public AjaxResult(Object data) {
        this(SUCCESS, null, data);
    }

    public AjaxResult(Object data, boolean serializeNulls) {
        this(SUCCESS, null, data, serializeNulls);
    }

    public AjaxResult(Integer code, String msg) {
        this(code, msg, null);
    }

    public AjaxResult(Integer code, String msg, Object data) {
        this(code, msg, data, true);
    }

    public AjaxResult(Integer code, String msg, Object data, boolean serializeNulls) {
        if (!serializeNulls) {
            if (null == data) {
                data = "";
            }
            if (null == msg) {
                msg = "";
            }
        }
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.sNulls = serializeNulls;
    }

    @Override
    public String toString() {
        return "AjaxResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", sNulls=" + sNulls +
                '}';
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public String toDataJson() {
        return this.data == null ? "" : JsonUtil.toJson(this.data);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Boolean getsNulls() {
        return sNulls;
    }
}
