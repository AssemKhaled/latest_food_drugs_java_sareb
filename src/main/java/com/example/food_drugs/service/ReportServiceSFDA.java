package com.example.food_drugs.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.food_drugs.entity.TripDetailsRequest;

@Service
public interface ReportServiceSFDA {

	
	public ResponseEntity<?> getInventoriesReport(String TOKEN,Long [] inventoryId,int offset,String start,String end,String search,Long userId,String exportData);
	public ResponseEntity<?> getWarehousesReport(String TOKEN,Long [] warehouseId,int offset,String start,String end,String search,Long userId,String exportData);
	public ResponseEntity<?> getNotificationReport(String TOKEN,Long [] inventoryId,Long [] warehouseId,int offset,String start,String end,String search,Long userId,String exportData);
	public ResponseEntity<?> getVehicleTempHum(String TOKEN,Long [] deviceId,Long [] groupId,int offset,String start,String end,String search,Long userId,String exportData);

	public ResponseEntity<?> getviewTripDetails(String TOKEN,Long deviceId,String startTime,String endTime,String exportData,int offset);

	
	public ResponseEntity<?> getVehicleTempHumPDF(String TOKEN,Long deviceId,int offset,String start,String end,String search,Long userId,String exportData);
	public ResponseEntity<?>getTripPdfDetails(TripDetailsRequest request);
}
