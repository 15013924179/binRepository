package com.bin.meishikecan.repo;

import com.bin.meishikecan.entity.TravelDocumentMongodb;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TravelDocumentMongodbRepo extends MongoRepository<TravelDocumentMongodb,String> {

}
