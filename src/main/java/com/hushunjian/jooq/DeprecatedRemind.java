package com.hushunjian.jooq;


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DeprecatedRemind {



    @Pointcut("@within(java.lang.Deprecated)")
    public void deprecated() {

    }

    @Before("deprecated()")
    public void deprecatedRemind() {
        //System.out.println("ddddd");
        //dingMessageHandle.sendDing(String.format("调用过时接口,上下文参数:[%s]", JSONObject.toJSONString(SystemContextHolder.getContextMap())));
    }
}
