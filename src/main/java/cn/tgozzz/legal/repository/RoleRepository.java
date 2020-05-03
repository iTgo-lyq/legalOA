package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Role;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RoleRepository extends ReactiveMongoRepository<Role, String> {
}
