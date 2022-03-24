package com.siamese.bri.handler;
import org.aspectj.lang.ProceedingJoinPoint;

public interface BadRequestHandler {
    /**
     * 在目标方法执行之前要执行的方法
     * 入参为 切点方法
     */
    Object handleBefore(ProceedingJoinPoint point) throws IllegalAccessException, NoSuchMethodException, ClassNotFoundException;

    /**
     * 在目标方法执行之后要执行的方法
     *  入参为 切点方法
     */
    Object handleAfter(ProceedingJoinPoint point);


    /**
     * 记录一次无效请求
     * 入参为 切点方法
     */
    Object record(ProceedingJoinPoint point) throws IllegalAccessException;

    /**
     * 清空无效请求错误次数记录
     */
    Object flush();
}
