package cn.tgozzz.legal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.function.Function;
import java.util.function.Predicate;

@RestController()
@RequestMapping("/users")
public class Test {

    public Mono<String> test() {
        return null;
    }

    @GetMapping("/1")
    public Mono<String> getAll() {
        return Mono.just("1");
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = "鍝堝搱";
        System.out.println(System.getProperty("file.encoding"));
       System.out.println( new String(s.getBytes("GBK"), "UTF-8"));
    }
}