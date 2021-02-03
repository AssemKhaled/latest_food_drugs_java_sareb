package com.example.food_drugs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import com.example.examplequerydslspringdatajpamaven.entity.CustomDeviceList;
import com.example.food_drugs.entity.DeviceSFDA;

/**
 * Queries on table tc_devices with SFDA
 * @author fuinco
 *
 */
@Component
public interface DeviceRepositorySFDA extends  JpaRepository<DeviceSFDA, Long>, QueryDslPredicateExecutor<DeviceSFDA> {

	@Query(nativeQuery = true, name = "getDevicesListDeactive")
	List<CustomDeviceList> getDevicesListDeactive(@Param("userIds")List<Long> userIds,@Param("offset") int offset,@Param("search") String search);

	@Query(nativeQuery = true, name = "getDevicesListDeactiveExport")
	List<CustomDeviceList> getDevicesListDeactiveExport(@Param("userIds")List<Long> userIds,@Param("search") String search);

	@Query(nativeQuery = true, name = "getDevicesListByIdsDeactive")
	List<CustomDeviceList> getDevicesListByIdsDeactive(@Param("deviceIds")List<Long> deviceIds,@Param("offset") int offset,@Param("search") String search);

	@Query(nativeQuery = true, name = "getDevicesListByIdsDeactiveExport")
	List<CustomDeviceList> getDevicesListByIdsDeactiveExport(@Param("deviceIds")List<Long> deviceIds,@Param("search") String search);

	@Query(value = " SELECT count(*) From ( "
			+ "SELECT count(*) X "
     		+ " FROM tc_devices LEFT JOIN  tc_device_driver ON tc_devices.id=tc_device_driver.deviceid"
     		+ " LEFT JOIN  tc_drivers ON tc_drivers.id=tc_device_driver.driverid and tc_drivers.delete_date is null" 
     		+ " LEFT JOIN  tc_device_geofence ON tc_devices.id=tc_device_geofence.deviceid" 
     		+ " LEFT JOIN  tc_geofences ON tc_geofences.id=tc_device_geofence.geofenceid and tc_geofences.delete_date"
     		+ " is null INNER JOIN tc_user_device ON tc_user_device.deviceid = tc_devices.id "
     		+ " LEFT JOIN tc_users ON tc_user_device.userid = tc_users.id" 
     		+ " where tc_user_device.userid IN(:userIds) and tc_devices.delete_date is not null"
     		+ " AND ( tc_devices.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.uniqueid LIKE LOWER(CONCAT('%',:search, '%')) "
     		+ " OR tc_devices.reference_key LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.sequence_number LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.lastupdate LIKE LOWER(CONCAT('%',:search, '%'))"
     		+ " OR tc_drivers.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_geofences.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_users.name LIKE LOWER(CONCAT('%',:search, '%')) ) "
     		+ " GROUP BY tc_devices.id,tc_drivers.id,tc_users.id ) Y" ,nativeQuery = true )
	public Integer getDevicesListSizeDeactive(@Param("userIds")List<Long> userIds,@Param("search") String search);
	
	
	@Query(value = " SELECT count(*) From ( "
			+ " SELECT count(*) X"
     		+ " FROM tc_devices LEFT JOIN  tc_device_driver ON tc_devices.id=tc_device_driver.deviceid"
     		+ " LEFT JOIN  tc_drivers ON tc_drivers.id=tc_device_driver.driverid and tc_drivers.delete_date is null" 
     		+ " LEFT JOIN  tc_device_geofence ON tc_devices.id=tc_device_geofence.deviceid" 
     		+ " LEFT JOIN  tc_geofences ON tc_geofences.id=tc_device_geofence.geofenceid and tc_geofences.delete_date"
     		+ " is null INNER JOIN tc_user_device ON tc_user_device.deviceid = tc_devices.id "
     		+ " LEFT JOIN tc_users ON tc_user_device.userid = tc_users.id" 
     		+ " where tc_devices.id IN(:deviceIds) and tc_devices.delete_date is not null"
     		+ " AND ( tc_devices.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.uniqueid LIKE LOWER(CONCAT('%',:search, '%')) "
     		+ " OR tc_devices.reference_key LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.sequence_number LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.lastupdate LIKE LOWER(CONCAT('%',:search, '%'))"
     		+ " OR tc_drivers.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_geofences.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_users.name LIKE LOWER(CONCAT('%',:search, '%')) ) "
     		+ " GROUP BY tc_devices.id,tc_drivers.id,tc_users.id ) Y " ,nativeQuery = true )
	public Integer getDevicesListSizeByIdsDeactive(@Param("deviceIds")List<Long> deviceIds,@Param("search") String search);
	
