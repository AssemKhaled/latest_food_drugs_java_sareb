package com.example.food_drugs.rest;


import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.food_drugs.entity.Inventory;
import com.example.food_drugs.service.InventoryServiceImpl;

@RestController
@RequestMapping(path = "/inventoriesSFDA")
@CrossOrigin
public class InventoryController {
	
	@Autowired
	InventoryServiceImpl inventoryServiceImpl;

	@GetMapping("/getInventoriesList")
	public ResponseEntity<?> getInventoriesList(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
		                                       @RequestParam (value = "exportData", defaultValue = "") String exportData,                                                                     
					                           @RequestParam (value = "userId",defaultValue = "0") Long userId,
											   @RequestParam(value = "offset", defaultValue = "0") int offset,
									           @RequestParam(value = "search", defaultValue = "") String search,
									           @RequestParam(value = "active", defaultValue = "1") int active) {
	 
		return inventoryServiceImpl.getInventoriesList(TOKEN,userId,offset,search,active,exportData);
		
	}
	
	@PostMapping(path ="/createInventories")
	public ResponseEntity<?> createInventories(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                              @RequestParam (value = "userId",defaultValue = "0") Long userId,
			                              @RequestBody(required = false) Inventory inventory) {
		
			 return inventoryServiceImpl.createInventories(TOKEN,inventory,userId);				
	}
	
	@RequestMapping(value = "/editInventories", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<?> editInventories(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                          @RequestBody(required = false) Inventory inventory,
			                                          @RequestParam (value = "userId", defaultValue = "0") Long id) {
		
		
		return inventoryServiceImpl.editInventories(TOKEN,inventory,id);

	}
	
	@RequestMapping(value = "/getInventoryById", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getInventoryById(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                               @RequestParam (value = "inventoryId", defaultValue = "0") Long inventoryId,
			                                               @RequestParam (value = "userId", defaultValue = "0") Long userId) {
		
    	return  inventoryServiceImpl.getInventoryById(TOKEN,inventoryId,userId);

	}
	
	@RequestMapping(value = "/deleteInventory", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> deleteInventory(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                            @RequestParam (value = "inventoryId", defaultValue = "0") Long inventoryId,
			                                            @RequestParam (value = "userId", defaultValue = "0") Long userId) {

    	return  inventoryServiceImpl.deleteInventory(TOKEN,inventoryId,userId);

	}
	
	@RequestMapping(value = "/activeInventory", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> activeInventory(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                            @RequestParam (value = "inventoryId", defaultValue = "0") Long inventoryId,
			                                            @RequestParam (value = "userId", defaultValue = "0") Long userId) {

    	return  inventoryServiceImpl.activeInventory(TOKEN,inventoryId,userId);

	}
	
	@GetMapping("/assignWarehouseToInventory")
	public ResponseEntity<?>assignWarehouseToInventory(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
											@RequestParam (value = "userId",defaultValue = "0") Long userId,
											@RequestParam (value = "inventoryId",defaultValue = "0") Long inventoryId,
											@RequestParam (value = "warehouseId",defaultValue = "0") Long warehouseId){
	
		if(warehouseId == 0) {
			return inventoryServiceImpl.removeWarehouseFromInventory(TOKEN,userId,inventoryId,warehouseId);
		}else {
			return inventoryServiceImpl.assignWarehouseToInventory(TOKEN,userId,inventoryId,warehouseId);
		}
		
	}
	
	@GetMapping("/getSelectedAndListWarehouse")
	public ResponseEntity<?>getSelectedAndListWarehouse(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
											@RequestParam (value = "userId",defaultValue = "0") Long userId,
											@RequestParam (value = "inventoryId",defaultValue = "0") Long inventoryId,
											@RequestParam (value = "loggedUserId",defaultValue = "0") Long loggedUserId){
	
		return inventoryServiceImpl.getSelectedAndListWarehouse(TOKEN,loggedUserId,userId,inventoryId);
		
		
	}
	
	@GetMapping("/getSelectListInventories")
	public ResponseEntity<?> getSelectListInventories(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
				                           @RequestParam (value = "userId",defaultValue = "0") Long userId) {
	 
		return inventoryServiceImpl.getSelectListInventories(TOKEN,userId);
		
	}

	@RequestMapping(value = "/getInventoryUnSelectOfClient", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getInventoryUnSelectOfClient(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
																	@RequestParam (value = "loggedUserId", defaultValue = "0") Long loggedUserId,                                             
																	@RequestParam (value = "userId", defaultValue = "0") Long userId) {
		
    	return  inventoryServiceImpl.getInventoryUnSelectOfClient(TOKEN,loggedUserId,userId);

	}
	
	@GetMapping("/assignClientInventories")
	public ResponseEntity<?> assignClientInventories(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
		                           			   @RequestParam (value = "loggedUserId", defaultValue = "0") Long loggedUserId,
											   @RequestParam (value = "userId", defaultValue = "0") Long userId,
											   @RequestParam (value = "inventoryIds", defaultValue = "0") Long [] inventoryIds) {
		return inventoryServiceImpl.assignClientInventories(TOKEN,loggedUserId,userId,inventoryIds);
	}
	
	@GetMapping("/getClientInventories")
	public ResponseEntity<?> getClientInventories(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
		                           			@RequestParam (value = "loggedUserId", defaultValue = "0") Long loggedUserId,
											@RequestParam (value = "userId", defaultValue = "0") Long userId) {
		return inventoryServiceImpl.getClientInventories(TOKEN,loggedUserId,userId);
	}
	

	@GetMapping("/getEasyCloudData")
    @Scheduled(fixedRate = 30000)
	public ResponseEntity<?> getEasyCloudData() {
		
		return inventoryServiceImpl.getEasyCloudData();
	}
	
	@GetMapping(path ="/getDataProtocols")
	public ResponseEntity<?> getDataProtocols(@RequestParam (value = "userId",defaultValue = "0") Long userId,
			                                  @RequestBody ArrayList<Map<Object,Object>> data,
			                                  @RequestParam (value = "type",defaultValue = "0") String type){
		
		return inventoryServiceImpl.getDataProtocols(data,type,userId);
	}
	
}
