package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
}
