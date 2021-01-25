package com.example.food_drugs.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ReportServiceSFDA {

	
	public ResponseEntity<?> getInventoriesReport(String TOKEN,Long [] inventoryId,int offset,String start,String end,String search,Long userId,String exportData);
	public ResponseEntity<?> getWarehousesReport(String TOKEN,Long [] warehouseId,int offset,String start,String end,String search,Long userId,String exportData);
	public ResponseEntity<?> getNotificationReport(String TOKEN,Long [] inventoryId,Long [] warehouseId,int offset,String start,String end,String search,Long userId,String exportData);
	public ResponseEntity<?> getVehicleTempHum(String TOKEN,Long [] deviceId,Long [] groupId,int offset,String start,String end,String search,Long userId,String exportData);

}
