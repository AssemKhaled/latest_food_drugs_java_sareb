package com.example.food_drugs.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.examplequerydslspringdatajpamaven.entity.CustomDeviceList;
import com.example.examplequerydslspringdatajpamaven.entity.CustomDeviceLiveData;
import com.example.examplequerydslspringdatajpamaven.entity.Device;
import com.example.examplequerydslspringdatajpamaven.entity.MongoPositions;
import com.example.examplequerydslspringdatajpamaven.entity.User;
import com.example.examplequerydslspringdatajpamaven.repository.DeviceRepository;
import com.example.examplequerydslspringdatajpamaven.repository.MongoPositionsRepository;
import com.example.examplequerydslspringdatajpamaven.repository.UserClientDeviceRepository;
import com.example.examplequerydslspringdatajpamaven.responses.GetObjectResponse;
import com.example.examplequerydslspringdatajpamaven.rest.RestServiceController;
import com.example.examplequerydslspringdatajpamaven.service.DeviceServiceImpl;
import com.example.examplequerydslspringdatajpamaven.service.UserRoleServiceImpl;
import com.example.examplequerydslspringdatajpamaven.service.UserServiceImpl;
import com.example.food_drugs.repository.DeviceRepositorySFDA;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * services functionality related to devices SFDA
 * @author fuinco
 *
 */
@Component
@Service
public class DeviceServiceImplSFDA extends RestServiceController implements DeviceServiceSFDA{

	private static final Log logger = LogFactory.getLog(DeviceServiceImplSFDA.class);
	
	private GetObjectResponse getObjectResponse;
	
	@Autowired
	private UserServiceImpl userServiceImpl;
	
	@Autowired
	private UserRoleServiceImpl userRoleServiceImpl;
	
	@Autowired
	private DeviceServiceImpl deviceServiceImpl;
	
	@Autowired
	private DeviceRepositorySFDA deviceRepositorySFDA;
	
	@Autowired
	private MongoPositionsRepository mongoPositionsRepository;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private UserClientDeviceRepository userClientDeviceRepository;
	
