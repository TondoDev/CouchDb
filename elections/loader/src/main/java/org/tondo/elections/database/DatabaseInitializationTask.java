package org.tondo.elections.database;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.tondo.elections.VotesGenerator;
import org.tondo.elections.pojo.Region;
import org.tondo.elections.pojo.Vote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseInitializationTask implements Runnable {
	
	private Region region;
	private WebTarget target;
	private VotesGenerator generator;
	private InitializationConfig config;
	private ObjectMapper parser;
	
	public DatabaseInitializationTask(Region region, WebTarget target, VotesGenerator generator, ObjectMapper jsonParser, InitializationConfig config) {
		this.region = region;
		this.target = target;
		this.generator = generator;
		this.config = config;
		this.parser = jsonParser;
	}

	@Override
	public void run() {
		
		String regionDbName = normalizeForDbName(region.getName());
		try {
			target.path(regionDbName)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.delete()
					.close();
		} catch (ProcessingException e) {
			System.out.println("Can't delete database " + regionDbName + ", because " + e.getMessage());
			return;
		}
		
		Response createDbResponse = null;
		try {
			createDbResponse = target.path(regionDbName)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.put(Entity.json(""));	// put empty body for DB creation
		} catch (ProcessingException e) {
			System.out.println("Can't create database " + regionDbName + ", because " + e.getMessage());
			return;
		}
		
		if (createDbResponse.getStatus() != 201) {
			System.out.println("Can't create database " + regionDbName + ", because " + createDbResponse.readEntity(String.class));
		}
		// request are closed because after some requests (aprox. 30000) app get this exception
		//Exception in thread "pool-1-thread-1" Exception in thread "pool-1-thread-4" javax.ws.rs.ProcessingException: java.net.SocketException: No buffer space available (maximum connections reached?): connect
		createDbResponse.close();
		
		int numberOfVotes = (int)Math.ceil(region.getVotersCoef()*config.getBaseVotesCount());
		for (int i = 0; i < numberOfVotes && !Thread.interrupted(); i++) {
			Vote vote = generator.generateVote();
			
			try {
				target.path(regionDbName)
							.request()
							.post(Entity.entity(parser.writeValueAsString(vote), MediaType.APPLICATION_JSON))
							.close();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} 
		}
		
	}
	
	private static String normalizeForDbName(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		
		String[] nameParts = str.split("\\s+");
		String dbName = "";
		for (String part : nameParts) {
			dbName += part;
			
		}
		
		return Normalizer.normalize(dbName, Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").toLowerCase();
	}

}
