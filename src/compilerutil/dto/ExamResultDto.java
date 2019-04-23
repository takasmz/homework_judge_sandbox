package compilerutil.dto;

import java.lang.reflect.Method;

/**
 * @Author 李如豪
 * @Date 2019/2/1 10:33
 * @VERSION 1.0
 **/
public class ExamResultDto extends ExamResult{
    private Method method;
    private Object instance;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

}
