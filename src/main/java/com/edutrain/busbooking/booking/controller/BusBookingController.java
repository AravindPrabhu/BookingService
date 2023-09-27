package com.edutrain.busbooking.booking.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.JMSException;


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
import com.fasterxml.jackson.databind.JsonMappingException;
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
	
	@Autowired
	private JmsTemplate jmsTemplate;

	public BusBookingController(BookingRepository bookingRepository,PassengerRepository passengerRepository) {
		this.bookingRepository = bookingRepository;
		this.passengerRepository = passengerRepository;
	}

	@PostMapping("bookseats")
	public String BookSeats(@RequestBody BookSeats bookSeats) {

		// call inventory service to get avaialable number of seats
		RestTemplate restTemplate = new RestTemplate();
		String inventoryUrl = "http://localhost:8791/inventory/getinventory/" + bookSeats.getBusNo();
		InventoryModel inventoryModel = restTemplate.getForObject(inventoryUrl, InventoryModel.class);

		if (Integer.parseInt(inventoryModel.getAvailableSeats()) >= Integer.parseInt(bookSeats.getNoOfSeats())) {
			
			Random rand = new Random();
			bookingModel.setBookingNo(String.valueOf(rand.nextInt(10000000)));
			System.out.println("Booking number in Booking controller"+bookingModel.getBookingNo());
			bookingModel.setBookingDate(java.time.LocalDate.now());
			System.out.println("Date  in Booking controller"+bookingModel.getBookingDate());
			bookingModel.setStatus("PENDING");
			bookingModel.setBusNo(bookSeats.getBusNo());			
			
			
			bookPayment.setBookingNo(bookingModel.getBookingNo());
			bookPayment.setBusNo(bookSeats.getBusNo());
			bookPayment.setNoOfSeats(bookSeats.getNoOfSeats());
			bookPayment.setPassengerId(bookSeats.getPassengerId());
			bookPayment.setPassengerName(bookSeats.getPassengerName());
			
			
			/*Get price from Admin Service */		
			
			String adminServiceUrl = "http://localhost:8731/busroutes/getbusroute/" + bookSeats.getBusNo();
			busRoute= restTemplate.getForObject(adminServiceUrl, BusRoute.class);
			bookPayment.setPrice(busRoute.getBusNo());	
			bookingModel.setSource(busRoute.getSource());
			bookingModel.setDestination(busRoute.getDestination());
			
			editBooking(bookingModel);
						
			//jmsMessagingTemplate.convertAndSend("BookingToPayment", bookPayment);
			
			/*
			 * jmsMessagingTemplate.convertAndSend("BookingToPayment",new MessageCreator() {
			 * 
			 * });
			 */
			
			sendMessage(bookPayment);

			return "Ticket Booking in progress";

		} else {
			return "Seats not available";
		}

		
	}
	
	
	public void sendMessage(final BookPayment bookPayment) {
		
		jmsTemplate.setDefaultDestinationName("BookingToPayment");
		jmsTemplate.send(new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {

				ObjectMessage objectMessage = session.createObjectMessage(bookPayment);
				return objectMessage;
			}
			
			
			
		});
		
		
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
			/*
			 * String BookingModelStr = "bookingNo: " + bookingModel.getBookingNo() +
			 * ",busNo: " + bookingModel.getBusNo() + ", bookingDate: " +
			 * bookingModel.getBookingDate() + " ,Source: " + bookingModel.getSource() +
			 * " ,Destination: " + bookingModel.getDestination() + " ,noOfSeats: " +
			 * bookingModel.getNoOfSeats() + " ,Status: " + bookingModel.getStatus();
			 */
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String jsonString;
			try {
				jsonString = ow.writeValueAsString(bookingModel);
				return jsonString;
			} catch (JsonProcessingException e) {
				
				e.printStackTrace();
				return "Exception occured";
			}					

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

	@JmsListener(destination = "InventoryToBooking")
	public String ReceiveBookingAndProcessPayment(Object obj) {

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Message Received" + obj);

		// Add business logic
		bookPayment = (BookPayment) obj;
		
		bookingModel.setStatus("CONFIRMED");
		/* Updating Booking confirmed Status */
		
		bookPayment.getPassengerId();
		bookPayment.getPassengerName();
		bookPayment.getBookingNo();
		
		

		return "Ticket Booked Successfullly";

	}
	
	
	@PostMapping("/addpassenger")
	public String addPassenger(@RequestBody PassengerModel passengerModel) {

		String bookingNo = passengerModel.getBookingNo();
		System.out.println("bookingNo in addPassenger is " + bookingNo);

		// BusData busData= new BusData();
		passengerModelWrapper.setBookingNo(bookingNo);
		passengerModelWrapper.setPassengerModel(passengerModel);

		try {
			PassengerModelWrapper retValue=null;
			try {
				retValue = passengerRepository.save(passengerModelWrapper);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (retValue != null) {
				return "Passenger Added successfully";
			} else {
				return "There is an error in adding Booking";
			}
		} catch (Exception e) {

			return "There is an error in adding Booking";
		}

	}

}
