package tianda.chaohen.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class MockServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method,Object[] args) throws Throwable{
        Class<?> methodReturnType = method.getReturnType();
        log.info("mock invoke {}",method.getName());
        return getDefaultObject(methodReturnType);
    }

    public Object getDefaultObject(Class<?> type){
        if(type.isPrimitive()){ //该方法是判断一个类是不是原始类型
            if(type==boolean.class)
                return false;
            else if(type==short.class)
                return (short)0;
            else if(type==int.class)
                return (int)0;
            else if(type==long.class)
                return 0L;
        }
        return null;
    }


}
