package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Template;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends ReactiveMongoRepository<Template, String> {
}
