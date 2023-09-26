package com.edutrain.busbooking.booking.model;


import org.springframework.stereotype.Component;


@Component
public class BookSeats {
	
	private String busNo;
	private String noOfSeats;
	private String passengerId;
	private String passengerName;
	
	public String getBusNo() {
		return busNo;
	}
	public void setBusNo(String busNo) {
		this.busNo = busNo;
	}
	public String getNoOfSeats() {
		return noOfSeats;
	}
	public void setNoOfSeats(String noOfSeats) {
		this.noOfSeats = noOfSeats;
	}
	public String getPassengerId() {
		return passengerId;
	}
	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}
	public String getPassengerName() {
		return passengerName;
	}
	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}
	
	
	

}
