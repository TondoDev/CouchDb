package org.tondo.elections.database;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author TondoDev
 *
 */
public class CouchDbConnection {

	private ObjectMapper parser;
	private WebTarget target;
	
	public CouchDbConnection(WebTarget target, ObjectMapper mapper) {
		this.target = target;
		this.parser = mapper;
	}
	
	
	public CouchResult get(String resource) {
		Response response = null;
		try {
			response = target.path(resource)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
			
			return readResponse(response);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	public CouchResult put(String resource, Object value) throws JsonProcessingException {
		Response response = null;
		try {
			 response = target.path(resource)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.put(createJsonEntity(value));
			 
			return readResponse(response);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	public CouchResult post(String resource, Object value) throws JsonProcessingException {
		Response response = null;
		try {
			response = target.path(resource)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(createJsonEntity(value));
			return readResponse(response);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	public CouchResult delete(String resource) {
		Response response = null;
		try {
			response = target.path(resource)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.delete();
			
			return readResponse(response);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	private Entity<?> createJsonEntity(Object value) throws JsonProcessingException {
		// null can represent empty payload so empty string is send (jersey desn't accept null)
		String json = "";
		if (value instanceof String) {
			json = (String) value;
		} else if (value != null) {
			json = parser.writeValueAsString(value);
		}
		
		return Entity.entity(json, MediaType.APPLICATION_JSON);
	}
	
	private CouchResult readResponse(Response response) {
		String body = response.readEntity(String.class);
		Map<String, Object> bodyData = null;
		try {
			bodyData = parser.readValue(body, new TypeReference<Map<String, Object>>() {});
		} catch (JsonParseException e) {
			System.out.println("Payload parsing problem: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("I/O problem: " + e.getMessage());
		}
		
		return new CouchResult(response.getStatus(), bodyData);
	}
}
