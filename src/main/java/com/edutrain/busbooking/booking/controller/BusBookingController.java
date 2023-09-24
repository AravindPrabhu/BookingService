package com.edutrain.busbooking.booking.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutrain.busbooking.booking.model.BookingModel;
import com.edutrain.busbooking.booking.model.BookingModelWrapper;
import com.edutrain.busbooking.booking.repository.BookingRepository;

@RestController
@RequestMapping("/booking")
public class BusBookingController {

	@Autowired
	BookingModel bookingModel;

	@Autowired
	BookingModelWrapper bookingModelWrapper;

	@Autowired
	private final BookingRepository bookingRepository;

	public BusBookingController(BookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	@GetMapping("/getallbookings")
	public List<String> getAllBookings() {

		List<BookingModel> bookingList = new ArrayList<BookingModel>();
		List<String> stringRouteList = new ArrayList<String>();

		bookingRepository.findAll().forEach((bookingModelWrapper) -> {
			bookingList.add(bookingModelWrapper.getBookingModel());
		});

		bookingList.forEach((bookingModel) -> {
			stringRouteList.add("bookingNo: " + bookingModel.getBookingNo() + ",busNo: " + bookingModel.getBusNo()
					+ ", bookingDate: " + bookingModel.getBookingDate() + " ,Source: " + bookingModel.getSource()
					 + " ,Destination: " + bookingModel.getDestination() + " ,noOfSeats: " + bookingModel.getNoOfSeats()
					 + " ,Status: " + bookingModel.getStatus());
		});

		return stringRouteList;

	}

	@PostMapping("/addbooking")
	public String addBooking(@RequestBody BookingModel bookingModel) {

		String bookingNo = bookingModel.getBookingNo();
		System.out.println("bookingNo in addBooking is " + bookingNo);

		// BusData busData= new BusData();
		bookingModelWrapper.setBookingNo(bookingNo);
		bookingModelWrapper.setBookingModel(bookingModel);

		try {
			BookingModelWrapper retValue = bookingRepository.save(bookingModelWrapper);

			if (retValue != null) {
				return "Booking Added successfully";
			} else {
				return "There is an error in adding Booking";
			}
		} catch (Exception e) {

			return "There is an error in adding Booking";
		}

	}

	@GetMapping("/getbooking/{bookingNo}")
	public String getBooking(@PathVariable String bookingNo) {

		Optional<BookingModelWrapper> bookingModelWrapperRetVal = bookingRepository.findById(bookingNo);

		if (bookingModelWrapperRetVal.isPresent()) {

			bookingModelWrapper = bookingModelWrapperRetVal.get();
			bookingModel = bookingModelWrapper.getBookingModel();
			String BookingModelStr = "bookingNo: " + bookingModel.getBookingNo() + ",busNo: " + bookingModel.getBusNo()
			+ ", bookingDate: " + bookingModel.getBookingDate() + " ,Source: " + bookingModel.getSource()
			 + " ,Destination: " + bookingModel.getDestination() + " ,noOfSeats: " + bookingModel.getNoOfSeats()
			 + " ,Status: " + bookingModel.getStatus();

			return BookingModelStr;

		} else {

			return "Booking Not found";
		}

	}

	@DeleteMapping("/deletebooking/{bookingNo}")
	public String deleteBusRoute(@PathVariable String bookingNo) {
		try {
			bookingRepository.deleteById(bookingNo);
			return "Route Deleted successfully";
		} catch (Exception e) {
			return "Error while deletion";
		}

	}

	@PutMapping("/editBooking")
	public String editBooking(@RequestBody BookingModel bookingModel) {

		String bookingNo = bookingModel.getBookingNo();
		System.out.println("bookingNo in editBooking is " + bookingNo);
		String RetValue = getBooking(bookingNo);

		if (RetValue.equalsIgnoreCase("Booking Not found")) {
			return "Booking Not found,Please enter valid booking";
		} else {

			// BusData busData= new BusData();
			bookingModelWrapper.setBookingNo(bookingNo);
			bookingModelWrapper.setBookingModel(bookingModel);

			try {
				BookingModelWrapper retValue = bookingRepository.save(bookingModelWrapper);

				if (retValue != null) {
					return "Booking Updated successfully";
				} else {
					return "There is an error in updating Booking";
				}
			} catch (Exception e) {

				return "There is an error in updating  Booking";
			}
		}

	}

}
