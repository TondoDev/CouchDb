package org.tondo.elections.database;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.tondo.elections.config.ElectionsConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author TondoDev
 *
 */
public class CouchDbConnectionFactory {
	
	private ObjectMapper jsonMapper;
	private WebTarget target;
	
	public CouchDbConnectionFactory(ElectionsConfiguration config) {
		this.jsonMapper = new ObjectMapper();
		
		String username =  config.getUserName();
		String passwrod = config.getPassword();
		boolean bothAuthNull = username == null && passwrod == null;
		boolean bothAuthPresent = username != null && passwrod != null;
		
		if (!bothAuthNull && !bothAuthPresent) {
			throw new IllegalArgumentException("Incorrectly provided authentication information!");
		}
		
		String host = config.getHost();
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("CouchDb host must be provided!");
		}
		
		int port = config.getPort();
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid port number provided!");
		}
		
		StringBuilder sb = new StringBuilder("http://")
				.append(config.getHost())
				.append(":").append(port);
		this.target =  ClientBuilder.newClient().target(sb.toString());
		
		if (!bothAuthNull) {
			this.target.register(HttpAuthenticationFeature.basic(username, passwrod));
		}
	}
	
	public CouchDbConnection getConnection() {
		return new CouchDbConnection(target, jsonMapper);
	}
}
