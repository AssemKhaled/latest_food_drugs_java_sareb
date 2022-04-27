package com.example.food_drugs.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.example.food_drugs.service.NotificationServiceSFDA;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.example.examplequerydslspringdatajpamaven.entity.Notification;
import com.example.examplequerydslspringdatajpamaven.entity.User;
import com.example.examplequerydslspringdatajpamaven.repository.NotificationRepository;
import com.example.examplequerydslspringdatajpamaven.responses.GetObjectResponse;
import com.example.examplequerydslspringdatajpamaven.rest.RestServiceController;
import com.example.examplequerydslspringdatajpamaven.service.NotificationServiceImpl;
import com.example.examplequerydslspringdatajpamaven.service.UserRoleService;
import com.example.examplequerydslspringdatajpamaven.service.UserServiceImpl;
import com.example.food_drugs.repository.NotificationRepositorySFDA;

/**
 * services functionality related to notifications
 * @author fuinco
 *
 */
@Component
@Service
public class NotificationServiceImplSFDA extends RestServiceController implements NotificationServiceSFDA {

	private static final Log logger = LogFactory.getLog(NotificationServiceImpl.class);

	private GetObjectResponse getObjectResponse;
	
	@Autowired
	private UserServiceImpl userService;
	
	@Autowired
	private UserRoleService userRoleService;
	
	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private NotificationRepositorySFDA notificationRepositorySFDA;
	
	@Autowired 
	private NotificationServiceImpl notificationServiceImpl;

	@Override
	public ResponseEntity<?> activeNotification(String TOKEN, Long notificationId, Long userId) {
		logger.info("************************ activeNotification STARTED ***************************");

		List<Notification> notifications = new ArrayList<Notification>();
		User user = userService.findById(userId);
		if(user == null ) {
			getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "This User is not Found",notifications);
			return  ResponseEntity.status(404).body(getObjectResponse);

		}
		