	@Override
	public ResponseEntity<?> activeDeviceSFDA(String TOKEN, Long userId, Long deviceId) {
	    logger.info("************************ activeDevice ENDED ***************************");
		if(TOKEN.equals("")) {
			 List<Device> devices = null;
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",devices);
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		if(super.checkActive(TOKEN)!= null)
		{
			return super.checkActive(TOKEN);
		}
		 if(deviceId.equals(0) || userId.equals(0)) {
			 List<Device> devices = null;
			 getObjectResponse = new GetObjectResponse( HttpStatus.BAD_REQUEST.value(), "User ID and Device ID are Required",devices);
		     logger.info("************************ activeDevice ENDED ***************************");
		     return ResponseEntity.badRequest().body(getObjectResponse); 
		 }
		User loggedUser = userServiceImpl.findById(userId);
		if(loggedUser == null) {
			List<Device> devices = null;
			getObjectResponse = new GetObjectResponse( HttpStatus.NOT_FOUND.value(), "User ID is not found",devices);
		    logger.info("************************ activeDevice ENDED ***************************");
		    return ResponseEntity.status(404).body(getObjectResponse);
		}
		if(!loggedUser.getAccountType().equals(1)) {
			if(!userRoleServiceImpl.checkUserHasPermission(userId, "DEVICE", "delete")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to delete device",null);
				 logger.info("************************ activeDevice ENDED ***************************");
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
		}
		 Device device = deviceRepository.findOne(deviceId);
		 if(device == null)
		 {
			 List<Device> devices = null;
			 getObjectResponse = new GetObjectResponse( HttpStatus.NOT_FOUND.value(), "This device is not found",devices);
		     logger.info("************************ activeDevice ENDED ***************************");
		     return ResponseEntity.status(404).body(getObjectResponse);
		 }
		 else
		 {
			 boolean isParent = false;
			 User creater= userServiceImpl.findById(userId);
			 if(creater == null) {
				 List<Device> devices = null;
				 getObjectResponse = new GetObjectResponse( HttpStatus.NOT_FOUND.value(), "This user is not found",devices);
			     logger.info("************************ activeDevice ENDED ***************************");
			     return ResponseEntity.status(404).body(getObjectResponse);
			 }
			 if(creater.getAccountType().equals(4)) {
				    Set<User>parentClient = creater.getUsersOfUser();
					if(parentClient.isEmpty()) {
						getObjectResponse = new GetObjectResponse( HttpStatus.BAD_REQUEST.value(), "you are not allowed to edit this user ",null);
						logger.info("************************ editDevice ENDED ***************************");
						return ResponseEntity.badRequest().body(getObjectResponse);
					}else {
					  
						User parent =null;
						for(User object : parentClient) {
							parent = object;
						}
						Set<User> deviceParent = device.getUser();
						if(deviceParent.isEmpty()) {
							getObjectResponse = new GetObjectResponse( HttpStatus.BAD_REQUEST.value(), " this device is not assigned to any user ,you are not allowed to edit this user ",null);
							logger.info("************************ editDevice ENDED ***************************");
							return ResponseEntity.badRequest().body(getObjectResponse);
						}else {
							
							for(User deviceUser : deviceParent) {
								if(deviceUser.getId().equals(parent.getId())) {
									
									isParent = true;
									break;
								}
							}
						}
					}
			 }
			 if(!deviceServiceImpl.checkIfParent(device , creater)&& ! isParent) {
					getObjectResponse = new GetObjectResponse( HttpStatus.BAD_REQUEST.value(), "you are not allowed to active this user ",null);
					logger.info("************************ editDevice ENDED ***************************");
					return ResponseEntity.badRequest().body(getObjectResponse);
			 }
			 
		     device.setDelete_date(null);
		     deviceRepository.save(device);
		     
		     List<Device> devices = null;
		     
		     
		     
			 getObjectResponse = new GetObjectResponse( HttpStatus.OK.value(), "success",devices);
		     logger.info("************************ activeDevice ENDED ***************************");
		     return ResponseEntity.ok().body(getObjectResponse);
		 }
			 
	}


	@Override
	public ResponseEntity<?> getAllUserDevicesSFDA(String TOKEN, Long userId, int offset, String search, int active,String exportData) {
		// TODO Auto-generated method stub
 
		logger.info("************************ getAllUserDevices STARTED ***************************");
		if(TOKEN.equals("")) {
			 List<Device> devices = null;
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",devices);
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		if(super.checkActive(TOKEN) != null)
		{
			return super.checkActive(TOKEN);
		}
		
		if(userId.equals(0)) {
			 List<CustomDeviceList> devices= null;
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "User ID is Required",devices);
			 logger.info("************************ getAllUserDevices ENDED ***************************");
			return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		User loggedUser = userServiceImpl.findById(userId);
		if(loggedUser == null) {
			 List<CustomDeviceList> devices= null;
			 getObjectResponse = new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "This user is not found",devices);
			 logger.info("************************ getAllUserDevices ENDED ***************************");
			return  ResponseEntity.status(404).body(getObjectResponse);
		}

		if(!loggedUser.getAccountType().equals(1)) {
			if(!userRoleServiceImpl.checkUserHasPermission(userId, "DEVICE", "list")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to get devices list",null);
				 logger.info("************************ getAllUserDevices ENDED ***************************");
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
		}
		
		 List<Long>usersIds= new ArrayList<>();
		 Integer size=0;
		 List<CustomDeviceList> devices = new ArrayList<CustomDeviceList>();
		 if(loggedUser.getAccountType().equals(4)) {
			 
			 List<Long> deviceIds = userClientDeviceRepository.getDevicesIds(userId);
			 
			 if(deviceIds.size()>0) {

				 if(active == 0) {
					if(exportData.equals("exportData")) {
						
						devices = deviceRepositorySFDA.getDevicesListByIdsDeactiveExport(deviceIds, search);

					}
					else {
						devices = deviceRepositorySFDA.getDevicesListByIdsDeactive(deviceIds, offset, search);
					    size = deviceRepositorySFDA.getDevicesListSizeByIdsDeactive(deviceIds, search);
					}


				 }
				 
                 if(active == 2) {
 					if(exportData.equals("exportData")) {
	                	 devices = deviceRepositorySFDA.getDevicesListByIdsAllExport(deviceIds, search);

 					}
 					else {

 	                	 devices = deviceRepositorySFDA.getDevicesListByIdsAll(deviceIds, offset, search);
 					     size = deviceRepositorySFDA.getDevicesListSizeByIdsAll(deviceIds, search);
 					}

				 }
                 
                 if(active == 1) {
  					if(exportData.equals("exportData")) {
  						devices= deviceRepository.getDevicesListByIdsExport(deviceIds,search);

  					}
  					else {
  						devices= deviceRepository.getDevicesListByIds(deviceIds,offset,search);
  	    				size=  deviceRepository.getDevicesListSizeByIds(deviceIds,search);
  					}


    			 }
				 
			 }

			 
		 }
		 else {
			 List<User>childernUsers = userServiceImpl.getAllChildernOfUser(userId);
			 if(childernUsers.isEmpty()) {
				 usersIds.add(userId);
			 }
			 else {
				 usersIds.add(userId);
				 for(User object : childernUsers) {
					 usersIds.add(object.getId());
				 }
			 }
		 
		 
			 if(active == 0) {
				if(exportData.equals("exportData")) {
					devices= deviceRepositorySFDA.getDevicesListDeactiveExport(usersIds,search);

				}
				else {
					devices= deviceRepositorySFDA.getDevicesListDeactive(usersIds,offset,search);
					size=  deviceRepositorySFDA.getDevicesListSizeDeactive(usersIds,search);
				}


			 }
			 
             if(active == 2) {
 				if(exportData.equals("exportData")) {
 	            	devices= deviceRepositorySFDA.getDevicesListAllExport(usersIds,search);

 				}
 				else {
 	            	devices= deviceRepositorySFDA.getDevicesListAll(usersIds,offset,search);
 	    			size=  deviceRepositorySFDA.getDevicesListSizeAll(usersIds,search);
 				}


			 }
             
             if(active == 1) {
  				if(exportData.equals("exportData")) {
  	    			devices= deviceRepository.getDevicesListExport(usersIds,search);

  				}
  				else {
  	    			devices= deviceRepository.getDevicesList(usersIds,offset,search);
  	    			size=  deviceRepository.getDevicesListSize(usersIds,search);
  				}


			 }
		 

		}
		 getObjectResponse = new GetObjectResponse(HttpStatus.OK.value(), "success",devices,size);
		 logger.info("************************ getAllUserDevices ENDED ***************************");
		 return  ResponseEntity.ok().body(getObjectResponse);
	}


	

}
