package cn.tgozzz.legal.repository;

import cn.tgozzz.legal.domain.Contract;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ContractRepository extends ReactiveMongoRepository<Contract, String> {

     default Mono<Contract> UpdateStatusByCid(String lastCid, int status) {
         return findById(lastCid)
                 .doOnNext(contract -> contract.setStatus(status))
                 .flatMap(this::save);
     };
}
