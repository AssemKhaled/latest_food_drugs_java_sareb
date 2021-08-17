package com.example.food_drugs.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

import com.example.examplequerydslspringdatajpamaven.entity.MongoEvents;
import com.example.examplequerydslspringdatajpamaven.repository.MongoEventsRepository;
import com.example.examplequerydslspringdatajpamaven.responses.AlarmSectionWrapperResponse;
import com.example.food_drugs.repository.*;
import com.example.food_drugs.responses.AlarmsReportResponseWrapper;
import com.example.food_drugs.responses.DeviceAttributes;
import com.example.food_drugs.responses.GraphDataWrapper;
import com.example.food_drugs.responses.GraphObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.examplequerydslspringdatajpamaven.entity.Device;
import com.example.examplequerydslspringdatajpamaven.entity.Group;
import com.example.examplequerydslspringdatajpamaven.entity.User;
import com.example.examplequerydslspringdatajpamaven.repository.GroupRepository;
import com.example.examplequerydslspringdatajpamaven.repository.UserClientDeviceRepository;
import com.example.examplequerydslspringdatajpamaven.repository.UserClientGroupRepository;
import com.example.examplequerydslspringdatajpamaven.responses.GetObjectResponse;
import com.example.examplequerydslspringdatajpamaven.rest.RestServiceController;
import com.example.examplequerydslspringdatajpamaven.service.DeviceServiceImpl;
import com.example.examplequerydslspringdatajpamaven.service.GroupsServiceImpl;
import com.example.examplequerydslspringdatajpamaven.service.UserRoleService;
import com.example.examplequerydslspringdatajpamaven.service.UserServiceImpl;

import com.example.food_drugs.entity.DeviceTempHum;
import com.example.food_drugs.entity.Inventory;
import com.example.food_drugs.entity.InventoryLastData;
import com.example.food_drugs.entity.InventoryNotification;
import com.example.food_drugs.entity.MonitorStaticstics;
import com.example.food_drugs.entity.PdfSummaryData;
import com.example.food_drugs.entity.Position;
import com.example.food_drugs.entity.ReportDetails;
import com.example.food_drugs.entity.Series;
import com.example.food_drugs.entity.TripDetailsRequest;
import com.example.food_drugs.entity.Warehouse;
import com.example.food_drugs.repository.InventoryRepository;
import com.example.food_drugs.repository.MongoInventoryLastDataRepo;
import com.example.food_drugs.repository.MongoInventoryNotificationRepo;
import com.example.food_drugs.repository.MongoPositionRepoSFDA;
import com.example.food_drugs.repository.PositionMongoSFDARepository;
import com.example.food_drugs.repository.WarehousesRepository;



/**
 * services functionality related to reports SFDA
 * @author fuinco
 *
 */
@Component
@Service
public class ReportServiceImplSFDA extends RestServiceController implements ReportServiceSFDA{

    private static final Log logger = LogFactory.getLog(DeviceServiceImplSFDA.class);
	
	private GetObjectResponse getObjectResponse;
	
	@Autowired
	private UserClientDeviceRepository userClientDeviceRepository;
	
	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Autowired
	private WarehousesRepository warehousesRepository;

	@Autowired
	private UserRoleService userRoleService;
	
	@Autowired
	private GroupRepository groupRepository;
	
	@Autowired
	private UserServiceImpl userServiceImpl;

	@Autowired
	private UserClientGroupRepository userClientGroupRepository;	
	
	@Autowired
	private MongoInventoryLastDataRepo mongoInventoryLastDataRepo;
	
	@Autowired
	private MongoInventoryNotificationRepo mongoInventoryNotificationRepo;

	@Autowired
	private DeviceServiceImpl deviceServiceImpl;
	
	@Autowired
	private GroupsServiceImpl groupsServiceImpl;
	
	@Autowired
	private MongoPositionRepoSFDA mongoPositionRepoSFDA;
	

	private final PositionMongoSFDARepository positionMongoSFDARepository ;

	private final DeviceRepositorySFDA deviceRepositorySFDA ;

	private final MongoEventsRepository mongoEventsRepository;

	public ReportServiceImplSFDA(PositionMongoSFDARepository positionMongoSFDARepository, DeviceRepositorySFDA deviceRepositorySFDA, MongoEventsRepository mongoEventsRepository) {
		this.positionMongoSFDARepository = positionMongoSFDARepository;
		this.deviceRepositorySFDA = deviceRepositorySFDA;
		this.mongoEventsRepository = mongoEventsRepository;
	}

	@Override
	public ResponseEntity<?> getInventoriesReport(String TOKEN, Long[] inventoryIds, int offset, String start,
			String end, String search, Long userId,String exportData) {
		logger.info("************************ getInventoriesReport STARTED ***************************");		
		List<InventoryLastData> inventoriesReport = new ArrayList<InventoryLastData>();
		if(TOKEN.equals("")) {
			
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",inventoriesReport);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		
		if(!TOKEN.equals("Schedule")) {
			if(super.checkActive(TOKEN)!= null)
			{
				return super.checkActive(TOKEN);
			}
		}
		
		
		User loggedUser = new User();
		if(userId != 0) {
			
			loggedUser = userServiceImpl.findById(userId);
			if(loggedUser == null) {
				getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "logged user is not found",inventoriesReport);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.status(404).body(getObjectResponse);
			}
		}	
		
		if(!loggedUser.getAccountType().equals(1)) {
			if(!userRoleService.checkUserHasPermission(userId, "INVENTORYTEMPHUMD", "list")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to get inventoriesReport list",inventoriesReport);
					logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
		}
		
		
		
		List<Long>allInventories= new ArrayList<>();
		
		if(inventoryIds.length != 0 ) {
			for(Long inventoryId:inventoryIds) {
				if(inventoryId !=0) {
					Inventory inventory =inventoryRepository.findOne(inventoryId);
					if(inventory != null) {
						
						Long createdBy=inventory.getUserId();
						Boolean isParent=false;

						if(createdBy.toString().equals(userId.toString())) {
							isParent=true;
						}
						List<User>childs = new ArrayList<User>();
						if(loggedUser.getAccountType().equals(4)) {
							 List<User> parents=userServiceImpl.getAllParentsOfuser(loggedUser,loggedUser.getAccountType());
							 if(parents.isEmpty()) {
								getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "as you are not have parent you cannot allow to edit this inventory.",null);
								return  ResponseEntity.badRequest().body(getObjectResponse);
							 }
							 else {
								 User parentClient = new User() ;

								 for(User object : parents) {
									 parentClient = object;
									 break;
								 }
								 
								userServiceImpl.resetChildernArray();
								childs = userServiceImpl.getAllChildernOfUser(parentClient.getId()); 
							 }
							 
						}
						else {
							userServiceImpl.resetChildernArray();
							childs = userServiceImpl.getAllChildernOfUser(userId);
						}
						
						
						
						User parentChilds = new User();
						if(!childs.isEmpty()) {
							for(User object : childs) {
								parentChilds = object;
								if(parentChilds.getId().toString().equals(createdBy.toString())) {
									isParent=true;
									break;
								}
							}
						}
						if(isParent == false) {
							getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Not creater or parent of creater to get inventory",null);
							return  ResponseEntity.badRequest().body(getObjectResponse);
						}
						
						allInventories.add(inventoryId);
					}
					
				}
			}
		}
		else {
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Inventory is not found",inventoriesReport);
			logger.info("************************ getInventoriesReport ENDED ***************************");		
			return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		Date dateFrom;
		Date dateTo;
		if(start.equals("0") || end.equals("0")) {
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Date start and end is Required",null);
			logger.info("************************ getInventoriesReport ENDED ***************************");
			return  ResponseEntity.badRequest().body(getObjectResponse);

		}
		else {

			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
			inputFormat1.setLenient(false);
			inputFormat.setLenient(false);
			outputFormat.setLenient(false);

			
			try {
				dateFrom = inputFormat.parse(start);
				start = outputFormat.format(dateFrom);
				

			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				try {
					dateFrom = inputFormat1.parse(start);
					start = outputFormat.format(dateFrom);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
					logger.info("************************ getInventoriesReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
			try {
				dateTo = inputFormat.parse(end);
				end = outputFormat.format(dateTo);
				

			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				try {
					dateTo = inputFormat1.parse(end);
					end = outputFormat.format(dateTo);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
					logger.info("************************ getInventoriesReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
			
			
			
			Date today=new Date();

			if(dateFrom.getTime() > dateTo.getTime()) {
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date should be Earlier than End Date",null);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
			if(today.getTime()<dateFrom.getTime() || today.getTime()<dateTo.getTime() ){
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date and End Date should be Earlier than Today",null);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}

		}
		List<InventoryLastData> data = new ArrayList<InventoryLastData>();
		Integer size=0;
		
		
		if(exportData.equals("exportData")) {
			data = mongoInventoryLastDataRepo.getInventoriesReportSchedule(allInventories, dateFrom, dateTo);

			if(data.size()>0) {
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());

					}
				}
					
			}
			getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",data,size);
			logger.info("************************ getInventoriesReport ENDED ***************************");
			return  ResponseEntity.ok().body(getObjectResponse);
		}
		
		if(!TOKEN.equals("Schedule")) {
			data = mongoInventoryLastDataRepo.getInventoriesReport(allInventories, offset, dateFrom, dateTo);
			
			if(data.size()>0) {
				size= mongoInventoryLastDataRepo.getInventoriesReportSize(allInventories,dateFrom, dateTo);
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());

					}
				}
					
			}
			
		}
		else {
			data = mongoInventoryLastDataRepo.getInventoriesReportSchedule(allInventories, dateFrom, dateTo);
			if(data.size()>0) {
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());

					}
				}
					
			}

		}
		
		getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",data,size);
		logger.info("************************ getInventoriesReport ENDED ***************************");
		return  ResponseEntity.ok().body(getObjectResponse);
	}

