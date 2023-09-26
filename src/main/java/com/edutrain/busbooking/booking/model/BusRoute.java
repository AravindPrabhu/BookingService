package com.edutrain.busbooking.booking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component
@Document("Busroute")
public class BusRoute {

	@Id
	private String BusNo;
	private String Source;
	private String Destination;
	private String price;

	public String getBusNo() {
		return BusNo;
	}

	public void setBusNo(String busNo) {
		BusNo = busNo;
	}

	public String getSource() {
		return Source;
	}

	public void setSource(String source) {
		Source = source;
	}

	public String getDestination() {
		return Destination;
	}

	public void setDestination(String destination) {
		Destination = destination;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "BusRoute [BusNo=" + BusNo + ", Source=" + Source + ", Destination=" + Destination + ", price=" + price
				+ "]";
	}

}
