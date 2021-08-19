package com.example.food_drugs.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.*;

import com.example.food_drugs.entity.MonogoInventoryLastData;;
public interface MongoInventoryLastDataRepository extends MongoRepository<MonogoInventoryLastData, String>{

	@Query("{ '_id' : ?0 }")
	MonogoInventoryLastData findById(String lastId);
//	List<MonogoInventoryLastData> findAllByInventory(Long i);
}
