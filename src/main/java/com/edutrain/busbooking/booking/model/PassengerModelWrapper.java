package com.edutrain.busbooking.booking.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.data.annotation.Id;

@Component
@Document("PassengerDtls")
public class PassengerModelWrapper {
	
	@Id
	private String bookingNo;
	private PassengerModel passengerModel;
	
	
	public String getBookingNo() {
		return bookingNo;
	}
	public void setBookingNo(String bookingNo) {
		this.bookingNo = bookingNo;
	}
	public PassengerModel getPassengerModel() {
		return passengerModel;
	}
	public void setPassengerModel(PassengerModel passengerModel) {
		this.passengerModel = passengerModel;
	}
	
	

}
