package org.tondo.elections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.tondo.elections.database.CouchDbConnection;
import org.tondo.elections.database.DatabaseInitializationTask;
import org.tondo.elections.database.InitializationConfig;
import org.tondo.elections.pojo.Member;
import org.tondo.elections.pojo.Party;
import org.tondo.elections.pojo.Region;
import org.tondo.elections.pojo.Vote;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author TondoDev
 *
 */
public class Elections {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ObjectMapper jsonParser = new ObjectMapper();
		
		// load parties
		List<Party> parties = null;
		try (InputStream is = new FileInputStream("../parties.json")) {
			parties = jsonParser.readValue(is, new TypeReference<List<Party>>() {
			});
		}
		VotesGenerator votesGenerator = new VotesGenerator(parties);
		
		// load regions
		List<Region> regions = null;
		try (InputStream is = new FileInputStream("../regions.json")) {
			regions = jsonParser.readValue(is, new TypeReference<List<Region>>() {});
		}
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://127.0.0.1:5984")
				.register(HttpAuthenticationFeature.basic("tondodev", "tondodev"));
		CouchDbConnection connection = new CouchDbConnection(target, jsonParser);
		InitializationConfig config = new InitializationConfig();
		config.setBaseVotesCount(2000);
		
		ExecutorService dbInitExecutor = Executors.newCachedThreadPool();
		for (Region region : regions) {
			dbInitExecutor.execute(new DatabaseInitializationTask(connection, region, votesGenerator, config));
		}
		
		// needed for awaitTerminantion, because used pool is cached, and idle threads 
		// are terminated after 60 seconds
		dbInitExecutor.shutdown();
		try {
			System.out.println("waiting for termination!");
			boolean normalTermination = dbInitExecutor.awaitTermination(100, TimeUnit.SECONDS);
			System.out.println("Terminated! " + normalTermination);
			if(!normalTermination) {
				dbInitExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			// nothing to do
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
	
	
	public void proto() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper jsonParser = new ObjectMapper();
		
		// load regions
		List<Region> regions = null;
		try (InputStream is = new FileInputStream("../regions.json")) {
			regions = jsonParser.readValue(is, new TypeReference<List<Region>>() {});
		}
		
		// for testing create DB only for one region
		//regions.retainAll(Arrays.asList(regions.get(0)));
		
		// load parties
		List<Party> parties = null;
		try (InputStream is = new FileInputStream("../parties.json")) {
			parties = jsonParser.readValue(is, new TypeReference<List<Party>>() {});
		}
		
		VotesGenerator votesGenerator = new VotesGenerator(parties);
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://127.0.0.1:5984");
		for (Region r : regions) {
			String regionDbName = normalizeForDbName(r.getName());
			Response deleteResponse = null;
			try {
				deleteResponse = target.path(regionDbName)
						.register(HttpAuthenticationFeature.basic("tondodev", "tondodev"))
						.request()
						.accept(MediaType.APPLICATION_JSON)
						.delete();
			} catch (ProcessingException e) {
				System.out.println("Can't delete database " + regionDbName + ", because " + e.getMessage());
				break;
			}
			
			System.out.println("delete status: " + deleteResponse.getStatus());
			System.out.println("delete response: " + deleteResponse.readEntity(String.class));
			
			Response createDbResponse = null;
			try {
				createDbResponse = target.path(regionDbName)
						.register(HttpAuthenticationFeature.basic("tondodev", "tondodev")).request()
						.accept(MediaType.APPLICATION_JSON).put(Entity.json(""));
			} catch (ProcessingException e) {
				System.out.println("Can't create database " + regionDbName + ", because " + e.getMessage());
				break;
			}

			System.out.println("create status: " + createDbResponse.getStatus());
			System.out.println("create response: " + createDbResponse.readEntity(String.class));
			
			
			for (int voteCounter = 0; voteCounter < 10; voteCounter++) {
				Vote vote = votesGenerator.generateVote();
				Response voteResponse = null;
				
				voteResponse = target.path(regionDbName)
							.register(HttpAuthenticationFeature.basic("tondodev", "tondodev"))
							.request()
							.post(Entity.entity(jsonParser.writeValueAsString(vote), MediaType.APPLICATION_JSON));
				
				System.out.println("vote status: " + voteResponse.getStatus());
				System.out.println("vote response: " + voteResponse.readEntity(String.class));
			}
		}
	}
	
	public void tmp() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://tondodev:tonadodev@127.0.0.1:5984");
		Response body = target.path("_all_dbs")
			.request()
			.accept(MediaType.APPLICATION_JSON)
			.get();
		
		System.out.println(body.getStatusInfo().getStatusCode());
		System.out.println(body.readEntity(String.class));
		Member member = new Member("Janek", "hrasek", new Date(), "M", true);
		Response createResult = target.path("devteam")
			.request()
			.post(Entity.entity(mapper.writeValueAsString(member), MediaType.APPLICATION_JSON));
		System.out.println("Created result code: " + createResult.getStatus());
		mapper.readValue("", new TypeReference<String>() {});
		System.out.println(mapper.readValue(createResult.readEntity(String.class), Object.class));
	}
}
