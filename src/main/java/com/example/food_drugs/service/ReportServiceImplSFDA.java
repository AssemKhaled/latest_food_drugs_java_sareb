package com.example.food_drugs.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.example.examplequerydslspringdatajpamaven.entity.Device;
import com.example.examplequerydslspringdatajpamaven.entity.Group;
import com.example.examplequerydslspringdatajpamaven.entity.TripPositions;
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
import com.example.food_drugs.entity.Warehouse;
import com.example.food_drugs.repository.InventoryRepository;
import com.example.food_drugs.repository.MongoInventoryLastDataRepo;
import com.example.food_drugs.repository.MongoInventoryNotificationRepo;
import com.example.food_drugs.repository.MongoPositionRepoSFDA;
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
	
	
}