	@Query(nativeQuery = true, name = "getDevicesListAll")
	List<CustomDeviceList> getDevicesListAll(@Param("userIds")List<Long> userIds,@Param("offset") int offset,@Param("search") String search);

	@Query(nativeQuery = true, name = "getDevicesListAllExport")
	List<CustomDeviceList> getDevicesListAllExport(@Param("userIds")List<Long> userIds,@Param("search") String search);

	
	@Query(nativeQuery = true, name = "getDevicesListByIdsAll")
	List<CustomDeviceList> getDevicesListByIdsAll(@Param("deviceIds")List<Long> deviceIds,@Param("offset") int offset,@Param("search") String search);

	@Query(nativeQuery = true, name = "getDevicesListByIdsAllExport")
	List<CustomDeviceList> getDevicesListByIdsAllExport(@Param("deviceIds")List<Long> deviceIds,@Param("search") String search);

	
	@Query(value = " SELECT count(*) From ( "
			+ "SELECT count(*) X "
     		+ " FROM tc_devices LEFT JOIN  tc_device_driver ON tc_devices.id=tc_device_driver.deviceid"
     		+ " LEFT JOIN  tc_drivers ON tc_drivers.id=tc_device_driver.driverid and tc_drivers.delete_date is null" 
     		+ " LEFT JOIN  tc_device_geofence ON tc_devices.id=tc_device_geofence.deviceid" 
     		+ " LEFT JOIN  tc_geofences ON tc_geofences.id=tc_device_geofence.geofenceid and tc_geofences.delete_date"
     		+ " is null INNER JOIN tc_user_device ON tc_user_device.deviceid = tc_devices.id "
     		+ " LEFT JOIN tc_users ON tc_user_device.userid = tc_users.id" 
     		+ " where tc_user_device.userid IN(:userIds) "
     		+ " AND ( tc_devices.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.uniqueid LIKE LOWER(CONCAT('%',:search, '%')) "
     		+ " OR tc_devices.reference_key LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.sequence_number LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.lastupdate LIKE LOWER(CONCAT('%',:search, '%'))"
     		+ " OR tc_drivers.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_geofences.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_users.name LIKE LOWER(CONCAT('%',:search, '%')) ) "
     		+ " GROUP BY tc_devices.id,tc_drivers.id,tc_users.id ) Y" ,nativeQuery = true )
	public Integer getDevicesListSizeAll(@Param("userIds")List<Long> userIds,@Param("search") String search);
	
	
	@Query(value = " SELECT count(*) From ( "
			+ " SELECT count(*) X"
     		+ " FROM tc_devices LEFT JOIN  tc_device_driver ON tc_devices.id=tc_device_driver.deviceid"
     		+ " LEFT JOIN  tc_drivers ON tc_drivers.id=tc_device_driver.driverid and tc_drivers.delete_date is null" 
     		+ " LEFT JOIN  tc_device_geofence ON tc_devices.id=tc_device_geofence.deviceid" 
     		+ " LEFT JOIN  tc_geofences ON tc_geofences.id=tc_device_geofence.geofenceid and tc_geofences.delete_date"
     		+ " is null INNER JOIN tc_user_device ON tc_user_device.deviceid = tc_devices.id "
     		+ " LEFT JOIN tc_users ON tc_user_device.userid = tc_users.id" 
     		+ " where tc_devices.id IN(:deviceIds) "
     		+ " AND ( tc_devices.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.uniqueid LIKE LOWER(CONCAT('%',:search, '%')) "
     		+ " OR tc_devices.reference_key LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.sequence_number LIKE LOWER(CONCAT('%',:search, '%')) OR tc_devices.lastupdate LIKE LOWER(CONCAT('%',:search, '%'))"
     		+ " OR tc_drivers.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_geofences.name LIKE LOWER(CONCAT('%',:search, '%')) OR tc_users.name LIKE LOWER(CONCAT('%',:search, '%')) ) "
     		+ " GROUP BY tc_devices.id,tc_drivers.id,tc_users.id ) Y " ,nativeQuery = true )
	public Integer getDevicesListSizeByIdsAll(@Param("deviceIds")List<Long> deviceIds,@Param("search") String search);
	
}
