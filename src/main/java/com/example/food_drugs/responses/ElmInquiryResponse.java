package com.example.food_drugs.responses;

import lombok.*;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ElmInquiryResponse {
    private List body;
    private Long statusCode;
    private String message;

}
