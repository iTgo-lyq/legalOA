package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Template;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TemplateRepository extends ReactiveMongoRepository<Template, String> {
    @Query(value = "{ name: { $regex: ?0}}")
    Flux<Template> findAllLikeName(String name);

    default Mono<Template> findByIdAndUpdateGroup(String tid, String group) {
        return findById(tid)
                .doOnNext(template -> template.setGroup(group))
                .flatMap(this::save);
    }
}
