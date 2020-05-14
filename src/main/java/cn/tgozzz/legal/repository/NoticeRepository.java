package cn.tgozzz.legal.repository;


import cn.tgozzz.legal.domain.Notice;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface NoticeRepository extends ReactiveMongoRepository<Notice, String> {
}
