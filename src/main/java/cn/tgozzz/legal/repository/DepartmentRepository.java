package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Department;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DepartmentRepository extends ReactiveMongoRepository<Department, String> {
}
