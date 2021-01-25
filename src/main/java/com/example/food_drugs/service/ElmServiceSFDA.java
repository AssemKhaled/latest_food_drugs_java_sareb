package com.example.food_drugs.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ElmServiceSFDA {

	public ResponseEntity<?> statsInventories();
	public ResponseEntity<?> lastLocationsSFDA();

	public ResponseEntity<?> companyRegistrtaionSFDA(String TOKEN,Long userId,Long loggedUserId);
	public ResponseEntity<?> deviceRegistrtaionSFDA(String TOKEN,Long deviceId,Long userId);

	public ResponseEntity<?> warehouseRegistrtaion(String TOKEN,Long warehouseId,Long userId);
	public ResponseEntity<?> warehouseDelete(String TOKEN,Long warehouseId,Long userId);
	public ResponseEntity<?> warehouseUpdate(String TOKEN,Map<String, String> data,Long warehouseId,Long userId);

	public ResponseEntity<?> inventoryRegistrtaion(String TOKEN,Long inventoryId,Long userId);
	public ResponseEntity<?> inventoryUpdate(String TOKEN,Map<String, String> data,Long inventoryId,Long userId);
	public ResponseEntity<?> inventoryDelete(String TOKEN,Long InventoryId,Long userId);
	
	public ResponseEntity<?> deviceUpdateStoring(String TOKEN,Map<String, String> data,Long deviceId,Long userId);
	
}
