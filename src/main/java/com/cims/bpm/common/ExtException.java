package com.cims.bpm.common;

import com.dragon.tools.common.ReturnCode;
import lombok.Data;
import org.springframework.core.NestedRuntimeException;

@Data
public class ExtException extends NestedRuntimeException {
    public String errorCode;
    public String errorMsg;



    public ExtException(String msg) {
        super(msg);
        this.errorCode = ReturnCode.FAIL;
        this.errorMsg = msg;
    }

    public ExtException(String msg ,Throwable throwable){
        super(msg,throwable);
    }


    public ExtException(String errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
        this.errorMsg=msg;

    }


    public final static String formatMsg(String errorCode) {
        return SpringContextUtil.getMessage(String.valueOf(errorCode));
    }

}