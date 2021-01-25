package com.example.food_drugs.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.food_drugs.entity.MonogoInventoryLastData;;

public interface MongoInventoryLastDataRepository extends MongoRepository<MonogoInventoryLastData, String>{

	@Query("{ '_id' : ?0 }")
	public MonogoInventoryLastData findById(String lastId);
}
