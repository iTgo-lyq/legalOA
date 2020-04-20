package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Word;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends ReactiveMongoRepository<Word, String> {

}
