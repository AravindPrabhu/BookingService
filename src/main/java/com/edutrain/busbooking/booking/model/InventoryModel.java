package com.edutrain.busbooking.booking.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class InventoryModel implements Serializable{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -3363714231433511881L;	
	
	private String busNo;
	private String availableSeats;
	private Date lastUpdtDate;
	
	public String getBusNo() {
		return busNo;
	}
	public void setBusNo(String busNo) {
		this.busNo = busNo;
	}
	public String getAvailableSeats() {
		return availableSeats;
	}
	public void setAvailableSeats(String availableSeats) {
		this.availableSeats = availableSeats;
	}
	public Date getLastUpdtDate() {
		return lastUpdtDate;
	}
	public void setLastUpdtDate(Date lastUpdtDate) {
		this.lastUpdtDate = lastUpdtDate;
	}

	
	
	
	

}
