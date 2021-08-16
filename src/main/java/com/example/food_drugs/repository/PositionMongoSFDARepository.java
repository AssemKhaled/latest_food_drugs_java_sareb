package com.example.food_drugs.repository;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.food_drugs.entity.Position;

@Repository
public interface PositionMongoSFDARepository extends MongoRepository<Position, String> {
	
//	@Override
	public Position findBy_id(ObjectId _id);
	
	public List<Position> findAllByDeviceid(long i);
	

	public List<Position> findAllByDevicetimeBetweenAndDeviceid(Date from,Date to,long i);
	
	
	
//	public static List<Position> getPositions(){
//		Query query = new Query();
//		query.addCriteria(Criteria.where("name").is("Eric"));
//		List<Position> positions = mongoTemplate.find(query, Position.class);
//	}
//	
}
