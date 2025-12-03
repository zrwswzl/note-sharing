package com.project.login.repository;

import com.project.login.model.dataobject.RemarkCompensationDO;
import com.project.login.model.dataobject.RemarkDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RemarkCompensationRepository extends MongoRepository<RemarkCompensationDO, String> {

}
