package com.dan.util.constant;

import com.dan.util.enums.CommonStatusEnum;

/**
 * @version 1.0
 * @author: Bo
 * @fileName: Result
 * @createDate: 2020-04-03 09:44.
 * @description: 通用的处理返回结果
 */
public abstract class AbstractResult<T> implements BaseResult {

    /**
     * 成功code
     */
    protected static final int SUCCESS_CODE = CommonStatusEnum.SUCCESS.value;
    /**
     * 失败code
     */
    protected static final int FAIL_CODE = CommonStatusEnum.FAIL.value;
    private static final long serialVersionUID = -3766826473104683293L;

    abstract Integer getCode();

    abstract String getMessage();

    abstract T getData();

    boolean isSuccess() {
        Integer code = getCode();
        return code != null && SUCCESS_CODE == code;
    }

}
