package com.example.food_drugs.service;

import com.example.examplequerydslspringdatajpamaven.responses.GetObjectResponse;
import com.example.food_drugs.dto.responses.GraphDataWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;

@Service
public interface DeviceServiceSFDA {

	public  ResponseEntity<?> activeDeviceSFDA(String TOKEN,Long userId, Long deviceId);
	public ResponseEntity<?>  getAllUserDevicesSFDA(String TOKEN,Long userId , int offset, String search, int active,String exportData);
	ResponseEntity<?> getDeviceGraphData(String TOKEN,Long userId) throws IOException;
	ResponseEntity<?> getDeviceGraphDataDashboard(String TOKEN,Long userId,int offset,int size);
	ResponseEntity<GetObjectResponse<GraphDataWrapper>> getDataForGraphByDeviceID(int deviceID);
	void startAndEndDate() throws ParseException;

}
