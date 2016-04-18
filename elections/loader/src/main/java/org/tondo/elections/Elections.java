package org.tondo.elections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.tondo.elections.pojo.Region;


import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author TondoDev
 *
 */
public class Elections {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.out.println(System.getProperty("user.dir"));
		ObjectMapper mapper = new ObjectMapper();
		List<Region> regions = null;
		try (InputStream is = new FileInputStream("../regions.json")) {
			mapper.readValue(is, Region.class);
		}
	}
}
