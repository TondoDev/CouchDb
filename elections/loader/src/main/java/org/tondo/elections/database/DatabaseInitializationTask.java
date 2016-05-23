package org.tondo.elections.database;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import javax.ws.rs.ProcessingException;

import org.tondo.elections.VotesGenerator;
import org.tondo.elections.pojo.Region;
import org.tondo.elections.pojo.Vote;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DatabaseInitializationTask implements Runnable {
	
	private Region region;
	private VotesGenerator generator;
	private InitializationConfig config;
	private CouchDbConnection connection;
	
	public DatabaseInitializationTask(CouchDbConnection con, Region region, VotesGenerator generator, InitializationConfig config) {
		this.region = region;
		this.generator = generator;
		this.config = config;
		this.connection = con;
	}

	@Override
	public void run() {
		
		String regionDbName = normalizeForDbName(region.getName());
		
		try {
			this.connection.delete(regionDbName);
		} catch (ProcessingException e) {
			System.err.println("Can't delete database " + regionDbName + ", because " + e.getMessage());
			return;
		}
		
		try {
			CouchResult createDbResult = this.connection.put(regionDbName, null);
			if (createDbResult.getStatus() != 201) {
				System.err.println("Can't create database " + regionDbName + ", because " + createDbResult.getPayload());
			}
		} catch (ProcessingException | JsonProcessingException e) {
			System.err.println("Can't create database " + regionDbName + ", because " + e.getMessage());
			return;
		}
		
		int numberOfVotes = (int)Math.ceil(region.getVotersCoef()*config.getBaseVotesCount());
		for (int i = 0; i < numberOfVotes && !Thread.interrupted(); i++) {
			Vote vote = generator.generateVote();
			try {
				CouchResult voteResult = connection.post(regionDbName, vote);
				if (voteResult.getStatus() != 201) {
					System.err.println("Can't create vote " + regionDbName + ", because " + voteResult.getPayload());
				}
			} catch (ProcessingException | JsonProcessingException e) {
				System.err.println("Can't create vote " + regionDbName + ", because " + e.getMessage());
				return;
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
