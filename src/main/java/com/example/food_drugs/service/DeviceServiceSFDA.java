package com.example.food_drugs.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface DeviceServiceSFDA {

	public  ResponseEntity<?> activeDeviceSFDA(String TOKEN,Long userId, Long deviceId);
	public ResponseEntity<?>  getAllUserDevicesSFDA(String TOKEN,Long userId , int offset, String search, int active,String exportData);


}