	@Override
	public ResponseEntity<?> getWarehousesReport(String TOKEN, Long[] warehouseIds, int offset, String start, String end,
			String search, Long userId,String exportData) {
		
		logger.info("************************ getWarehousesReport STARTED ***************************");		
		List<InventoryLastData> inventoriesReport = new ArrayList<InventoryLastData>();
		if(TOKEN.equals("")) {
			
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",inventoriesReport);
				logger.info("************************ getWarehousesReport ENDED ***************************");		
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		
		if(!TOKEN.equals("Schedule")) {
			if(super.checkActive(TOKEN)!= null)
			{
				return super.checkActive(TOKEN);
			}
		}
		
		
		User loggedUser = new User();
		if(userId != 0) {
			
			loggedUser = userServiceImpl.findById(userId);
			if(loggedUser == null) {
				getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "logged user is not found",inventoriesReport);
				logger.info("************************ getWarehousesReport ENDED ***************************");		
				return  ResponseEntity.status(404).body(getObjectResponse);
			}
		}	
		
		if(!loggedUser.getAccountType().equals(1)) {
			if(!userRoleService.checkUserHasPermission(userId, "WAREHOUSETEMPHUMD", "list")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to get inventoriesReport list",inventoriesReport);
					logger.info("************************ getWarehousesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
		}
		
		
		
		List<Long>allInventories= new ArrayList<>();
		List<Long>allWarehouses= new ArrayList<>();

		if(warehouseIds.length != 0 ) {
			for(Long warehouseId:warehouseIds) {
				if(warehouseId !=0) {
					Warehouse warehouse =warehousesRepository.findOne(warehouseId);
					if(warehouse != null) {
						Long createdBy=warehouse.getUserId();
						Boolean isParent=false;

						if(createdBy.toString().equals(userId.toString())) {
							isParent=true;
						}
						
						List<User>childs = new ArrayList<User>();
						if(loggedUser.getAccountType().equals(4)) {
							 List<User> parents=userServiceImpl.getAllParentsOfuser(loggedUser,loggedUser.getAccountType());
							 if(parents.isEmpty()) {
								getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "as you are not have parent you cannot allow to edit this warehouses.",null);
								return  ResponseEntity.badRequest().body(getObjectResponse);
							 }
							 else {
								 User parentClient = new User() ;

								 for(User object : parents) {
									 parentClient = object;
									 break;
								 }
								 
								userServiceImpl.resetChildernArray();
								childs = userServiceImpl.getAllChildernOfUser(parentClient.getId()); 
							 }
							 
						}
						else {
							userServiceImpl.resetChildernArray();
							childs = userServiceImpl.getAllChildernOfUser(userId);
						}
						
						
				 		
						User parentChilds = new User();
						if(!childs.isEmpty()) {
							for(User object : childs) {
								parentChilds = object;
								if(parentChilds.getId().toString().equals(createdBy.toString())) {
									isParent=true;
									break;
								}
							}
						}
						if(isParent == false) {
							getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Not creater or parent of creater to get warehouses",null);
							return  ResponseEntity.badRequest().body(getObjectResponse);
						}
						allWarehouses.add(warehouseId);

					}
					
				}
			}
		}
		else {
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Warehouse is not found",inventoriesReport);
			logger.info("************************ getWarehousesReport ENDED ***************************");		
			return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		if(allWarehouses.isEmpty()) {
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Warehouse is not found",inventoriesReport);
			logger.info("************************ getWarehousesReport ENDED ***************************");		
			return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		else {
			allInventories = inventoryRepository.getAllInventoriesOfWarehouse(allWarehouses);
			if(allInventories.isEmpty()) {
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "No Inventories for those Warehouses",inventoriesReport);
				logger.info("************************ getWarehousesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
		}
		
		
		
		Date dateFrom;
		Date dateTo;
		if(start.equals("0") || end.equals("0")) {
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Date start and end is Required",null);
			logger.info("************************ getWarehousesReport ENDED ***************************");
			return  ResponseEntity.badRequest().body(getObjectResponse);

		}
		else {

			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
			inputFormat1.setLenient(false);
			inputFormat.setLenient(false);
			outputFormat.setLenient(false);

			
			try {
				dateFrom = inputFormat.parse(start);
				start = outputFormat.format(dateFrom);
				

			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				try {
					dateFrom = inputFormat1.parse(start);
					start = outputFormat.format(dateFrom);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
					logger.info("************************ getWarehousesReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
			try {
				dateTo = inputFormat.parse(end);
				end = outputFormat.format(dateTo);
				

			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				try {
					dateTo = inputFormat1.parse(end);
					end = outputFormat.format(dateTo);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
					logger.info("************************ getWarehousesReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
			
			
			
			Date today=new Date();

			if(dateFrom.getTime() > dateTo.getTime()) {
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date should be Earlier than End Date",null);
				logger.info("************************ getWarehousesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
			if(today.getTime()<dateFrom.getTime() || today.getTime()<dateTo.getTime() ){
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date and End Date should be Earlier than Today",null);
				logger.info("************************ getWarehousesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}

		}
		List<InventoryLastData> data = new ArrayList<InventoryLastData>();
		Integer size=0;
		
		
		if(exportData.equals("exportData")) {
			data = mongoInventoryLastDataRepo.getInventoriesReportSchedule(allInventories, dateFrom, dateTo);

			if(data.size()>0) {
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());
						Warehouse war = warehousesRepository.findOne(inventory.getWarehouseId());
						data.get(i).setWarehouseId(war.getId());
						data.get(i).setWarehouseName(war.getName());
					}
				}
					
			}
			getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",data,size);
			logger.info("************************ getInventoriesReport ENDED ***************************");
			return  ResponseEntity.ok().body(getObjectResponse);
		}
		
		if(!TOKEN.equals("Schedule")) {
			data = mongoInventoryLastDataRepo.getInventoriesReport(allInventories, offset, dateFrom, dateTo);
			
			if(data.size()>0) {
				size= mongoInventoryLastDataRepo.getInventoriesReportSize(allInventories,dateFrom, dateTo);
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());
						Warehouse war = warehousesRepository.findOne(inventory.getWarehouseId());
						data.get(i).setWarehouseId(war.getId());
						data.get(i).setWarehouseName(war.getName());
					}
				}
					
			}
			
		}
		else {
			data = mongoInventoryLastDataRepo.getInventoriesReportSchedule(allInventories, dateFrom, dateTo);
			if(data.size()>0) {
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());
						Warehouse war = warehousesRepository.findOne(inventory.getWarehouseId());
						data.get(i).setWarehouseId(war.getId());
						data.get(i).setWarehouseName(war.getName());
					}
				}
					
			}

		}
		
		getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",data,size);
		logger.info("************************ getInventoriesReport ENDED ***************************");
		return  ResponseEntity.ok().body(getObjectResponse);
		
		

	}

	@Override
	public ResponseEntity<?> getNotificationReport(String TOKEN, Long[] inventoryIds, Long[] warehouseIds, int offset,
			String start, String end, String search, Long userId,String exportData) {
		logger.info("************************ getInventoriesReport STARTED ***************************");		
		List<InventoryLastData> inventoriesReport = new ArrayList<InventoryLastData>();
		if(TOKEN.equals("")) {
			
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",inventoriesReport);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		
		if(!TOKEN.equals("Schedule")) {
			if(super.checkActive(TOKEN)!= null)
			{
				return super.checkActive(TOKEN);
			}
		}
		
		
		User loggedUser = new User();
		if(userId != 0) {
			
			loggedUser = userServiceImpl.findById(userId);
			if(loggedUser == null) {
				getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "logged user is not found",inventoriesReport);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.status(404).body(getObjectResponse);
			}
		}	
		
		if(!loggedUser.getAccountType().equals(1)) {
			if(!userRoleService.checkUserHasPermission(userId, "NOTIFICATIONTEMPHUMD", "list")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to get inventoriesReport list",inventoriesReport);
					logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
		}
		
		
		
		List<Long>allInventories= new ArrayList<>();
		
		if(inventoryIds.length != 0 ) {
			for(Long inventoryId:inventoryIds) {
				if(inventoryId !=0) {
					Inventory inventory =inventoryRepository.findOne(inventoryId);
					if(inventory != null) {
						
						Long createdBy=inventory.getUserId();
						Boolean isParent=false;

						if(createdBy.toString().equals(userId.toString())) {
							isParent=true;
						}
						List<User>childs = new ArrayList<User>();
						if(loggedUser.getAccountType().equals(4)) {
							 List<User> parents=userServiceImpl.getAllParentsOfuser(loggedUser,loggedUser.getAccountType());
							 if(parents.isEmpty()) {
								getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "as you are not have parent you cannot allow to edit this inventory.",null);
								return  ResponseEntity.badRequest().body(getObjectResponse);
							 }
							 else {
								 User parentClient = new User() ;

								 for(User object : parents) {
									 parentClient = object;
									 break;
								 }
								 
								userServiceImpl.resetChildernArray();
								childs = userServiceImpl.getAllChildernOfUser(parentClient.getId()); 
							 }
							 
						}
						else {
							userServiceImpl.resetChildernArray();
							childs = userServiceImpl.getAllChildernOfUser(userId);
						}
						
						
						
						User parentChilds = new User();
						if(!childs.isEmpty()) {
							for(User object : childs) {
								parentChilds = object;
								if(parentChilds.getId().toString().equals(createdBy.toString())) {
									isParent=true;
									break;
								}
							}
						}
						if(isParent == false) {
							getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Not creater or parent of creater to get inventory",null);
							return  ResponseEntity.badRequest().body(getObjectResponse);
						}
						
						allInventories.add(inventoryId);
					}
					
				}
			}
		}
		
		List<Long>allWarehouses= new ArrayList<>();

		if(warehouseIds.length != 0 ) {
			for(Long warehouseId:warehouseIds) {
				if(warehouseId !=0) {
					Warehouse warehouse =warehousesRepository.findOne(warehouseId);
					if(warehouse != null) {
						Long createdBy=warehouse.getUserId();
						Boolean isParent=false;

						if(createdBy.toString().equals(userId.toString())) {
							isParent=true;
						}
						
						List<User>childs = new ArrayList<User>();
						if(loggedUser.getAccountType().equals(4)) {
							 List<User> parents=userServiceImpl.getAllParentsOfuser(loggedUser,loggedUser.getAccountType());
							 if(parents.isEmpty()) {
								getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "as you are not have parent you cannot allow to edit this warehouses.",null);
								return  ResponseEntity.badRequest().body(getObjectResponse);
							 }
							 else {
								 User parentClient = new User() ;

								 for(User object : parents) {
									 parentClient = object;
									 break;
								 }
								 
								userServiceImpl.resetChildernArray();
								childs = userServiceImpl.getAllChildernOfUser(parentClient.getId()); 
							 }
							 
						}
						else {
							userServiceImpl.resetChildernArray();
							childs = userServiceImpl.getAllChildernOfUser(userId);
						}
						
						
				 		
						User parentChilds = new User();
						if(!childs.isEmpty()) {
							for(User object : childs) {
								parentChilds = object;
								if(parentChilds.getId().toString().equals(createdBy.toString())) {
									isParent=true;
									break;
								}
							}
						}
						if(isParent == false) {
							getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Not creater or parent of creater to get warehouses",null);
							return  ResponseEntity.badRequest().body(getObjectResponse);
						}
						allWarehouses.add(warehouseId);

					}
					
				}
			}
		}
		
		if(!allWarehouses.isEmpty()) {
			allInventories.addAll(inventoryRepository.getAllInventoriesOfWarehouse(allWarehouses));
			
		}

		Date dateFrom;
		Date dateTo;
		if(start.equals("0") || end.equals("0")) {
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Date start and end is Required",null);
			logger.info("************************ getInventoriesReport ENDED ***************************");
			return  ResponseEntity.badRequest().body(getObjectResponse);

		}
		else {

			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
			inputFormat1.setLenient(false);
			inputFormat.setLenient(false);
			outputFormat.setLenient(false);

			
			try {
				dateFrom = inputFormat.parse(start);
				start = outputFormat.format(dateFrom);
				

			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				try {
					dateFrom = inputFormat1.parse(start);
					start = outputFormat.format(dateFrom);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
					logger.info("************************ getInventoriesReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
			try {
				dateTo = inputFormat.parse(end);
				end = outputFormat.format(dateTo);
				

			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				try {
					dateTo = inputFormat1.parse(end);
					end = outputFormat.format(dateTo);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
					logger.info("************************ getInventoriesReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
			
			
			
			Date today=new Date();

			if(dateFrom.getTime() > dateTo.getTime()) {
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date should be Earlier than End Date",null);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
			if(today.getTime()<dateFrom.getTime() || today.getTime()<dateTo.getTime() ){
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date and End Date should be Earlier than Today",null);
				logger.info("************************ getInventoriesReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}

		}
		
	
		
		List<InventoryNotification> data = new ArrayList<InventoryNotification>();
		Integer size=0;
		
		
		if(exportData.equals("exportData")) {
			data = mongoInventoryNotificationRepo.getNotificationsReportSchedule(allInventories, dateFrom, dateTo);
			if(data.size()>0) {
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());
						Warehouse war = warehousesRepository.findOne(inventory.getWarehouseId());
						data.get(i).setWarehouseId(war.getId());
						data.get(i).setWarehouseName(war.getName());
					}
				}
					
			}
			
			getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",data,size);
			logger.info("************************ getInventoriesReport ENDED ***************************");
			return  ResponseEntity.ok().body(getObjectResponse);
		}
		
		if(!TOKEN.equals("Schedule")) {
			data = mongoInventoryNotificationRepo.getNotificationsReport(allInventories, offset, dateFrom, dateTo);			
			if(data.size()>0) {
				size= mongoInventoryNotificationRepo.getNotificationsReportSize(allInventories,dateFrom, dateTo);
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());
						Warehouse war = warehousesRepository.findOne(inventory.getWarehouseId());
						data.get(i).setWarehouseId(war.getId());
						data.get(i).setWarehouseName(war.getName());
					}
				}
					
			}
			
		}
		else {
			data = mongoInventoryNotificationRepo.getNotificationsReportSchedule(allInventories, dateFrom, dateTo);
			if(data.size()>0) {
				for(int i=0;i<data.size();i++) {
					
					Inventory inventory = inventoryRepository.findOne(data.get(i).getInventory_id());
					if(inventory != null) {
						data.get(i).setInventoryName(inventory.getName());
						Warehouse war = warehousesRepository.findOne(inventory.getWarehouseId());
						data.get(i).setWarehouseId(war.getId());
						data.get(i).setWarehouseName(war.getName());
					}
				}
					
			}

		}
		
		getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",data,size);
		logger.info("************************ getInventoriesReport ENDED ***************************");
		return  ResponseEntity.ok().body(getObjectResponse);
	}

	@Override
	public ResponseEntity<?> getVehicleTempHum(String TOKEN, Long[] deviceIds, Long[] groupIds, int offset, String start,
			String end, String search, Long userId, String exportData) {
		 logger.info("************************ getSensorsReport STARTED ***************************");

			List<DeviceTempHum> positionsList = new ArrayList<DeviceTempHum>();
			if(TOKEN.equals("")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",positionsList);
				 logger.info("************************ getSensorsReport ENDED ***************************");
				 return  ResponseEntity.badRequest().body(getObjectResponse);
			}
			
			
			if(!TOKEN.equals("Schedule")) {
				if(super.checkActive(TOKEN)!= null)
				{
					return super.checkActive(TOKEN);
				}
			}
			
			
			User loggedUser = new User();
			if(userId != 0) {
				
				loggedUser = userServiceImpl.findById(userId);
				if(loggedUser == null) {
					getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "logged user is not found",positionsList);
					 logger.info("************************ getSensorsReport ENDED ***************************");
					return  ResponseEntity.status(404).body(getObjectResponse);
				}
			}	
			
			if(!loggedUser.getAccountType().equals(1)) {
				if(!userRoleService.checkUserHasPermission(userId, "SENSORWEIGHT", "list")) {
					 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to get SENSORWEIGHT list",positionsList);
					 logger.info("************************ getSensorsReport ENDED ***************************");
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
			}
			
			
			
			List<Long>allDevices= new ArrayList<>();

			if(groupIds.length != 0) {
				for(Long groupId:groupIds) {
					if(groupId != 0) {
				    	Group group=groupRepository.findOne(groupId);
				    	if(group != null) {
							if(group.getIs_deleted() == null) {
								boolean isParent = false;
								if(loggedUser.getAccountType().equals(4)) {
									Set<User> clientParents = loggedUser.getUsersOfUser();
									if(clientParents.isEmpty()) {
										getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "you are not allowed to get this group",positionsList);
										 logger.info("************************ getSensorsReport ENDED ***************************"); 
										return  ResponseEntity.badRequest().body(getObjectResponse);
									}else {
										User parent = null;
										for(User object : clientParents) {
											parent = object ;
										}

										Set<User>groupParents = group.getUserGroup();
										if(groupParents.isEmpty()) {
											getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "you are not allowed to get this group",positionsList);
											 logger.info("************************ getSensorsReport ENDED ***************************");
											return  ResponseEntity.badRequest().body(getObjectResponse);
										}else {
											for(User parentObject : groupParents) {
												if(parentObject.getId().equals(parent.getId())) {
													isParent = true;
													break;
												}
											}
										}
									}
									List<Long> CheckData = userClientGroupRepository.getGroup(userId,groupId);
									if(CheckData.isEmpty()) {
											isParent = false;
									}
									else {
											isParent = true;
									}
								}
								if(!groupsServiceImpl.checkIfParent(group , loggedUser) && ! isParent) {
									getObjectResponse = new GetObjectResponse( HttpStatus.BAD_REQUEST.value(), "you are not allowed to get this group ",positionsList);
									 logger.info("************************ getSensorsReport ENDED ***************************");
									return ResponseEntity.badRequest().body(getObjectResponse);
								}
								if(group.getType() != null) {
									if(group.getType().equals("driver")) {
										
										allDevices.addAll(groupRepository.getDevicesFromDriver(groupId));
									

									}
									else if(group.getType().equals("device")) {
										
										allDevices.addAll(groupRepository.getDevicesFromGroup(groupId));
										
										
									}
									else if(group.getType().equals("geofence")) {
										
										allDevices.addAll(groupRepository.getDevicesFromGeofence(groupId));
										

									}
								}

								
							}
				    	}
				    	

					}
				}
			}
			if(deviceIds.length != 0 ) {
				for(Long deviceId:deviceIds) {
					if(deviceId !=0) {
						Device device =deviceServiceImpl.findById(deviceId);
						boolean isParent = false;
						if(loggedUser.getAccountType() == 4) {
							Set<User>parentClients = loggedUser.getUsersOfUser();
							if(parentClients.isEmpty()) {
								getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user is not allwed to get data of this device ",positionsList);
								 logger.info("************************ getSensorsReport ENDED ***************************");
								return  ResponseEntity.badRequest().body(getObjectResponse);
							}else {
								User parent = null;
								for(User object : parentClients) {
									parent = object ;
								}
								Set<User>deviceParent = device.getUser();
								if(deviceParent.isEmpty()) {
									getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user is not allwed to get data of this device ",positionsList);
									 logger.info("************************ getSensorsReport ENDED ***************************");
									return  ResponseEntity.badRequest().body(getObjectResponse);
								}else {
									for(User  parentObject : deviceParent) {
										if(parent.getId() == parentObject.getId()) {
											isParent = true;
											break;
										}
									}
								}
							}
							List<Long> CheckData = userClientDeviceRepository.getDevice(userId,deviceId);
							if(CheckData.isEmpty()) {
									isParent = false;
							}
							else {
									isParent = true;
							}
						}
						if(!deviceServiceImpl.checkIfParent(device , loggedUser) && ! isParent) {
							getObjectResponse = new GetObjectResponse( HttpStatus.BAD_REQUEST.value(), "you are not allowed to get this device",positionsList);
							 logger.info("************************ getSensorsReport ENDED ***************************");
							return ResponseEntity.badRequest().body(getObjectResponse);
						}
						
						allDevices.add(deviceId);

						
		
					}
				}
			}

			Date dateFrom;
			Date dateTo;
			if(start.equals("0") || end.equals("0")) {
				getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Date start and end is Required",null);
				logger.info("************************ getEventsReport ENDED ***************************");		
				return  ResponseEntity.badRequest().body(getObjectResponse);

			}
			else {
				SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
				inputFormat1.setLenient(false);
				inputFormat.setLenient(false);
				outputFormat.setLenient(false);

				
				try {
					dateFrom = inputFormat.parse(start);
					start = outputFormat.format(dateFrom);
					

				} catch (ParseException e2) {
					// TODO Auto-generated catch block
					try {
						dateFrom = inputFormat1.parse(start);
						start = outputFormat.format(dateFrom);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						
						getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
						logger.info("************************ getEventsReport ENDED ***************************");		
						return  ResponseEntity.badRequest().body(getObjectResponse);
					}
					
				}
				
				try {
					dateTo = inputFormat.parse(end);
					end = outputFormat.format(dateTo);
					

				} catch (ParseException e2) {
					// TODO Auto-generated catch block
					try {
						dateTo = inputFormat1.parse(end);
						end = outputFormat.format(dateTo);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
						logger.info("************************ getEventsReport ENDED ***************************");		
						return  ResponseEntity.badRequest().body(getObjectResponse);
					}
					
				}
				
				
				
				
				Date today=new Date();

				if(dateFrom.getTime() > dateTo.getTime()) {
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date should be Earlier than End Date",null);
					logger.info("************************ getEventsReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				if(today.getTime()<dateFrom.getTime() || today.getTime()<dateTo.getTime() ){
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start Date and End Date should be Earlier than Today",null);
					logger.info("************************ getEventsReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
				search = "%"+search+"%";				
				
				String appendString="";
		
				if(allDevices.size()>0) {
					  for(int i=0;i<allDevices.size();i++) {
						  if(appendString != "") {
							  appendString +=","+allDevices.get(i);
						  }
						  else {
							  appendString +=allDevices.get(i);
						  }
					  }
				 }
				allDevices = new ArrayList<Long>();
				
				String[] data = {};
				if(!appendString.equals("")) {
			        data = appendString.split(",");

				}
		        

		        for(String d:data) {

		        	if(!allDevices.contains(Long.parseLong(d))) {
			        	allDevices.add(Long.parseLong(d));
		        	}
		        }
		        
		        if(allDevices.isEmpty()) {

		        	getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "no data for devices of group or devices that you selected ",positionsList);
					 logger.info("************************ getSensorsReport ENDED ***************************");
		        	return  ResponseEntity.badRequest().body(getObjectResponse);
		        }
			}
			Integer size = 0;

			if(exportData.equals("exportData")) {
				
				positionsList = mongoPositionRepoSFDA.getVehicleTempHumListScheduled(allDevices,dateFrom, dateTo);
				

				getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",positionsList,size);
				logger.info("************************ getSensorsReport ENDED ***************************");
				return  ResponseEntity.ok().body(getObjectResponse);
				
			}
			if(!TOKEN.equals("Schedule")) {
				search = "%"+search+"%";
				positionsList = mongoPositionRepoSFDA.getVehicleTempHumList(allDevices, offset, dateFrom, dateTo);
				if(positionsList.size()>0) {
					    size=mongoPositionRepoSFDA.getVehicleTempHumListSize(allDevices,dateFrom, dateTo);
				
				}
				
			}
			else {
				positionsList = mongoPositionRepoSFDA.getVehicleTempHumListScheduled(allDevices,dateFrom, dateTo);

			}
			
			getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",positionsList,size);
			logger.info("************************ getSensorsReport ENDED ***************************");
			return  ResponseEntity.ok().body(getObjectResponse);
	}

	@Override
	public ResponseEntity<?> getviewTripDetails(String TOKEN, Long deviceId, String from, String to,String exportData,int offset) {
		// TODO Auto-generated method stub
		

		logger.info("************************ getviewTrip STARTED ***************************");

		List<DeviceTempHum> positions = new ArrayList<DeviceTempHum>();
		if(TOKEN.equals("")) {
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",positions);
				logger.info("************************ getviewTrip ENDED ***************************");
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		if(super.checkActive(TOKEN)!= null)
		{
			return super.checkActive(TOKEN);
		}
		

		Date dateFrom;
		Date dateTo;

		SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS SSSS");
		SimpleDateFormat inputFormat1 = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");
		inputFormat1.setLenient(false);
		inputFormat.setLenient(false);
		outputFormat.setLenient(false);

		
		try {
			dateFrom = inputFormat2.parse(from);
			from = outputFormat.format(dateFrom);
			

		} catch (ParseException e2) {
			try {
				dateFrom = inputFormat.parse(from);
				from = outputFormat.format(dateFrom);
				

			} catch (ParseException e3) {
				// TODO Auto-generated catch block
				try {
					dateFrom = inputFormat1.parse(from);
					from = outputFormat.format(dateFrom);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z' or yyyy-MM-dd'T'HH:mm:ss.SSS SSSS",null);
					logger.info("************************ getEventsReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
		}
		
		
		try {
			dateTo = inputFormat2.parse(to);
			to = outputFormat.format(dateTo);
			

		} catch (ParseException e2) {
			try {
				dateTo = inputFormat.parse(to);
				to = outputFormat.format(dateTo);
				

			} catch (ParseException e3) {
				// TODO Auto-generated catch block
				try {
					dateTo = inputFormat1.parse(to);
					to = outputFormat.format(dateTo);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
					logger.info("************************ getEventsReport ENDED ***************************");		
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
				
			}
			
		}
		
		Device device = deviceServiceImpl.findById(deviceId);
		
		Integer size = 0;
		
		if(device != null) {
			
			
			if(exportData.equals("exportData")) {
				
				
				positions = mongoPositionRepoSFDA.getTripPositionsDetailsExport(deviceId, dateFrom, dateTo);

				getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",positions);
				logger.info("************************ getviewTrip ENDED ***************************");
				return  ResponseEntity.ok().body(getObjectResponse);
			}
			
			positions = mongoPositionRepoSFDA.getTripPositionsDetails(deviceId, dateFrom, dateTo,offset);

			if(positions.size() > 0) {
				size = mongoPositionRepoSFDA.getTripPositionsDetailsSize(deviceId, dateFrom, dateTo);

			}
			
			
			getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",positions,size);
			logger.info("************************ getviewTrip ENDED ***************************");
			return  ResponseEntity.ok().body(getObjectResponse);

		}
		else {
			getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "Device ID is not found",positions);
			logger.info("************************ getviewTrip ENDED ***************************");
			return  ResponseEntity.status(404).body(getObjectResponse);

		}
		
	}
	
	@Override
	public ResponseEntity<?> getVehicleTempHumPDF(String TOKEN, Long deviceId, int offset, String from,
			String to, String search, Long userId, String exportData) {
		 logger.info("************************ getSensorsReport STARTED ***************************");

			List<MonitorStaticstics> positionsList = new ArrayList<MonitorStaticstics>();
			if(TOKEN.equals("")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",positionsList);
				 logger.info("************************ getSensorsReport ENDED ***************************");
				 return  ResponseEntity.badRequest().body(getObjectResponse);
			}
			
			
			if(!TOKEN.equals("Schedule")) {
				if(super.checkActive(TOKEN)!= null)
				{
					return super.checkActive(TOKEN);
				}
			}
			
			
			User loggedUser = new User();
			if(userId != 0) {
				
				loggedUser = userServiceImpl.findById(userId);
				if(loggedUser == null) {
					getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "logged user is not found",positionsList);
					 logger.info("************************ getSensorsReport ENDED ***************************");
					return  ResponseEntity.status(404).body(getObjectResponse);
				}
			}	
			
			if(!loggedUser.getAccountType().equals(1)) {
				if(!userRoleService.checkUserHasPermission(userId, "SENSORWEIGHT", "list")) {
					 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to get SENSORWEIGHT list",positionsList);
					 logger.info("************************ getSensorsReport ENDED ***************************");
					return  ResponseEntity.badRequest().body(getObjectResponse);
				}
			}
			
			
			
			List<Long>allDevices= new ArrayList<>();
			allDevices.add(deviceId);
			Device device =deviceServiceImpl.findById(deviceId);

			String storingCategory="";
		    if(device.getAttributes() != null) {
			   if(device.getAttributes().toString().startsWith("{")) {
				   JSONObject object = new JSONObject();

				   object = new JSONObject(device.getAttributes().toString());		
		      	  
		      	   if(object.has("storingCategory")) {
		          	  storingCategory = object.getString("storingCategory");
		    	   }
			   }
			  
		    }
			

			Date dateFrom;
			Date dateTo;

			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS SSSS");
			SimpleDateFormat inputFormat1 = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");
			inputFormat1.setLenient(false);
			inputFormat.setLenient(false);
			outputFormat.setLenient(false);

			
			try {
				dateFrom = inputFormat2.parse(from);
				from = outputFormat.format(dateFrom);
				

			} catch (ParseException e2) {
				try {
					dateFrom = inputFormat.parse(from);
					from = outputFormat.format(dateFrom);
					

				} catch (ParseException e3) {
					// TODO Auto-generated catch block
					try {
						dateFrom = inputFormat1.parse(from);
						from = outputFormat.format(dateFrom);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						
						getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z' or yyyy-MM-dd'T'HH:mm:ss.SSS SSSS",null);
						logger.info("************************ getEventsReport ENDED ***************************");		
						return  ResponseEntity.badRequest().body(getObjectResponse);
					}
					
				}
				
			}
			
			
			try {
				dateTo = inputFormat2.parse(to);
				to = outputFormat.format(dateTo);
				

			} catch (ParseException e2) {
				try {
					dateTo = inputFormat.parse(to);
					to = outputFormat.format(dateTo);
					

				} catch (ParseException e3) {
					// TODO Auto-generated catch block
					try {
						dateTo = inputFormat1.parse(to);
						to = outputFormat.format(dateTo);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "Start and End Dates should be in the following format YYYY-MM-DD or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null);
						logger.info("************************ getEventsReport ENDED ***************************");		
						return  ResponseEntity.badRequest().body(getObjectResponse);
					}
					
				}
				
			}
				
				
			

			positionsList = mongoPositionRepoSFDA.getVehicleTempHumListDigram(allDevices,dateFrom, dateTo);
			
			List<Map> list = new ArrayList<>();
		    Map obj = new HashMap();
		    

            double high=0.0;
            double low=0.0;
            double avg=0.0;

		    Map rec = new HashMap();
		    rec.put("sequenceNumber",device.getSequence_number());
		    rec.put("uniqueId",device.getUniqueid());
		    rec.put("low",null);
		    rec.put("high",null);
		    rec.put("lowExtreme",null);
		    rec.put("highExtreme",null);
		    rec.put("size", null);
		    rec.put("start", null);
		    rec.put("end", null);
		    rec.put("avg",null);
		    rec.put("lowCheck", false);
		    rec.put("highCheck", false);
		    rec.put("lowAlarm", false);
		    rec.put("highAlarm", false);
		    rec.put("lowLimit", null);
		    rec.put("highLimit", null);
		    rec.put("storingCategory", storingCategory);

			for(MonitorStaticstics position:positionsList) {
				if(position.getName().equals("Temperature")) {
				    rec.put("size", position.getSeries().size());
				    if(position.getSeries().size() > 0) {
					    rec.put("end", position.getSeries().get(0).getName());
					    rec.put("start", position.getSeries().get(position.getSeries().size()-1).getName());
					    low=position.getSeries().get(0).getValue();
					    high=position.getSeries().get(0).getValue();

				    }

					for(Series series:position.getSeries()) {
						
						if(low>series.getValue()) {
							low = series.getValue();
						}
						
						if(high<series.getValue()) {
							high = series.getValue();
						}
						avg +=series.getValue();

					    rec = checkTemp(storingCategory,series.getValue(),rec);

						
					}
					
				    rec.put("avg",Math.round( (avg/position.getSeries().size()) * 100.0) / 100.0);

				}
			}
		    rec.put("low",low);
		    rec.put("high",high);

		    

		    
		    
		    obj.put("digram", positionsList);
		    obj.put("data", rec);
		    list.add(obj);
			
			getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",list);
			logger.info("************************ getSensorsReport ENDED ***************************");
			return  ResponseEntity.ok().body(getObjectResponse);
			
			
	}

	public Map checkTemp(String category,Double AvgTemp,Map record) {
		// TODO Auto-generated method stub
        
	
		
		//SCD1 -20°C to -10°C
		if(category.equals("SCD1")) {
			
			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", -20);
			record.put("highLimit", -10);
			

			if(AvgTemp < -20) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > -10) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}


		}
		
        //SCD2 2°C to 8°C
		else if(category.equals("SCD2")) {
			
			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", 2);
			record.put("highLimit", 8);
			
			if(AvgTemp < 2) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > 8) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}
		}
		
        //SCD3 Less than 25°C
		else if(category.equals("SCD3")) {
			
			record.put("highAlarm", true);
			record.put("highLimit", 25);
			
			
			if(AvgTemp >= 25) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
        //SCC1 Less than 25°C
		else if(category.equals("SCC1")) {
			
			record.put("highAlarm", true);
			record.put("highLimit", 25);
			
			if(AvgTemp >= 25) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
        //SCM1 -20°C to -10°C
		else if(category.equals("SCM1")) {
			
			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", -20);
			record.put("highLimit", -10);
			
			if(AvgTemp < -20) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > -10) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}
		}
	
        //SCM2 2°C to 8°C
		else if(category.equals("SCM2")) {
			
			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", 2);
			record.put("highLimit", 8);
			
			if(AvgTemp < 2) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > 8) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}
		}
		
		//SCM3 8°C to 15°C
		else if(category.equals("SCM3")) {
            
			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", 8);
			record.put("highLimit", 15);
			
			if(AvgTemp < 8) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > 15) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}
		}
		
		//SCM4 15°C to 30°C
		else if(category.equals("SCM4")) {
            
			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", 15);
			record.put("highLimit", 30);
			
			if(AvgTemp < 15) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > 30) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
        //SCM5 Should not exceed 40°C
		else if(category.equals("SCM5")) {
			
			record.put("highAlarm", true);
			record.put("highLimit", 40);	

			
			if(AvgTemp > 40) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
		//SCF1 Should not exceed 25°C
		else if(category.equals("SCF1")) {
            
			record.put("highAlarm", true);
			record.put("highLimit", 25);		
			
			if(AvgTemp > 25) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}
		}
		
		//SCF2 -1.5°C to 10°C
		else if(category.equals("SCF2")) {

			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", -1.5);
			record.put("highLimit", 10);
			
			
			if(AvgTemp < -1.5) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > 10) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}
		}
		
        //SCF3 -1.5°C to 21°C 
		else if(category.equals("SCF3")) {
			
			record.put("lowAlarm", true);
			record.put("highAlarm", true);
			
			record.put("lowLimit", -1.5);
			record.put("highLimit", 21);
			
			
			if(AvgTemp < -1.5) {
				record.put("lowCheck", true);

				if(record.get("lowExtreme") != null) {
					if((double)record.get("lowExtreme") > AvgTemp) {
						record.put("lowExtreme",AvgTemp);

					}
				}
				else {
					record.put("lowExtreme",AvgTemp);

				}

			}
			
			if(AvgTemp > 21) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
		//SCF4 Should not exceed (-18)°C
		else if(category.equals("SCF4")) {
            
			record.put("highAlarm", true);
			record.put("highLimit", -18);
			

			if(AvgTemp > -18) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
		//SCA1 Should not exceed 30°C
		else if(category.equals("SCA1")) {
			record.put("highAlarm", true);
			record.put("highLimit", 30);
			
			if(AvgTemp > 30) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
        //SCP1 Should not exceed 35°C
		else if(category.equals("SCP1")) {
			
			record.put("highAlarm", true);
			record.put("highLimit", 35);
			
			if(AvgTemp > 35) {
				record.put("highCheck", true);

				if(record.get("highExtreme") != null) {
					if((double)record.get("highExtreme") < AvgTemp) {
						record.put("highExtreme",AvgTemp);

					}
				}
				else {
					record.put("highExtreme",AvgTemp);

				}

			}

		}
		
		
		return record;
	}

	@Override
	public ResponseEntity<?> getTripPdfDetails(TripDetailsRequest request) {
		//get trip summary data --->maryam

		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");
		formatter.setLenient(false);
		PdfSummaryData summaryData = new PdfSummaryData();
		List<ReportDetails> reportDetails = new ArrayList();
		try {
			Date from = formatter.parse(request.getStartTime());
			Date to = formatter.parse(request.getEndTime());
			List<Position> positions = getDevicePositionsWithinDateRange(from , to , 12L);
			summaryData = getSummaryData(positions);
			reportDetails = getReportDetails(positions);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//get trip alarms ---->ehab
		//get graphs data ----> ehab
		//get trip details --->maryam
		
		ArrayList<Object> response = new ArrayList();
		response.add(summaryData);
		response.add(reportDetails);

		//getSummaryData(request);
		//get trip alarms ---->ehab
		//get graphs data ----> ehab
		//get trip details --->maryam

//		here
		AlarmsReportResponseWrapper alarmsReport = new AlarmsReportResponseWrapper();
		alarmsReport = getAlarmSection(request.getVehilceId(),request.getStartTime(),request.getEndTime());
		response.add(alarmsReport);
		getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",response);
		return  ResponseEntity.ok().body(getObjectResponse);
		
	}

	
	public PdfSummaryData getSummaryData(List<Position> positions) {
		int count = 0;
		Double avg = 0.0;
		Double max = 0.0;
		Double min = 0.0;
		PdfSummaryData pdfSummary = new PdfSummaryData() ;

		
			for(Position position :positions) {

				Map attributesMap = position.getAttributes();
				Iterator<Map.Entry<String, Integer>> iterator = attributesMap.entrySet().iterator();
//				System.out.println(getAvgTemp(attributesMap));
				Double recordAvg = getAvgTemp(attributesMap);
				
				if(recordAvg != 0.0) {
//					
					if(count == 0) {
						max = recordAvg;
						min = recordAvg;
						avg = recordAvg;
						count ++;
					}
					else {
						if(recordAvg > max) {
							max = recordAvg;
						}
						if(recordAvg < min) {
							min = recordAvg;
						}
						avg += recordAvg;
						count ++;
						
					}
					
				}
				
				
//			    while (iterator.hasNext()) {
//			        Map.Entry<String, Integer> entry = iterator.next();
//			        System.out.println(entry.getKey() + ":" + entry.getValue());
//			    }
				
			}
			avg = avg/count;
			System.out.println("max"+ max);
			System.out.println("min"+ min);
			System.out.println("avg"+ avg);
			double mkt = calcMKT(positions);
			pdfSummary = PdfSummaryData.builder().average(avg)
					.max(max).min(min).totalLength(positions.size()).mkt(mkt)
					  .build();
			

		// TODO Auto-generated method stub
		return pdfSummary;
//		return null;
		
	}

	public AlarmsReportResponseWrapper getAlarmSection(long deviceID,String start,String end){
		try {

			String storingCategory = new ObjectMapper().readValue(
					deviceRepositorySFDA.findOne(deviceID).getAttributes()
					, DeviceAttributes.class).getStoringCategory();
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss aa");
			formatter.setLenient(false);
			Date startDate = formatter.parse(start);
			Date endDate = formatter.parse(end);
			String tempAlarmConditionOver = "";
			String tempAlarmConditionBelow = "";
			String humAlarmConditionOver = "";
			String humAlarmConditionBelow = "";
			switch (storingCategory){
				case "SCD1":
				case "SCM1":
					tempAlarmConditionOver = "Temperature Over -10°C";
					tempAlarmConditionBelow = "Temperature  Below -20°C";
					break;
				case "SCD2":
				case "SCM2":
					tempAlarmConditionOver = "Temperature Over 8°C";
					tempAlarmConditionBelow = "Temperature Below 2°C";
					break;
				case "SCD3":
				case "SCC1":
					tempAlarmConditionOver = "Temperature Over 25°C";
					humAlarmConditionOver = "Humidity Over 60%";
					break;
				case "SCM3":
					tempAlarmConditionOver = "Temperature Over 15°C";
					tempAlarmConditionBelow = "Temperature Below 8°C";
					humAlarmConditionOver = "Humidity Over 60%";
					break;
				case "SCM4":
					tempAlarmConditionOver = "Temperature Over 30°C";
					tempAlarmConditionBelow ="Temperature Below 15°C";
					humAlarmConditionOver = "Humidity Over 60%";
					break;
				case "SCM5":
					tempAlarmConditionOver = "Temperature Over 40°C";
					break;
				case "SCF1":
					tempAlarmConditionOver = "Temperature Over 25°C ";
					humAlarmConditionOver = "Humidity Over 60%";
					break;
				case "SCF2":
					tempAlarmConditionOver = "Temperature over 10°C";
					tempAlarmConditionBelow = "Temperature Below -1.5°C";
					humAlarmConditionOver = "Humidity Over 90%";
					humAlarmConditionBelow = "Humidity Below 75%";
					break;
				case "SCF3":
					tempAlarmConditionOver = "Temperature over 21°C";
					tempAlarmConditionBelow = "Temperature Below -1.5°C";
					humAlarmConditionOver = "Humidity Over 95%";
					humAlarmConditionBelow = "Humidity Below 85%";
					break;
				case "SCF4":
					tempAlarmConditionOver = "Temperature over -18°C";
					humAlarmConditionOver = "Humidity Over 75%";
					humAlarmConditionBelow = "Humidity Below 99%";
					break;
				case "SCA1":
					tempAlarmConditionOver = "Temperature over 30°C";
					humAlarmConditionOver = "Humidity Over 60%";
					break;
				case "SCP1":
					tempAlarmConditionOver = "Temperature over 35°C";
					break;
				default:
					tempAlarmConditionOver = "";
					humAlarmConditionOver = "";
			}

			List<MongoEvents> tempOverAlarms = mongoEventsRepository
					.findAllByDeviceidAndServertimeBetweenAndType(deviceID,startDate,
							endDate,"tepmperatureIncreasedAlarm");
			if(tempOverAlarms.size()>1){
				tempOverAlarms.sort(Comparator.comparing(MongoEvents::getServertime));
			}


			List<MongoEvents> tempBelowAlarms = mongoEventsRepository
					.findAllByDeviceidAndServertimeBetweenAndType(deviceID,startDate,
							endDate,"tepmperatureDecreasedAlarm");
			if(tempBelowAlarms.size()>1){
				tempBelowAlarms.sort(Comparator.comparing(MongoEvents::getServertime));
			}


			List<MongoEvents> humidityOverAlarms = mongoEventsRepository
					.findAllByDeviceidAndServertimeBetweenAndType(deviceID,startDate,
							endDate,"humidityIncreasedAlarm");
			if(humidityOverAlarms.size()>1){
				humidityOverAlarms.sort(Comparator.comparing(MongoEvents::getServertime));
			}

			List<MongoEvents> humidityBelowAlarms = mongoEventsRepository
					.findAllByDeviceidAndServertimeBetweenAndType(deviceID,startDate,
							endDate,"humidityDecreasedAlarm");
			if(humidityBelowAlarms.size()>1){
				humidityBelowAlarms.sort(Comparator.comparing(MongoEvents::getServertime));
			}


			List<AlarmSectionWrapperResponse> alarmSectionWrapperList = new ArrayList<>();

			if(!tempAlarmConditionOver.equals("")&&tempOverAlarms.size()>0){

				alarmSectionWrapperList.add(
						AlarmSectionWrapperResponse.builder()
						.alarmCondition(tempAlarmConditionOver)
						.firstAlarmTime(tempOverAlarms.get(0).getServertime())
						.numOfAlarms(tempOverAlarms.size())
						.build());

			}

			if(!tempAlarmConditionBelow.equals("")&&tempBelowAlarms.size()>0){
				alarmSectionWrapperList.add(
					AlarmSectionWrapperResponse.builder()
							.alarmCondition(tempAlarmConditionBelow)
							.firstAlarmTime(tempBelowAlarms.get(0).getServertime())
							.numOfAlarms(tempBelowAlarms.size())
							.build()
				);
			}

			if(!humAlarmConditionOver.equals("")&&humidityOverAlarms.size()>0){
				alarmSectionWrapperList.add(
						AlarmSectionWrapperResponse.builder()
								.alarmCondition(humAlarmConditionOver)
								.firstAlarmTime(humidityOverAlarms.get(0).getServertime())
								.numOfAlarms(humidityOverAlarms.size())
								.build()
				);
			}

			if(!humAlarmConditionBelow.equals("")&&humidityBelowAlarms.size()>0){
				alarmSectionWrapperList.add(
						AlarmSectionWrapperResponse.builder()
								.alarmCondition(humAlarmConditionBelow)
								.firstAlarmTime(humidityBelowAlarms.get(0).getServertime())
								.numOfAlarms(humidityBelowAlarms.size())
								.build()
				);
			}


			List<Position> positionList =positionMongoSFDARepository.findAllByDevicetimeBetweenAndDeviceid(startDate,endDate,deviceID);
			List<GraphObject> temperatureGraph = new ArrayList<>();
			List<GraphObject> humidityGraph = new ArrayList<>();
			List<GraphDataWrapper> graphDataWrapperList = new ArrayList<>();
			for(Position position :positionList){
				temperatureGraph.add(GraphObject.builder()
						.name(position.getDevicetime().toString())
						.value(getAvgTemp(position.getAttributes()))
						.build());
				double hum = (Double)position.getAttributes().get("hum1") ;
				if(hum>0&&hum<300){
					humidityGraph.add(GraphObject.builder()
							.name(position.getDevicetime().toString())
							.value(hum)
							.build());
				}
			}
			graphDataWrapperList.add(GraphDataWrapper.builder()
					.series("series")
					.graphObjectList(temperatureGraph).build());

			graphDataWrapperList.add(GraphDataWrapper.builder()
					.series("series")
					.graphObjectList(humidityGraph).build());

			return AlarmsReportResponseWrapper.builder()
					.alarmsSection(alarmSectionWrapperList)
					.graphDataWrapperList(graphDataWrapperList)
					.build();

		}catch (Exception e){
			System.out.println(e);
			System.out.println(e.getMessage());
			return null;
		}

	}



	public Double getAvgTemp(Map attributesMap) {
		int count = 0;
		Double avg = 0.0;
		List<Double> avgs = new ArrayList<>();
		if(attributesMap.keySet().toString().contains("temp")){
			attributesMap.keySet().stream().filter(o ->
					o.toString().contains("temp"))
					.forEach(o -> {
						if(!attributesMap.get(o).equals(0.0) && !attributesMap.get(o).equals(300.0)) {
							avgs.add(Double.parseDouble(attributesMap.get(o).toString()));
						}


					});
		}
		for(Double avrage : avgs){
			avg+=avrage;
		}
		if(avgs.size()>0){
			avg/=avgs.size();
		}
//		if(attributesMap.containsKey("temp1")) {
//			if(!attributesMap.get("temp1").equals(0.0) && !attributesMap.get("temp1").equals(300.0)) {
//				System.out.println("temp1"+attributesMap.get("temp1"));
//				count++;
//				avg += (Double)attributesMap.get("temp1");
//			}
//		}
//		if(attributesMap.containsKey("temp2") && !attributesMap.get("temp2").equals(300.0)) {
//			if(!attributesMap.get("temp2").equals(0.0)){
//				System.out.println("temp2"+attributesMap.get("temp2"));
//				count++;
//				avg += (Double)attributesMap.get("temp2");
//			}
//		}
//		if(attributesMap.containsKey("temp3") && !attributesMap.get("temp2").equals(300.0)) {
//			if(!attributesMap.get("temp3").equals(0.0)){
//				System.out.println("temp3"+attributesMap.get("temp3"));
//				count++;
//				avg += (Double)attributesMap.get("temp3");
//			}
//		}
//		if(attributesMap.containsKey("temp4") && !attributesMap.get("temp2").equals(300.0)) {
//			if(!attributesMap.get("temp4").equals(0)){
//				System.out.println("temp4"+attributesMap.get("temp4"));
//				count++;
//				avg += (Double)attributesMap.get("temp4");
//			}
//		}
//		if(attributesMap.containsKey("temp6") && !attributesMap.get("temp6").equals(300.0)) {
//			if(!attributesMap.get("temp6").equals(0.0)){
//				System.out.println("temp6"+attributesMap.get("temp6"));
//				count++;
//				avg += (Double)attributesMap.get("temp6");
//			}
//		}
//		if(attributesMap.containsKey("temp7") && !attributesMap.get("temp7").equals(300.0)) {
//			if(!attributesMap.get("temp7").equals(0.0)){
//				System.out.println("temp7"+attributesMap.get("temp7"));
//				count++;
//				avg += (Double)attributesMap.get("temp7");
//
//			}
//		}
//		if(attributesMap.containsKey("temp8") && !attributesMap.get("temp8").equals(300.0)) {
//			if(!attributesMap.get("temp8").equals(0.0)){
//				System.out.println("temp8"+attributesMap.get("temp8"));
//				count++;
//				avg += (Double)attributesMap.get("temp8");
//			}
//		}
//		if(attributesMap.containsKey("wiretemp1") && !attributesMap.get("wiretemp1").equals(300.0)) {
//			if(!attributesMap.get("wiretemp1").equals(0.0)){
//				System.out.println("wire1"+attributesMap.get("wiretemp1"));
//				count++;
//				avg += (Double)attributesMap.get("wiretemp1");
//			}
//		}
//		if(attributesMap.containsKey("wiretemp2") && !attributesMap.get("wiretemp2").equals(300.0)) {
//			if(!attributesMap.get("wiretemp2").equals(0.0)){
//				System.out.println("wire2"+attributesMap.get("wiretemp2"));
//				count++;
//				avg += (Double)attributesMap.get("wiretemp2");
//			}
//		}
//		if(attributesMap.containsKey("wiretemp3") && !attributesMap.get("wiretemp3").equals(300.0)) {
//			if(!attributesMap.get("wiretemp3").equals(0.0)){
//				System.out.println("wire3"+attributesMap.get("wiretemp3"));
//				count++;
//				avg += (Double)attributesMap.get("wiretemp3");
//			}
//		}
//		if(attributesMap.containsKey("wiretemp4") && !attributesMap.get("wiretemp4").equals(300.0)) {
//			if(!attributesMap.get("wiretemp4").equals(0.0)){
//				System.out.println("wire4"+attributesMap.get("wiretemp4"));
//				count++;
//				avg += (Double)attributesMap.get("wiretemp4");
//			}
//		}
//		if(avg>0) {
//			avg = avg/count;
//		}
//
		return avg;
		
	}
	
	public List<ReportDetails> getReportDetails(List<Position> positions) {
		List<ReportDetails> reportDetailsList = new ArrayList();
		SimpleDateFormat formatDateJava = new SimpleDateFormat("yyyy-mm-dd");
		SimpleDateFormat formatTime = new SimpleDateFormat("HH:MM:SS");
		
		for(Position position : positions) {
			
			ReportDetails reportDetails = new ReportDetails();
			Map attributesMap = position.getAttributes();
			Double recordAvgTemp = getAvgTemp(attributesMap);
			Double humidity = 0.0;
			if(attributesMap.containsKey("hum1")) {
				if((Double)attributesMap.get("hum1")!= 300) {
					humidity = (Double)attributesMap.get("hum1");
				}
				
			}
			String devicetimeAsDateStr = formatDateJava.format(position.getDevicetime());
			String devicetimeAsTimeStr = formatTime.format(position.getDevicetime());
			
			reportDetails = reportDetails.builder().date(devicetimeAsDateStr).time(devicetimeAsTimeStr)
					.temperature(recordAvgTemp).humidity(humidity)
					  .build();
			
			
			reportDetailsList.add(reportDetails);
			
		}
		
		return reportDetailsList;
	}
	
	public List<Position> getDevicePositionsWithinDateRange(Date from , Date to , long deviceid){
		List<Position> pos = positionMongoSFDARepository.findAllByDevicetimeBetweenAndDeviceid(from,to,12L);
		return pos;
	}
	
	public Double calcMKT(List<Position> positions) {
		double allExponenials = 0.0;
		double result = 0.0;
		for(Position position :positions) {
			Map attributesMap = position.getAttributes();
			Double recordAvg = getAvgTemp(attributesMap);
			 double t1 = -(10000/(recordAvg+273.1));//t1 value is ΔH/RT, according to the formula: H/R=10000K K = 273.1 + temperature, so 10000 divided by K equals t1
			 double e1 = Math.exp(t1);//Find the value of e to the power of t1 //Math.exp(x) e to the power of x
			 allExponenials += e1;
		}
		int n = positions.size();
		if (n>0) {
			result = Math.log((allExponenials)/n);
			result = ((-10000/result)-273.1);
		}
		return result;
		 
		        
		 
		         
	}
	
	
	
	
}
