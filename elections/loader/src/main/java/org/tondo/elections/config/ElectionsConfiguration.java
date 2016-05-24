package org.tondo.elections.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Load configuration for database initialization
 * 
 * @author TondoDev
 *
 */
public class ElectionsConfiguration {
	
	private int votesCount;
	private String host;
	private int port;
	private String userName;
	private String password;
	
	public ElectionsConfiguration() {
		populate(new Properties());
	}

	public void load(String fileName) throws IOException {
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(fileName)) {
			props.load(fis);
		}
		
		populate(props);
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public int getVotesCount() {
		return votesCount;
	}
	
	private void populate(Properties props) {
		this.host = props.getProperty("host", "127.0.0.1");
		this.userName = props.getProperty("username", null);
		this.password = props.getProperty("password", null);
		
		try {
			this.port = Integer.parseInt(props.getProperty("port", "5984"));
		} catch (NumberFormatException e) {
			this.port = 0;
		}
		try {
			this.votesCount = Integer.parseInt(props.getProperty("votescount", "10000"));
		} catch (NumberFormatException e) {
			this.votesCount = 0;
		}
	}
}

