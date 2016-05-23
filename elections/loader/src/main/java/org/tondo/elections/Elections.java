package org.tondo.elections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.tondo.elections.database.CouchDbConnection;
import org.tondo.elections.database.DatabaseInitializationTask;
import org.tondo.elections.database.InitializationConfig;
import org.tondo.elections.pojo.Party;
import org.tondo.elections.pojo.Region;

import com.fasterxml.jackson.core.type.TypeReference;
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
}
