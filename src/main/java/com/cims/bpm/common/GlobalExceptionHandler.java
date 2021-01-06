package com.cims.bpm.common;

import com.dragon.tools.common.ReturnCode;
import com.dragon.tools.vo.ReturnVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * 处理自定义的业务异常
	 * @param req
	 * @param e
	 * @return
	 */
    @ExceptionHandler(value = ExtException.class)
    @ResponseBody
	public ReturnVo extExceptionHandler(HttpServletRequest req, ExtException e){
    	log.error("发生业务异常！原因是：{}",e.getErrorMsg());
		ReturnVo returnVo = new ReturnVo(e.getErrorCode(), e.getErrorMsg());
    	return returnVo;
    }


    /**
        * 处理其他异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =Exception.class)
	@ResponseBody
	public ReturnVo exceptionHandler(HttpServletRequest req, Exception e){
    	log.error("未知异常！原因是:",e);
		ReturnVo returnVo = new ReturnVo(ReturnCode.FAIL, e.getMessage());
		return returnVo;
    }
}