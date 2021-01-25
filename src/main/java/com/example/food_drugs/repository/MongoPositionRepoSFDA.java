package com.example.food_drugs.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.count;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.example.food_drugs.entity.DeviceTempHum;
import com.mongodb.BasicDBObject;

@Repository
public class MongoPositionRepoSFDA {

	@Autowired
	MongoTemplate mongoTemplate;
	
	public List<DeviceTempHum> getVehicleTempHumListScheduled(List<Long> allDevices,Date start,Date end){

		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(start);
		calendarFrom.add(Calendar.HOUR_OF_DAY, 3);
		start = calendarFrom.getTime();
	    
		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(end);
		calendarTo.add(Calendar.HOUR_OF_DAY, 3);
		end = calendarTo.getTime();
		
		
		List<DeviceTempHum> positions = new ArrayList<DeviceTempHum>();

						
	    Aggregation aggregation = newAggregation(
	            match(Criteria.where("deviceid").in(allDevices).and("devicetime").gte(start).lte(end)),
	            project("deviceid","attributes","speed","deviceName","weight").and("devicetime").dateAsFormattedString("%Y-%m-%dT%H:%M:%S.%LZ").as("devicetime"),
	            sort(Sort.Direction.DESC, "devicetime")
	            
	        );

	    
	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_positions", BasicDBObject.class);

	        
            if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();
	            	DeviceTempHum device = new DeviceTempHum();
	            	
	            	if(object.containsField("attributes") && object.get("attributes").toString() != null) {
	                	device.setAttributes(object.get("attributes").toString());
	
                       JSONObject obj = new JSONObject(device.getAttributes().toString());
	                	
                       Integer countTemp = 0;
	                   Integer countHum = 0;
	                   	
	                   	Double Temp = (double) 0;
	                   	Double Hum = (double) 0;
	                   	
	                   	if(obj.has("temp1")) {
	                   		Temp = Temp + obj.getDouble("temp1");
	                   		countTemp = countTemp + 1;
							}
	                   	if(obj.has("temp2")) {
	                   		Temp = Temp + obj.getDouble("temp2");
	                   		countTemp = countTemp + 1;
	
							}
	                   	if(obj.has("temp3")) {
	                   		Temp = Temp + obj.getDouble("temp3");
	                   		countTemp = countTemp + 1;
	
							}
	                   	if(obj.has("temp4")) {
	                   		Temp = Temp + obj.getDouble("temp4");
	                   		countTemp = countTemp + 1;
	
							}
	                   	
	                   	if(obj.has("hum1")) {
	                   		Hum = Hum + obj.getDouble("hum1");
	                   		countHum = countHum + 1;
							}
	                   	if(obj.has("hum2")) {
	                   		Hum = Hum + obj.getDouble("hum2");
	                   		countHum = countHum + 1;
	
							}
	                   	if(obj.has("hum3")) {
	                   		Hum = Hum + obj.getDouble("hum3");
	                   		countHum = countHum + 1;
	
							}
	                   	if(obj.has("hum4")) {
	                   		Hum = Hum + obj.getDouble("hum4");
	                   		countHum = countHum + 1;
	
							}
	                   	Double avgTemp = (double) 0;
	                   	Double avgHum = (double) 0;
	                   	if(countTemp != 0) {
		                    	avgTemp = Temp / countTemp;
	
	                   	}
	                   	if(countHum != 0) {
	                   		avgHum = Hum / countHum;
	
	                   	}
	                   	
	                   	device.setTemperature(avgTemp);
	                   	device.setHumidity(avgHum);
	            	}
	            	if(object.containsField("deviceid") && object.get("deviceid") != null) {
	                	device.setDeviceId(object.getLong("deviceid"));
	
	            	}
	            	if(object.containsField("speed") && object.get("speed") != null) {
		            	device.setSpeed(object.getDouble("speed") * (1.852) );    		
	                }
					if(object.containsField("devicetime") && object.get("devicetime") != null) {
						
						Date dateTime = null;
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");

						try {
							dateTime = inputFormat.parse(object.getString("devicetime"));

						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						

						Calendar calendarTime = Calendar.getInstance();
						calendarTime.setTime(dateTime);
						calendarTime.add(Calendar.HOUR_OF_DAY, 3);
						dateTime = calendarTime.getTime();

						
						device.setServertime(outputFormat.format(dateTime));
						
						
	                }
					
					if(object.containsField("_id") && object.get("_id") != null) {
						device.setId(object.getObjectId("_id").toString());

					}
					if(object.containsField("deviceName") && object.get("deviceName") != null) {
		            	device.setDeviceName(object.getString("deviceName"));    		
	                }
					if(object.containsField("weight") && object.get("weight") != null) {
		            	device.setWeight(object.getDouble("weight"));    		
	                }
					
	            	
	            	
					positions.add(device);
	            }
	        }
        
