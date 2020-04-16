package cn.tgozzz.legal.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
@AllArgsConstructor
public class CommonException extends RuntimeException {

    private int status = 400;
    private String message = "";

    public CommonException(String msg) {
        this.message = msg;
    }

    @SneakyThrows
    public Map<String, Object> getAttributesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        //获得属性
        Field[] fields = CommonException.class.getDeclaredFields();
        for (Field field : fields) {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), CommonException.class);
            //获得get方法
            Method getMethod = pd.getReadMethod();
            //执行get方法返回一个Object
            Object obj = getMethod.invoke(this);
            map.put(field.getName(),obj);
        }
        return map;
    }
}
