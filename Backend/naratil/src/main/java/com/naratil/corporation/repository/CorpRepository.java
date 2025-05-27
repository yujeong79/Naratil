package com.naratil.corporation.repository;


import com.naratil.corporation.entity.Corporation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorpRepository extends JpaRepository<Corporation, Long> {

    Corporation save(Corporation corporation);
    Optional<Corporation> findByBizno(String bizno);

}
