package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.TemplateGroup;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateGroupRepository extends ReactiveMongoRepository<TemplateGroup, String> {
}
