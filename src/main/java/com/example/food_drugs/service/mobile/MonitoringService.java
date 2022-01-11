package com.example.food_drugs.service.mobile;

import java.util.*;

import com.example.food_drugs.responses.MongoInventoryWrapper;
import com.example.food_drugs.responses.ResponseWrapper;
import com.example.food_drugs.responses.mobile.DeviceMonitoringResponse;
import com.example.food_drugs.responses.mobile.MonitoringDevicePositionResponse;


public interface MonitoringService {
    ResponseWrapper<List<DeviceMonitoringResponse>> monitoringDeviceList(String TOKEN , Long userId, int offset , int size);
    ResponseWrapper<MonitoringDevicePositionResponse> monitoringGetDevicePosition(String TOKEN , Long deviceId);
    ResponseWrapper<List<MongoInventoryWrapper>> monitoringGetAllInventoriesLastInfo(String TOKEN, Long userId, int offset,String search);

}
