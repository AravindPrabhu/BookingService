package com.edutrain.busbooking.booking.controller;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.edutrain.busbooking.booking.model.BookPayment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BookPaymentMessageConverter implements MessageConverter {

	@Autowired
	BookPayment bookPayment;

	@Override
	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
		// TODO Auto-generated method stub
		ObjectMapper mapper = new ObjectMapper();
		bookPayment = (BookPayment) object;
		String payload = null;
		try {
			payload = mapper.writeValueAsString(bookPayment);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TextMessage message = 
				
		//message.setText(payload);

		return null;
	}

	@Override
	public Object fromMessage(Message message) throws JMSException, MessageConversionException {
		// TODO Auto-generated method stub
		return null;
	}

}
