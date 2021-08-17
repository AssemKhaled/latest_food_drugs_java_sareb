package com.example.food_drugs.responses;

import com.example.examplequerydslspringdatajpamaven.responses.AlarmSectionWrapperResponse;
import lombok.*;
import java.util.*;
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class AlarmsReportResponseWrapper {
    List<AlarmSectionWrapperResponse> alarmsSection ;
    List<GraphDataWrapper> graphDataWrapperList ;
}