		return positions;
	}
	
	public List<DeviceTempHum> getVehicleTempHumList(List<Long> allDevices,int offset,Date start,Date end){

		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(start);
		calendarFrom.add(Calendar.HOUR_OF_DAY, 3);
		start = calendarFrom.getTime();
	    
		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(end);
		calendarTo.add(Calendar.HOUR_OF_DAY, 3);
		end = calendarTo.getTime();
		
		List<DeviceTempHum> positions = new ArrayList<DeviceTempHum>();

				
		
	    Aggregation aggregation = newAggregation(
	            match(Criteria.where("deviceid").in(allDevices).and("devicetime").gte(start).lte(end)),
	            project("deviceid","attributes","speed","deviceName","weight").and("devicetime").dateAsFormattedString("%Y-%m-%dT%H:%M:%S.%LZ").as("devicetime"),
	            sort(Sort.Direction.DESC, "devicetime"),
	            skip(offset),
	            limit(10)
	            
	        );

	    
	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_positions", BasicDBObject.class);

	        
            if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();
	            	DeviceTempHum device = new DeviceTempHum();
	            	
	            	
	            	
	            	if(object.containsField("attributes") && object.get("attributes") != null) {
	                	device.setAttributes(object.get("attributes").toString());
	
	                	JSONObject obj = new JSONObject(device.getAttributes().toString());
	                	
	                       Integer countTemp = 0;
		                   Integer countHum = 0;
		                   	
		                   	Double Temp = (double) 0;
		                   	Double Hum = (double) 0;
		                   	
		                   	if(obj.has("temp1")) {
		                   		Temp = Temp + obj.getDouble("temp1");
		                   		countTemp = countTemp + 1;
								}
		                   	if(obj.has("temp2")) {
		                   		Temp = Temp + obj.getDouble("temp2");
		                   		countTemp = countTemp + 1;
		
								}
		                   	if(obj.has("temp3")) {
		                   		Temp = Temp + obj.getDouble("temp3");
		                   		countTemp = countTemp + 1;
		
								}
		                   	if(obj.has("temp4")) {
		                   		Temp = Temp + obj.getDouble("temp4");
		                   		countTemp = countTemp + 1;
		
								}
		                   	
		                   	if(obj.has("hum1")) {
		                   		Hum = Hum + obj.getDouble("hum1");
		                   		countHum = countHum + 1;
								}
		                   	if(obj.has("hum2")) {
		                   		Hum = Hum + obj.getDouble("hum2");
		                   		countHum = countHum + 1;
		
								}
		                   	if(obj.has("hum3")) {
		                   		Hum = Hum + obj.getDouble("hum3");
		                   		countHum = countHum + 1;
		
								}
		                   	if(obj.has("hum4")) {
		                   		Hum = Hum + obj.getDouble("hum4");
		                   		countHum = countHum + 1;
		
								}
		                   	Double avgTemp = (double) 0;
		                   	Double avgHum = (double) 0;
		                   	if(countTemp != 0) {
			                    	avgTemp = Temp / countTemp;
		
		                   	}
		                   	if(countHum != 0) {
		                   		avgHum = Hum / countHum;
		
		                   	}
		                   	
		                   	device.setTemperature(avgTemp);
		                   	device.setHumidity(avgHum);
	            	}
	            	if(object.containsField("deviceid") && object.get("deviceid") != null) {
	                	device.setDeviceId(object.getLong("deviceid"));
	
	            	}
	            	if(object.containsField("speed") && object.get("speed") != null) {
		            	device.setSpeed(object.getDouble("speed") * (1.852) );    		
	                }
					if(object.containsField("devicetime") && object.get("devicetime") != null) {
						
						Date dateTime = null;
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");

						try {
							dateTime = inputFormat.parse(object.getString("devicetime"));

						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						

						Calendar calendarTime = Calendar.getInstance();
						calendarTime.setTime(dateTime);
						calendarTime.add(Calendar.HOUR_OF_DAY, 3);
						dateTime = calendarTime.getTime();

						
						device.setServertime(outputFormat.format(dateTime));
						
						
	                }
					if(object.containsField("_id") && object.get("_id") != null) {
						device.setId(object.getObjectId("_id").toString());

					}
					if(object.containsField("deviceName") && object.get("deviceName") != null) {
		            	device.setDeviceName(object.getString("deviceName"));    		
	                }
					if(object.containsField("weight") && object.get("weight") != null) {
		            	device.setWeight(object.getDouble("weight"));    		
	                }
	            	
					
					positions.add(device);
	            }
	        }
        
		return positions;
	}
	
	public Integer getVehicleTempHumListSize(List<Long> allDevices,Date start,Date end){

		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(start);
		calendarFrom.add(Calendar.HOUR_OF_DAY, 3);
		start = calendarFrom.getTime();
	    
		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(end);
		calendarTo.add(Calendar.HOUR_OF_DAY, 3);
		end = calendarTo.getTime();
		
		Integer size = 0;

		
		
	    Aggregation aggregation = newAggregation(
	    		match(Criteria.where("deviceid").in(allDevices).and("devicetime").gte(start).lte(end)),
	            project("deviceid","attributes","speed","deviceName","weight").and("devicetime").dateAsFormattedString("%Y-%m-%dT%H:%M:%S.%LZ").as("devicetime"),
	            sort(Sort.Direction.DESC, "devicetime"),
	            count().as("size")
	        );


	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_positions", BasicDBObject.class);


	        if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();
	            	size = object.getInt("size");
	            }

	        }
		return size;
	}
	
	
}
