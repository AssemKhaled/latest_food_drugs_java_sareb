package com.example.food_drugs.entity;

import java.util.Date;

import javax.persistence.Id;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.examplequerydslspringdatajpamaven.entity.Attributes;

@Document(collection = "tc_positions")
public class Position extends Attributes{
	
	public Position(ObjectId _id) {
		super();
		this._id = _id;
//		this.protocol = protocol;
//		this.servertime = servertime;
		this.devicetime = devicetime;
//		this.fixtime = fixtime;
//		this.latitude = latitude;
//		this.longitude = longitude;
//		this.speed = speed;
		this.deviceid = deviceid;
	}

	public Position() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Id
	private ObjectId _id;
	
//	private String protocol;
//	
//	private Date servertime;
//	
	private Date devicetime;
//	
//	private Date fixtime;
//	
//	private Double latitude;
//	
//	private Double longitude;
//	
//	private Double speed;
//	
	private Long deviceid;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

//	public String getProtocol() {
//		return protocol;
//	}
//
//	public void setProtocol(String protocol) {
//		this.protocol = protocol;
//	}
//
//	public Date getServertime() {
//		return servertime;
//	}
//
//	public void setServertime(Date servertime) {
//		this.servertime = servertime;
//	}
//
	public Date getDevicetime() {
		return devicetime;
	}

	public void setDevicetime(Date devicetime) {
		this.devicetime = devicetime;
	}
//
//	public Date getFixtime() {
//		return fixtime;
//	}
//
//	public void setFixtime(Date fixtime) {
//		this.fixtime = fixtime;
//	}
//
//	public Double getLatitude() {
//		return latitude;
//	}
//
//	public void setLatitude(Double latitude) {
//		this.latitude = latitude;
//	}
//
//	public Double getLongitude() {
//		return longitude;
//	}
//
//	public void setLongitude(Double longitude) {
//		this.longitude = longitude;
//	}
//
//	public Double getSpeed() {
//		return speed;
//	}
//
//	public void setSpeed(Double speed) {
//		this.speed = speed;
//	}
//
	public Long getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(Long deviceid) {
		this.deviceid = deviceid;
	}

	
	
	

}
