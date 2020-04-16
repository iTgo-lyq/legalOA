package cn.tgozzz.legal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public static void main(String[] args) {
        Function<String,String> fn1 = e -> {
            System.out.println("1");
            return "2";
        };
        Function<String,String>  fn2 = e -> {
            System.out.println("2");
            return "3";
        };
        fn1.andThen(fn2).apply("1");
    }
}