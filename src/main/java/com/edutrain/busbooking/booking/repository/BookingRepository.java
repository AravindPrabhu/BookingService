package com.edutrain.busbooking.booking.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.edutrain.busbooking.booking.model.BookingModelWrapper;

@Repository
public interface BookingRepository extends MongoRepository<BookingModelWrapper,String> {

}
