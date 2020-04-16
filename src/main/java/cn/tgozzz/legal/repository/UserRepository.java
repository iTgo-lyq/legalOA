package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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

}

