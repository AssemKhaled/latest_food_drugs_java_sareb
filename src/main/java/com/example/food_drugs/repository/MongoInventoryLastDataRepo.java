package com.example.food_drugs.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.count;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import com.example.food_drugs.entity.InventoryLastData;
import com.example.food_drugs.entity.MonogoInventoryLastData;
import com.mongodb.BasicDBObject;

@Repository
public class MongoInventoryLastDataRepo {

	
	

	@Autowired
	MongoTemplate mongoTemplate;
	
	public Integer getLastDataSize(List<Long> allInventories){

		Integer size = 0;
		
	    Aggregation aggregation = newAggregation(
	            match(Criteria.where("inventory_id").in(allInventories)),
	            project("temperature","humidity","inventory_id").and("create_date").dateAsFormattedString("%Y-%m-%d %H:%M:%S.%LZ").as("create_date"),
	            sort(Sort.Direction.ASC, "create_date"),
	            group("inventory_id").last("$$ROOT").as("test"),
	            replaceRoot("test"),
	            count().as("size")
	            
	        );


	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_inventory_last_data", BasicDBObject.class);
	        

	        if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();
	            	size = object.getInt("size");
	            }

	        }
	        
		return size;
	}
	public Integer getInventoriesReportSize(List<Long> allInventories,Date start,Date end){

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

	            match(Criteria.where("inventory_id").in(allInventories).and("create_date").gte(start).lte(end)),
	            project("temperature","humidity","inventory_id").and("create_date").dateAsFormattedString("%Y-%m-%d %H:%M:%S.%LZ").as("create_date"),
	            sort(Sort.Direction.DESC, "create_date"),
	            count().as("size")

	            
	        );


	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_inventory_last_data", BasicDBObject.class);
	        
	        if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();
	            	size = object.getInt("size");
	            }

	        }
	        
		return size;
	}
	
   public List<InventoryLastData> getLastData(List<Long> allInventories,int offset){
		
		

		
		List<InventoryLastData> inventories = new ArrayList<InventoryLastData>();

		
	    Aggregation aggregation = newAggregation(
	    		

	            match(Criteria.where("inventory_id").in(allInventories)),
	            project("temperature","humidity","inventory_id").and("create_date").dateAsFormattedString("%Y-%m-%d %H:%M:%S.%LZ").as("create_date"),
	            sort(Sort.Direction.ASC, "create_date"),
	            group("inventory_id").last("$$ROOT").as("test"),
	            replaceRoot("test"),
	            skip(offset),
	            limit(10)
	            
	        );

	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_inventory_last_data", BasicDBObject.class);


	        if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();

	            	InventoryLastData inventory = new InventoryLastData();
                    if(object.containsField("inventory_id") && object.get("inventory_id") !=null) {
                    	
                    	inventory.setInventory_id(object.getLong("inventory_id"));
	            	}
                    if(object.containsField("temperature") && object.get("temperature") !=null) {
                    	
                    	inventory.setTemperature(object.getDouble("temperature"));

	            	}
	            	if(object.containsField("humidity") && object.get("humidity") !=null) {
	            		inventory.setHumidity(object.getDouble("humidity"));
	
	            	}
					if(object.containsField("create_date") && object.get("create_date") !=null) {
						
						Date dateTime = null;
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");

						try {
							dateTime = inputFormat.parse(object.getString("create_date"));

						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						

						Calendar calendarTime = Calendar.getInstance();
						calendarTime.setTime(dateTime);
						calendarTime.add(Calendar.HOUR_OF_DAY, 3);
						dateTime = calendarTime.getTime();
						
						inventory.setCreate_date(outputFormat.format(dateTime));
						
	                }
					if(object.containsField("_id") && object.get("_id") !=null) {
		            	
						inventory.set_id(object.getObjectId("_id").toString());

					}
					inventories.add(inventory);
                     
	            }
	        }
	        
		return inventories;
	}

   public List<InventoryLastData> getInventoriesReport(List<Long> allInventories,int offset,Date start,Date end){
		
		
		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(start);
		calendarFrom.add(Calendar.HOUR_OF_DAY, 3);
		start = calendarFrom.getTime();
	    
		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(end);
		calendarTo.add(Calendar.HOUR_OF_DAY, 3);
		end = calendarTo.getTime();
		
		List<InventoryLastData> inventories = new ArrayList<InventoryLastData>();
		
	    Aggregation aggregation = newAggregation(
	    		

	            match(Criteria.where("inventory_id").in(allInventories).and("create_date").gte(start).lte(end)),
	            project("temperature","humidity","inventory_id").and("create_date").dateAsFormattedString("%Y-%m-%d %H:%M:%S.%LZ").as("create_date"),
	            sort(Sort.Direction.DESC, "create_date"),
	            skip(offset),
	            limit(10)
	            
	        );

	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_inventory_last_data", BasicDBObject.class);



	        if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();

	            	InventoryLastData inventory = new InventoryLastData();
                    if(object.containsField("inventory_id") && object.get("inventory_id") !=null) {
                    	
                    	inventory.setInventory_id(object.getLong("inventory_id"));
	            	}
                    if(object.containsField("temperature") && object.get("temperature") !=null) {
                    	
                    	inventory.setTemperature(object.getDouble("temperature"));

	            	}
	            	if(object.containsField("humidity") && object.get("humidity") !=null) {
	            		inventory.setHumidity(object.getDouble("humidity"));
	
	            	}

					if(object.containsField("create_date") && object.get("create_date") !=null) {
						Date dateTime = null;
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");

						try {
							dateTime = inputFormat.parse(object.getString("create_date"));

						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						

						Calendar calendarTime = Calendar.getInstance();
						calendarTime.setTime(dateTime);
						calendarTime.add(Calendar.HOUR_OF_DAY, 3);
						dateTime = calendarTime.getTime();
						
						inventory.setCreate_date(outputFormat.format(dateTime));
	                }
					if(object.containsField("_id") && object.get("_id") !=null) {
		            	
						inventory.set_id(object.getObjectId("_id").toString());

					}
					inventories.add(inventory);
                     
	            }
	        }
	        
		return inventories;
	}
   public List<InventoryLastData> getInventoriesReportSchedule(List<Long> allInventories,Date start,Date end){
		
		
		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(start);
		calendarFrom.add(Calendar.HOUR_OF_DAY, 3);
		start = calendarFrom.getTime();
	    
		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(end);
		calendarTo.add(Calendar.HOUR_OF_DAY, 3);
		end = calendarTo.getTime();
		
		List<InventoryLastData> inventories = new ArrayList<InventoryLastData>();
		
	    Aggregation aggregation = newAggregation(
	    		

	            match(Criteria.where("inventory_id").in(allInventories).and("create_date").gte(start).lte(end)),
	            project("temperature","humidity","inventory_id").and("create_date").dateAsFormattedString("%Y-%m-%d %H:%M:%S.%LZ").as("create_date"),
	            sort(Sort.Direction.DESC, "create_date")
	            
	        );

	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_inventory_last_data", BasicDBObject.class);



	        if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();

	            	InventoryLastData inventory = new InventoryLastData();
                   if(object.containsField("inventory_id") && object.get("inventory_id") !=null) {
                   	
                   	inventory.setInventory_id(object.getLong("inventory_id"));
	            	}
                   if(object.containsField("temperature") && object.get("temperature") !=null) {
                   	
                   	inventory.setTemperature(object.getDouble("temperature"));

	            	}
	            	if(object.containsField("humidity") && object.get("humidity") !=null) {
	            		inventory.setHumidity(object.getDouble("humidity"));
	
	            	}

					if(object.containsField("create_date") && object.get("create_date") !=null) {

						Date dateTime = null;
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");

						try {
							dateTime = inputFormat.parse(object.getString("create_date"));

						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						

						Calendar calendarTime = Calendar.getInstance();
						calendarTime.setTime(dateTime);
						calendarTime.add(Calendar.HOUR_OF_DAY, 3);
						dateTime = calendarTime.getTime();
						
						inventory.setCreate_date(outputFormat.format(dateTime));
	                }
					if(object.containsField("_id") && object.get("_id") !=null) {
		            	
						inventory.set_id(object.getObjectId("_id").toString());

					}
					inventories.add(inventory);
                    
	            }
	        }
	        
		return inventories;
	}
   public InventoryLastData getLastDataToSendELm(Long inventoryId){
		
		

     	InventoryLastData inventory = new InventoryLastData();
		
	    Aggregation aggregation = newAggregation(
	    		

	            match(Criteria.where("inventory_id").in(inventoryId)),
	            project("temperature","humidity","create_date","inventory_id").and("create_date").dateAsFormattedString("%Y-%m-%d %H:%M:%S.%LZ").as("create_date"),
	            sort(Sort.Direction.ASC, "create_date"),
	            skip(0),
	            limit(1)
	            
	        );

	        AggregationResults<BasicDBObject> groupResults
	            = mongoTemplate.aggregate(aggregation,"tc_inventory_last_data_live_elm", BasicDBObject.class);



	        if(groupResults.getMappedResults().size() > 0) {
	        	
	            Iterator<BasicDBObject> iterator = groupResults.getMappedResults().iterator();
	            while (iterator.hasNext()) {
	            	BasicDBObject object = (BasicDBObject) iterator.next();

                    if(object.containsField("inventory_id") && object.get("inventory_id") !=null) {
                    	
                    	inventory.setInventory_id(object.getLong("inventory_id"));
	            	}
                    if(object.containsField("temperature") && object.get("temperature") !=null) {
                    	
                    	inventory.setTemperature(object.getDouble("temperature"));

	            	}
	            	if(object.containsField("humidity") && object.get("humidity") !=null) {
	            		inventory.setHumidity(object.getDouble("humidity"));
	
	            	}
					if(object.containsField("create_date") && object.get("create_date") !=null) {
						
						Date dateTime = null;
						SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");

						try {
							dateTime = inputFormat.parse(object.getString("create_date"));

						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						

						Calendar calendarTime = Calendar.getInstance();
						calendarTime.setTime(dateTime);
						calendarTime.add(Calendar.HOUR_OF_DAY, 3);
						dateTime = calendarTime.getTime();
						
						inventory.setCreate_date(outputFormat.format(dateTime));    		
	                }
					if(object.containsField("_id") && object.get("_id") !=null) {
		            	
						inventory.set_id(object.getObjectId("_id").toString());

					}
                     
	            }
	        }
	        
		return inventory;
	}
}
