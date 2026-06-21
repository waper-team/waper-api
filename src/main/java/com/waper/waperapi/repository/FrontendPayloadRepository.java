package com.waper.waperapi.repository;

import com.waper.waperapi.model.FrontendPayload;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FrontendPayloadRepository extends MongoRepository<FrontendPayload, String> {
}
