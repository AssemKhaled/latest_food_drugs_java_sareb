package com.example.food_drugs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PdfSummaryData {
	
	private Double average;
	
	private Double max;
	
	private Double min;
	
	private Integer totalLength;
	
	private Double mkt;
	
}
