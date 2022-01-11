package com.example.food_drugs.service.mobile.Impl;

import com.example.food_drugs.dto.AttributesWrapper;

import com.example.food_drugs.entity.MonogoInventoryLastData;
import com.example.food_drugs.entity.Position;
import com.example.food_drugs.helpers.ResponseHandler;
import com.example.food_drugs.helpers.UserHelper;
import com.example.food_drugs.repository.DeviceRepositorySFDA;
import com.example.food_drugs.repository.InventoryRepository;
import com.example.food_drugs.repository.MongoInventoryLastDataRepository;
import com.example.food_drugs.repository.PositionMongoSFDARepository;

import com.example.food_drugs.responses.InventorySummaryDataWrapper;
import com.example.food_drugs.responses.MongoInventoryWrapper;
import com.example.food_drugs.responses.ResponseWrapper;
import com.example.food_drugs.responses.mobile.DeviceMonitoringResponse;
import com.example.food_drugs.helpers.DeviceHelper;
import com.example.food_drugs.responses.mobile.MonitoringDevicePositionResponse;
import com.example.food_drugs.service.mobile.MonitoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Service;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class MonitoringServiceImpl implements MonitoringService {



    private final InventoryRepository inventoryRepository;
    private final MongoInventoryLastDataRepository mongoInventoryLastDataRepository;

    private final UserHelper userHelper;
    private final PositionMongoSFDARepository positionMongoSFDARepository;
    private final DeviceHelper deviceHelper;
    private final DeviceRepositorySFDA deviceRepositorySFDA;
    private static final Log logger = LogFactory.getLog(MonitoringServiceImpl.class);

    public MonitoringServiceImpl(InventoryRepository inventoryRepository, MongoInventoryLastDataRepository mongoInventoryLastDataRepository, UserHelper userHelper, PositionMongoSFDARepository positionMongoSFDARepository,
                                 DeviceHelper deviceHelper, DeviceRepositorySFDA deviceRepositorySFDA){
        this.inventoryRepository = inventoryRepository;
        this.mongoInventoryLastDataRepository = mongoInventoryLastDataRepository;
        this.userHelper = userHelper;
        this.positionMongoSFDARepository = positionMongoSFDARepository;
        this.deviceHelper = deviceHelper;
        this.deviceRepositorySFDA = deviceRepositorySFDA;
    }


    @Override
    public ResponseWrapper<List<DeviceMonitoringResponse>> monitoringDeviceList(String TOKEN,Long userId , int offset , int size) {
        logger.info("******************** monitoringDeviceList Service Started ********************");
        ResponseHandler<List<DeviceMonitoringResponse>> responseHandler = new ResponseHandler<>();
        ResponseWrapper<List<DeviceMonitoringResponse>> userResponseWrapper = userHelper.userErrorsChecker(TOKEN,userId);
        if(!userResponseWrapper.getSuccess()){
            return userResponseWrapper;
        }

        try {
            List<Long> userIds = userHelper.getUserChildrenId(userId) ;
            List<Object[]> deviceMonitoringList = deviceRepositorySFDA.getDeviceByUserIdsWithLimitAndSize(userIds ,offset ,size);
            List<DeviceMonitoringResponse> deviceMonitoringResponses = new ArrayList<>();
            int dataSize = deviceRepositorySFDA.countDeviceByUserIds(userIds);
            if(deviceMonitoringList.size()<1){
                return responseHandler.reportError("Error");
            }
            for(Object[] device : deviceMonitoringList){
             try {
                 DecimalFormat decimalFormat = new DecimalFormat(".###");
                 AttributesWrapper attributes = new ObjectMapper().readValue((String) device[5],AttributesWrapper.class);
                    Position position = positionMongoSFDARepository.findOne((String) device[6]);
                    deviceMonitoringResponses.add(DeviceMonitoringResponse
                            .builder()
                                    .deviceName((String) device[0])
                                    .id((Integer) device[1])
                                    .lastUpdate(device[2].toString())
                                    .lastTemp(device[3]!=null?Double.parseDouble(decimalFormat.format(device[3])):300)
                                    .lastHum(device[4]!=null?Double.parseDouble(decimalFormat.format(device[4])):300)
                                    .storingCategory(deviceHelper.StoringCategoryConditionDetector(attributes.getStoringCategory()))
                                    .speed(position.getSpeed()!=null?Double.parseDouble(decimalFormat.format(position.getSpeed())):-1)
                                    .ignition(position.getAttributes().get("ignition") != null ? (Boolean) position.getAttributes().get("ignition") : false)
                                    .power(position.getAttributes().get("power")!=null?Double.parseDouble(decimalFormat.format(position.getAttributes().get("power"))):-1)
                                    .cooler(position.getAttributes().get("AC")!=null ? (Long) position.getAttributes().get("AC") : 300)
                                    .status(deviceHelper.deviceStatuesDetector(position.getServertime()))
                                    .gpsStatus(deviceHelper.deviceGPSDetector(position))
                            .build());
                 }catch (Error | Exception e){
                    responseHandler.errorLogger("Fail To Map SQL Data To DeviceMonitoringResponse In case :  " + Arrays.toString(device)
                            +" \n Position : " + device[6].toString());
             }
            }
            logger.info("******************** monitoringDeviceList Service Ended With Success ********************");
            return responseHandler.reportSuccess("Success", deviceMonitoringResponses,dataSize);
        } catch (Error | Exception e) {
            logger.info("******************** monitoringDeviceList Service Started With Error "+e.getMessage()+" ********************");
            return responseHandler.reportError(e.getMessage());
        }
    }

    @Override
    public ResponseWrapper<MonitoringDevicePositionResponse> monitoringGetDevicePosition(String TOKEN, Long deviceId) {

        ResponseHandler<MonitoringDevicePositionResponse> responseHandler = new ResponseHandler<>();
        logger.info("******************** monitoringGetDevicePosition Service Started ********************");
        ResponseWrapper<MonitoringDevicePositionResponse> userResponseWrapper = userHelper.userTokenErrorsChecker(TOKEN);
        if(!userResponseWrapper.getSuccess()){
            return userResponseWrapper;
        }
        try {
            DecimalFormat decimalFormat = new DecimalFormat(".###");
            Optional<Position> lastPositionOptional = positionMongoSFDARepository.findFirstByDeviceidOrderByServertimeDesc(deviceId);

            if(!lastPositionOptional.isPresent()){
                return responseHandler.reportError("Position Not Found With Id : "+deviceId);
            }
            Position lastPosition = lastPositionOptional.get();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return responseHandler.reportSuccess("Success",
                    MonitoringDevicePositionResponse
                            .builder()
                            .speed(Double.parseDouble(decimalFormat.format(lastPosition.getSpeed())))
                            .latitude(lastPosition.getLatitude())
                            .longitude(lastPosition.getLongitude())
                            .humidity(deviceHelper.findValueFromMap(lastPosition.getAttributes(),"hum"))
                            .temperature(deviceHelper.findValueFromMap(lastPosition.getAttributes(),"temp"))
                            .serverTime(simpleDateFormat.format(lastPosition.getServertime()))
                            .status(deviceHelper.deviceStatuesDetector(lastPosition.getServertime()))
                            .ignition(lastPosition.getAttributes().get("ignition") != null ? (Boolean) lastPosition.getAttributes().get("ignition") : false)
                            .power(lastPosition.getAttributes().get("power")!=null?Double.parseDouble(decimalFormat.format(lastPosition.getAttributes().get("power"))):-1)
                            .cooler(lastPosition.getAttributes().get("AC")!=null ? (Long) lastPosition.getAttributes().get("AC") : 300)
                            .gpsStatus(deviceHelper.deviceGPSDetector(lastPosition))
                            .build());
        }catch (Error | Exception e){
            logger.info("******************** monitoringGetDevicePosition Service Started With Error "+e.getMessage()+" ********************");
            return responseHandler.reportError(e.getMessage());
        }
    }

    @Override
    public ResponseWrapper<List<MongoInventoryWrapper>> monitoringGetAllInventoriesLastInfo(String TOKEN, Long userId, int offset, String search) {
        logger.info("************************ monitoringGetAllInventoriesLastInfo STARTED ***************************");
        ResponseHandler<List<MongoInventoryWrapper>>  responseHandler = new ResponseHandler<>();
        ResponseWrapper<List<MongoInventoryWrapper>> userResponseWrapper = userHelper.userErrorsChecker(TOKEN,userId);
        if(!userResponseWrapper.getSuccess()){
            return userResponseWrapper;
        }
        try {
            List<MongoInventoryWrapper> inventoryLastData =  new ArrayList<>();
            List<Long>usersIds= userHelper.getUserChildrenId(userId);
            List<InventorySummaryDataWrapper> allInventoriesSumDataFromMySQL = inventoryRepository.getAllInventoriesSummaryData(usersIds,offset);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            DecimalFormat df = new DecimalFormat("#.##");
            for (InventorySummaryDataWrapper inventorySummaryWrapper : allInventoriesSumDataFromMySQL){
                if(inventorySummaryWrapper.getLastDataId()!=null){
                    MonogoInventoryLastData mongoInv = mongoInventoryLastDataRepository.findById(inventorySummaryWrapper.getName());
                    if(mongoInv!=null){
                        inventoryLastData.add(
                                MongoInventoryWrapper
                                        .builder()
                                        ._id(mongoInv.get_id())
                                        .temperature(Double.valueOf(df.format(mongoInv.getTemperature())))
                                        .inventoryId(mongoInv.getInventoryId())
                                        .inventoryName(inventorySummaryWrapper.getLastDataId())
                                        .createDate(simpleDateFormat.format(mongoInv.getCreateDate()))
                                        .humidity(Double.valueOf(df.format(mongoInv.getHumidity())))
                                        .build());
                    }
                }
            }

            Integer size=inventoryRepository.getInventoriesSize(usersIds);
            if(inventoryLastData.size()>0 && Pattern.matches(".*\\S.*" , search)){
                inventoryLastData = inventoryLastData.stream().filter(inventoryLastDates ->
                        inventoryLastDates.getInventoryName().contains(search)).collect(Collectors.toList());
            }
            logger.info("************************ monitoringGetAllInventoriesLastInfo ENDED SUCCESS ***************************");
            return responseHandler.reportSuccess("Success",inventoryLastData,size);
        }catch (Exception | Error e){
            logger.info("************************ monitoringGetAllInventoriesLastInfo ENDED With ERROR ***************************");
            return responseHandler.reportError(e.getMessage());
        }


    }

}