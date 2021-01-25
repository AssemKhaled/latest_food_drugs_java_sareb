package com.example.food_drugs.entity;

import com.example.examplequerydslspringdatajpamaven.entity.CustomDeviceList;

public class CustomDeviceListSFDA extends CustomDeviceList{

	private int id;
	private String deviceName;
	private String uniqueId;
	private String sequenceNumber;
	private String lastUpdate;
	private String referenceKey;
	private String driverName;
	private String geofenceName;
	private String delete_date;
	private Long companyId;
	private Boolean expired;
	private String companyName;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public String getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public String getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public String getReferenceKey() {
		return referenceKey;
	}
	public void setReferenceKey(String referenceKey) {
		this.referenceKey = referenceKey;
	}
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public String getGeofenceName() {
		return geofenceName;
	}
	public void setGeofenceName(String geofenceName) {
		this.geofenceName = geofenceName;
	}
	public String getDelete_date() {
		return delete_date;
	}
	public void setDelete_date(String delete_date) {
		this.delete_date = delete_date;
	}
	public Long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	public Boolean getExpired() {
		return expired;
	}
	public void setExpired(Boolean expired) {
		this.expired = expired;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public CustomDeviceListSFDA() {
		
	}
	public CustomDeviceListSFDA(int id ,String deviceName,String uniqueId ,String sequenceNumber
			,String lastUpdate, String referenceKey , Boolean expired , String driverName ,
			String companyName, Long companyId,String geofenceName,String delete_date) {
		
		this.id = id;
		this.deviceName = deviceName;
		this.uniqueId = uniqueId;
		this.sequenceNumber = sequenceNumber;
		this.geofenceName = geofenceName;
		this.referenceKey = referenceKey;
		this.expired = expired;
		this.driverName = driverName;
	    this.lastUpdate = lastUpdate;
	    this.companyName = companyName;
	    this.companyId = companyId;
	    this.delete_date = delete_date;
		
		
	}

	
	
	

}
