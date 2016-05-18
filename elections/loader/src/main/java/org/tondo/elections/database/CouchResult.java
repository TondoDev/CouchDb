package org.tondo.elections.database;

import java.util.Map;

public class CouchResult {
	private int status;
	private Map<String, Object> payload;
	
	public CouchResult(int status, Map<String, Object> body) {
		this.status = status;
		this.payload = body;
	}
	
	public int getStatus() {
		return status;
	}
	
	public Map<String, Object> getPayload() {
		return payload;
	}
	
	

}
