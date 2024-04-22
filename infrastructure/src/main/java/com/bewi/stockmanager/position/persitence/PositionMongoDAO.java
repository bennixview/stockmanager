package com.bewi.stockmanager.position.persitence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PositionMongoDAO extends MongoRepository<PositionDoc, String> {

}
