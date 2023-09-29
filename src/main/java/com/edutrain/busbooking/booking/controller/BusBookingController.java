package com.edutrain.busbooking.booking.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.edutrain.busbooking.booking.model.BookPayment;
import com.edutrain.busbooking.booking.model.BookSeats;
import com.edutrain.busbooking.booking.model.BookingModel;
import com.edutrain.busbooking.booking.model.BookingModelWrapper;
import com.edutrain.busbooking.booking.model.BusRoute;
import com.edutrain.busbooking.booking.model.InventoryModel;
import com.edutrain.busbooking.booking.model.PassengerModel;
import com.edutrain.busbooking.booking.model.PassengerModelWrapper;
import com.edutrain.busbooking.booking.repository.BookingRepository;
import com.edutrain.busbooking.booking.repository.PassengerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


@RestController
@RequestMapping("/booking")
public class BusBookingController {

	@Autowired
	BookingModel bookingModel;

	@Autowired
	BookingModelWrapper bookingModelWrapper;

	@Autowired
	PassengerModel passengerModel;

	@Autowired
	PassengerModelWrapper passengerModelWrapper;

	@Autowired
	BookPayment bookPayment;

	@Autowired
	private final BookingRepository bookingRepository;

	@Autowired
	private final PassengerRepository passengerRepository;

	@Autowired
	BookSeats bookSeats;

	@Autowired
	BusRoute busRoute;

	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;

	@LoadBalanced
	RestTemplate loadBalanced = new RestTemplate();

	@Autowired
	private JmsTemplate jmsTemplate;
	
	private static final Logger LOGGER = LogManager.getLogger(BusBookingController.class);

	public BusBookingController(BookingRepository bookingRepository, PassengerRepository passengerRepository) {
		this.bookingRepository = bookingRepository;
		this.passengerRepository = passengerRepository;

	}

	@PostMapping("bookseats")
	public String BookSeats(@RequestBody BookSeats bookSeats) {

		LOGGER.info("Inside BookSeats");
		// call inventory service to get avaialable number of seats

		String inventoryUrl = "http://localhost:8791/inventory/getinventory/" + bookSeats.getBusNo();
		Object retObj = loadBalanced.getForObject(inventoryUrl, InventoryModel.class);

		if (retObj.getClass().equals(String.class)) {
			return retObj.toString();
		}

		InventoryModel inventoryModel = (InventoryModel) retObj;

		if (Integer.parseInt(inventoryModel.getAvailableSeats()) >= Integer.parseInt(bookSeats.getNoOfSeats())) {

			Random rand = new Random();
			bookingModel.setBookingNo(String.valueOf(rand.nextInt(10000000)));
			LOGGER.debug("Booking number in Booking controller" + bookingModel.getBookingNo());
			LocalDate lDate= java.time.LocalDate.now();
			Date  bookingDate = Date.from(lDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			bookingModel.setBookingDate(bookingDate);
			LOGGER.debug("Date  in Booking controller" + bookingModel.getBookingDate());
			bookingModel.setStatus("PENDING");
			bookingModel.setBusNo(bookSeats.getBusNo());

			bookPayment.setBookingNo(bookingModel.getBookingNo());
			bookPayment.setBusNo(bookSeats.getBusNo());
			bookPayment.setNoOfSeats(bookSeats.getNoOfSeats());
			bookPayment.setPassengerId(bookSeats.getPassengerId());
			bookPayment.setPassengerName(bookSeats.getPassengerName());

			/* Get price from Admin Service */

			String adminServiceUrl = "http://localhost:8731/busroutes/getbusroute/" + bookSeats.getBusNo();
			busRoute = loadBalanced.getForObject(adminServiceUrl, BusRoute.class);
			bookPayment.setPrice(busRoute.getBusNo());
			bookingModel.setSource(busRoute.getSource());
			bookingModel.setDestination(busRoute.getDestination());

			String retValue = addBooking(bookingModel);
			
			LOGGER.debug("Return value from Addboking is "+retValue);

			sendMessage(bookPayment);

			return "Ticket Booking in progress";

		} else {
			return "Seats not available";
		}

	}

	public void sendMessage(final BookPayment bookPayment) {

		LOGGER.info("Inside sendMessage");
		
		String bookPaymentStr = null;
		try {
			ObjectWriter objWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
			bookPaymentStr = objWriter.writeValueAsString(bookPayment);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception occured"+e.getMessage());
			e.printStackTrace();
		}

		LOGGER.debug(" BookPayment  " + bookPaymentStr);		

		LOGGER.info("New approach-Object");
		jmsTemplate.convertAndSend("BookingToPayment", bookPaymentStr);	

	}

	@GetMapping("/getallbookings")
	public List<String> getAllBookings() {
		
		LOGGER.info("In getAllBookings");

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
		
		LOGGER.info("In addBooking");

		String bookingNo = bookingModel.getBookingNo();
		LOGGER.debug("bookingNo in addBooking is " + bookingNo);

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
			LOGGER.error("Exception occured in addBooking"+e.getMessage());
			return "There is an error in adding Booking";
		}

	}

