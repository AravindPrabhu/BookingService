package com.edutrain.busbooking.booking.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.data.annotation.Id;

@Component
@Document("PassengerDtls")
public class PassengerModelWrapper {
	
	@Id
	private String passengerNo;
	private PassengerModel passengerModel;
	
	
	public String getPassengerNo() {
		return passengerNo;
	}
	public void setPassengerNo(String passengerNo) {
		this.passengerNo = passengerNo;
	}
	public PassengerModel getPassengerModel() {
		return passengerModel;
	}
	public void setPassengerModel(PassengerModel passengerModel) {
		this.passengerModel = passengerModel;
	}
	
	

}