		if(user.getAccountType()!= 1) {
			if(!userRoleService.checkUserHasPermission(userId, "NOTIFICATION", "delete")) {
				 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to active",null);
				 logger.info("************************ activeNotification ENDED ***************************");
				return  ResponseEntity.badRequest().body(getObjectResponse);
			}
		}
		if(TOKEN.equals("")) {
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",notifications);
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		if(super.checkActive(TOKEN)!= null)
		{
			return super.checkActive(TOKEN);
		}
		if(notificationId != 0) {
			Notification notification = notificationRepository.findOne(notificationId);
			if(notification != null) {
				
				
				 if(!notificationServiceImpl.checkIfParent(notification , user)) {
						getObjectResponse = new GetObjectResponse( HttpStatus.BAD_REQUEST.value(), "you are not allowed to active this notification ",notifications);
						logger.info("************************ activeNotification ENDED ***************************");
						return ResponseEntity.badRequest().body(getObjectResponse);
					}
				 
				    notification.setDelete_date(null);
				    notificationRepository.save(notification);
				    
					getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "success",notifications);
					logger.info("************************ activeNotification ENDED ***************************");
					return  ResponseEntity.ok().body(getObjectResponse);
				
				
					

				
				
			}
			else {

				getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "This notification ID was not found",notifications);
				return  ResponseEntity.status(404).body(getObjectResponse);

			}
						
		}
		else {
			
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "notification ID is Required",notifications);
			return  ResponseEntity.badRequest().body(getObjectResponse);

		}
	}

	@Override
	public ResponseEntity<?> getAllNotificationsSFDA(String TOKEN, Long id, int offset, String search, int active,String exportData) {
		
		logger.info("************************ getAllNotifications STARTED ***************************");
		
		List<Notification> notifications = new ArrayList<Notification>();
		
		if(TOKEN.equals("")) {
			 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",notifications);
			 return  ResponseEntity.badRequest().body(getObjectResponse);
		}
		
		if(super.checkActive(TOKEN)!= null)
		{
			return super.checkActive(TOKEN);
		}
		if(id != 0) {
			
			User user = userService.findById(id);
			if(user == null ) {
				getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "This User is not Found",notifications);
				return  ResponseEntity.status(404).body(getObjectResponse);

			}
			else {
				if(user.getAccountType()!= 1) {
					if(!userRoleService.checkUserHasPermission(id, "NOTIFICATION", "list")) {
						 getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user doesnot has permission to get notifications list",null);
						 logger.info("************************ getAllNotifications ENDED ***************************");
						return  ResponseEntity.badRequest().body(getObjectResponse);
					}
				}
				if(user.getDelete_date() == null) {
					
					userService.resetChildernArray();
				    if(user.getAccountType().equals(4)) {
					 
						 
						 List<Long>usersIds= new ArrayList<>();
						 usersIds.add(user.getId());
						 

						 Integer size = 0;
							
						 if(active == 0) {

							if(exportData.equals("exportData")) {
								notifications = notificationRepositorySFDA.getAllNotificationsDeactiveExport(usersIds,search);

							}
							else {
								notifications = notificationRepositorySFDA.getAllNotificationsDeactive(usersIds,offset,search);
								size = notificationRepositorySFDA.getAllNotificationsSizeDeactive(usersIds);
							}


						 }
						 
			             if(active == 2) {

			            	if(exportData.equals("exportData")) {
								notifications = notificationRepositorySFDA.getAllNotificationsAllExport(usersIds,search);

			            	}
			            	else {
								notifications = notificationRepositorySFDA.getAllNotificationsAll(usersIds,offset,search);
								size = notificationRepositorySFDA.getAllNotificationsSizeAll(usersIds);
			            	}

						 }
			             
			             if(active == 1) {
				            if(exportData.equals("exportData")) {
								notifications = notificationRepository.getAllNotificationsExport(usersIds,search);

				            }
				            else {

								notifications = notificationRepository.getAllNotifications(usersIds,offset,search);
								size = notificationRepository.getAllNotificationsSize(usersIds);
				            }

						 }
							
						 
                        getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "Success",notifications,size);
						logger.info("************************ getAllNotifications ENDED ***************************");
						return  ResponseEntity.ok().body(getObjectResponse);
						 
					 }
					 List<Long>usersIds= new ArrayList<>();
					 usersIds.add(id);

					 Integer size = 0;
					
					 if(active == 0) {
						 if(exportData.equals("exportData")) {
							notifications = notificationRepositorySFDA.getAllNotificationsDeactiveExport(usersIds,search);

						 }
						 else {
							notifications = notificationRepositorySFDA.getAllNotificationsDeactive(usersIds,offset,search);
							size = notificationRepositorySFDA.getAllNotificationsSizeDeactive(usersIds);
						 }
					 

					 }
					 
		             if(active == 2) {

		            	if(exportData.equals("exportData")) {
							notifications = notificationRepositorySFDA.getAllNotificationsAllExport(usersIds,search);

		            	}
		            	else {
							notifications = notificationRepositorySFDA.getAllNotificationsAll(usersIds,offset,search);
							size = notificationRepositorySFDA.getAllNotificationsSizeAll(usersIds);
		            	}
					 }
		             
		             if(active == 1) {
		            	if(exportData.equals("exportData")) {
							notifications = notificationRepository.getAllNotificationsExport(usersIds,search);

			            }
			            else {

							notifications = notificationRepository.getAllNotifications(usersIds,offset,search);
							size = notificationRepository.getAllNotificationsSize(usersIds);
			            }
					 }
					
					
					getObjectResponse= new GetObjectResponse(HttpStatus.OK.value(), "Success",notifications,size);
					logger.info("************************ getAllNotifications ENDED ***************************");
					return  ResponseEntity.ok().body(getObjectResponse);

				}
				else {
					getObjectResponse= new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "This User is not Found",notifications);
					return  ResponseEntity.status(404).body(getObjectResponse);

				}
				
			}

		}
		else{
			
			getObjectResponse= new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "User ID is Required",notifications);
			return  ResponseEntity.badRequest().body(getObjectResponse);

		}
	}
}
