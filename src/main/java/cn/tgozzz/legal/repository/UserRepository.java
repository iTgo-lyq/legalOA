package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    /**
     * 根据手机号查找用户
     */
    Mono<User> findOneByPhone(String phone);

    /**
     * 根据token查找用户,并且重设token
     */
    Mono<User> findAndUpdateByToken(String oldT, String newT);

    /**
     * 更具名字查询用户
     */
    Mono<User> findOneByName(String system);

    /**
     * 查询所有在部门列表中的用户
     */
    @Query(value = "{\"organization.department.did\":{$in:?0}}")
    Flux<User> findAllInProjects(ArrayList<String> depts);
}

