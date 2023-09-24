package com.edutrain.busbooking.booking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component
@Document("BookingDtls")
public class BookingModelWrapper {
	
	@Id
	private String bookingNo;
	private BookingModel bookingModel;
	
	
	public String getBookingNo() {
		return bookingNo;
	}
	public void setBookingNo(String bookingNo) {
		this.bookingNo = bookingNo;
	}
	public BookingModel getBookingModel() {
		return bookingModel;
	}
	public void setBookingModel(BookingModel bookingModel) {
		this.bookingModel = bookingModel;
	}
	
	
	
	
	

}