	@GetMapping("/getbooking/{bookingNo}")
	public String getBooking(@PathVariable String bookingNo) {
		
		LOGGER.info("In getBooking");

		Optional<BookingModelWrapper> bookingModelWrapperRetVal = bookingRepository.findById(bookingNo);

		if (bookingModelWrapperRetVal.isPresent()) {

			bookingModelWrapper = bookingModelWrapperRetVal.get();
			bookingModel = bookingModelWrapper.getBookingModel();
	
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String jsonString;
			try {
				jsonString = ow.writeValueAsString(bookingModel);
				return jsonString;
			} catch (JsonProcessingException e) {
				LOGGER.error("Exception occured in getBooking");
				e.printStackTrace();
				return "Exception occured";
			}

		} else {

			return "Booking Not found";
		}

	}

	@DeleteMapping("/deletebooking/{bookingNo}")
	public String deleteBusRoute(@PathVariable String bookingNo) {
		LOGGER.info("In deleteBusRoute");
		try {
			bookingRepository.deleteById(bookingNo);
			return "Route Deleted successfully";
		} catch (Exception e) {
			return "Error while deletion";
		}

	}

	@PutMapping("/editBooking")
	public String editBooking(@RequestBody BookingModel bookingModel) {
		
		LOGGER.info("In editBooking");

		String bookingNo = bookingModel.getBookingNo();
		LOGGER.debug("bookingNo in editBooking is " + bookingNo);
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

				LOGGER.error("Exception occured in editBooking "+e.getMessage());
				return "There is an error in updating  Booking";
			}
		}

	}

	@JmsListener(destination = "InventoryToBooking")
	public void ReceiveBookingAndProcessPayment(String  bookPaymentStr) {
		
		LOGGER.info("In ReceiveBookingAndProcessPayment");

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOGGER.error("Exception occured in ReceiveBookingAndProcessPayment"+e.getMessage());
		}

		LOGGER.debug("Message Received" + bookPaymentStr);
		try {
			bookPayment =  new ObjectMapper().readValue(bookPaymentStr, BookPayment.class);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception occured in  ReceiveBookingAndProcessPayment"+e.getMessage()); 
			e.printStackTrace();
		}
		
		LOGGER.debug("bookPayment object  Received" + bookPayment);
		
		try {
			bookingModel = new ObjectMapper().readValue(getBooking(bookPayment.getBookingNo()),BookingModel.class);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception occured in  ReceiveBookingAndProcessPayment"+e.getMessage());
			e.printStackTrace();
		}

		
		bookingModel.setStatus("CONFIRMED");
		/* Updating Booking confirmed Status */
		
		editBooking(bookingModel);		
		
		passengerModel.setPassengerId(bookPayment.getPassengerId());
		passengerModel.setPassengerName(bookPayment.getPassengerName());
		passengerModel.setBookingNo(bookPayment.getBookingNo());
		
		addPassenger(passengerModel);
		
		

	}

	@PostMapping("/addpassenger")
	public String addPassenger(@RequestBody PassengerModel passengerModel) {

		LOGGER.info("In ReceiveBookingAndProcessPayment");
		String bookingNo = passengerModel.getBookingNo();
		LOGGER.debug("bookingNo in addPassenger is " + bookingNo);

		// BusData busData= new BusData();
		passengerModelWrapper.setBookingNo(bookingNo);
		passengerModelWrapper.setPassengerModel(passengerModel);

		try {
			PassengerModelWrapper retValue = null;
			try {
				retValue = passengerRepository.save(passengerModelWrapper);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Exception occured in  addPassenger"+e.getMessage());
				e.printStackTrace();
			}

			if (retValue != null) {
				return "Passenger Added successfully";
			} else {
				return "There is an error in adding Booking";
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured in  addPassenger"+e.getMessage());
			return "There is an error in adding Booking";
		}

	}

}
