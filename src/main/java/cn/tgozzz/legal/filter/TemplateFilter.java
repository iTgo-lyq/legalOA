package cn.tgozzz.legal.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class TemplateFilter {

    public boolean updateTemp(ServerRequest request) {
        return false;
    }
}
