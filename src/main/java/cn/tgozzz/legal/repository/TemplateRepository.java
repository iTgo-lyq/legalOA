package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Template;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TemplateRepository extends ReactiveMongoRepository<Template, String> {
    @Query(value="{ name: { $regex: ?0}}")
    Flux<Template> findAllLikeName(String name);
}
