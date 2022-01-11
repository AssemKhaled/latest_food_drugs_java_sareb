package com.example.food_drugs.rest.mobileRestSFDA;

import com.example.food_drugs.responses.MongoInventoryWrapper;
import com.example.food_drugs.responses.ResponseWrapper;
import com.example.food_drugs.responses.mobile.DeviceMonitoringResponse;
import com.example.food_drugs.responses.mobile.MonitoringDevicePositionResponse;
import com.example.food_drugs.service.mobile.Impl.MonitoringServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Component
@RestController
@RequestMapping(path = "/mobile/monitoring")
public class MobileMonitoringControllerSFDA {
    private final MonitoringServiceImpl monitoringService;
    public MobileMonitoringControllerSFDA(MonitoringServiceImpl monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping(path = "/devices/list")
    public ResponseWrapper<List<DeviceMonitoringResponse>> listDevices(@RequestHeader(value = "TOKEN") String TOKEN , @RequestParam(value = "userId") Long userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "size") int size){
        return monitoringService.monitoringDeviceList(TOKEN ,userId , offset , size);
    }

    @GetMapping(path = "/inventories/list")
    public ResponseWrapper<List<MongoInventoryWrapper>> listInventoriesLastInfo(@RequestHeader(value = "TOKEN") String TOKEN , @RequestParam(value = "userId") Long userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "search") String search){
        return monitoringService.monitoringGetAllInventoriesLastInfo(TOKEN ,userId , offset ,search);
    }

    @GetMapping(path = "/devices/position")
    public ResponseWrapper<MonitoringDevicePositionResponse> getDevicePosition(@RequestHeader(value = "TOKEN") String TOKEN ,
                                                                               @RequestParam(value = "deviceId") Long deviceId){
        return monitoringService.monitoringGetDevicePosition(TOKEN ,deviceId);
    }

}
