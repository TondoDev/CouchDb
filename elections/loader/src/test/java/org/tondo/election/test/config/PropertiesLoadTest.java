package org.tondo.election.test.config;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.tondo.elections.config.ElectionsConfiguration;

public class PropertiesLoadTest {
	
	@Test
	public void testDefaultProperties() {
		ElectionsConfiguration config = new ElectionsConfiguration();
		assertEquals("Host", "127.0.0.1", config.getHost());
		assertEquals("Port", 5984, config.getPort());
		assertNull(config.getPassword());
		assertNull(config.getUserName());
		assertEquals("votes", 10000, config.getVotesCount());
	}
	
	@Test
	public void testLoadedFromFile() throws IOException {
		ElectionsConfiguration config = new ElectionsConfiguration();
		config.load("test.properties");
		
		assertEquals("Host", "127.10.20.1", config.getHost());
		assertEquals("Port", 6688, config.getPort());
		assertEquals("hrasek", config.getPassword());
		assertEquals("janek",config.getUserName());
		assertEquals("votes", 5, config.getVotesCount());
	}

}

