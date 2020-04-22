package cn.tgozzz.legal.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Log4j2
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    public GlobalErrorAttributes() {
        super(false);
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        // 使用super必要时可获取堆栈信息
        Map<String, Object> errorAttributes = super.getErrorAttributes(
                request, includeStackTrace);
        //获取error对象
        Throwable error = getError(request);

        if (error instanceof CommonException) {
            // 自定义错误属性追加
            errorAttributes.putAll(((CommonException) error).getAttributesMap());
        } else {
            if(!(error instanceof ResponseStatusException && ((ResponseStatusException) error).getStatus().value() == 404)){
                // 不是自定义错误也不是404，打印堆栈
                log.error(super.getErrorAttributes(request, true));

                errorAttributes.put("code", -1);
                errorAttributes.put("tip", "猜猜你错了还是我错了 🐷^(*￣(oo)￣)^");
            }
        }
        return errorAttributes;
    }
}
