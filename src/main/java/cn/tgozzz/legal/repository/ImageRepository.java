package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Image;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends ReactiveMongoRepository<Image, String> {

}
