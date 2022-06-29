package com.example.examplequerydslspringdatajpamaven.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.example.food_drugs.entity.MonogoInventoryLastData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.example.examplequerydslspringdatajpamaven.entity.MongoPositions;

/**
 * Built-in queries related to Mongo collection
 * @author fuinco
 *
 */
public interface MongoPositionsRepository extends MongoRepository<MongoPositions,String>{

	@Query(value="{ '_id' : { $in: ?0 } }", delete = true)
	public List<MongoPositions> deleteByIdIn(List<String> positionIds);
	
	public Integer countByDeviceidIn(List<Long> deviceIds);
	Optional<List<MongoPositions>> findAllBy_idIn(List<String> ids);
	
	@Query("{ '_id' : { $in: ?0 } }")
	public List<MongoPositions> findByIdIn(List<String> positionIds);
	
	@Query("{ '_id' : ?0 }")
	public MongoPositions findById(String positionId);
	
	
	@Query("{ 'deviceid' : { $in: ?0 } , 'deviceName' : { $exists : false }}")
	public List<MongoPositions> findByDeviceIdIn(List<Long> deviceIds,Pageable pageable);

//	List <MongoPositions> findFirst20ByDeviceidOrderByCreateDateDesc(int deviceId);

//	List <MongoPositions> findFirstBy20ByDeviceidOrderByServertimeDesc(Long deviceId);


	List <MongoPositions> findFirst10ByDeviceidOrderByServertimeDesc( int deviceId);

	List <MongoPositions> findFirst15ByDeviceidOrderByServertimeDesc( int deviceId);

	List <MongoPositions> findFirst20ByDeviceidOrderByServertimeDesc( int deviceId);

	List<MongoPositions> findAllByDeviceidAndDevicetimeBetweenOrderByDevicetimeDesc(Long deviceId, Date start, Date end);

	List<MongoPositions> findAllByDeviceidAndDevicetimeBetweenOrderByDevicetimeDesc(Long deviceId, Date start, Date end, Pageable pageable);

	Integer countAllByDeviceidAndDevicetimeBetween(Long deviceId, Date start, Date end);
	Optional<MongoPositions> findAllByDeviceid(Long deviceId);
	List<MongoPositions> findTop10ByDeviceidAndSpeedOrderByServertimeDesc(Long deviceId , double speed);

	List<MongoPositions> findTop10ByDeviceidAndSpeedAfterOrderByServertimeDesc(Long deviceId , double speed);
}

