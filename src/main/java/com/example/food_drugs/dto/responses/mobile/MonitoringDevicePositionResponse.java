package com.example.food_drugs.dto.responses.mobile;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class MonitoringDevicePositionResponse {
    Double speed ;
    Double temperature ;
    Double humidity ;
    Double latitude ;
    Double longitude ;
    String serverTime ;
    String status ;
    Double power;
    Boolean ignition;
    Long cooler;
    Boolean gpsStatus;
}
