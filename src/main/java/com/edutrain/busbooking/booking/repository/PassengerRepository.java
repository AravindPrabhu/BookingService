package com.edutrain.busbooking.booking.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.edutrain.busbooking.booking.model.BookingModelWrapper;
import com.edutrain.busbooking.booking.model.PassengerModelWrapper;

@Repository
public interface PassengerRepository extends MongoRepository<PassengerModelWrapper,String> {

}
