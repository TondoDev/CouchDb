package org.tondo.elections.database;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import javax.ws.rs.ProcessingException;

import org.tondo.elections.VotesGenerator;
import org.tondo.elections.designdoc.DesignDocFileSystemLoader;
import org.tondo.elections.designdoc.DesignDocument;
import org.tondo.elections.pojo.Region;
import org.tondo.elections.pojo.Vote;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DatabaseInitializationTask implements Runnable {
	
	private Region region;
	private VotesGenerator generator;
	private int votesBase;
	private CouchDbConnection connection;
	
	public DatabaseInitializationTask(CouchDbConnection con, Region region, VotesGenerator generator, int votesBase) {
		this.region = region;
		this.generator = generator;
		this.votesBase = votesBase;
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
		
		int numberOfVotes = (int)Math.ceil(region.getVotersCoef()*this.votesBase);
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
		
		String dbDesignLocation = Paths.get("..", "design", regionDbName).toString();
		List<DesignDocument> designDocs = null;
		try {
			designDocs = new DesignDocFileSystemLoader(dbDesignLocation).load();
		} catch (IOException e) {
			System.err.println("Can't load design docs for database " + regionDbName + ", because " + e.getMessage());
		}
		
		if (designDocs != null) {
			for (DesignDocument doc : designDocs) {
				try {
					CouchResult designCreateResult = connection.put(regionDbName + "/_design/" + doc.getName(), doc.getContent());
					if (designCreateResult.getStatus() != 201) {
						System.err.println("Can't create design document " + doc.getName() + " in database " + regionDbName + ", because " + designCreateResult.getPayload());
					}
				} catch (ProcessingException| JsonProcessingException e) {
					System.err.println("Can't create design document " + doc.getName() + " in database " + regionDbName + ", because " + e.getMessage());
				}
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
