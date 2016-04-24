package org.tondo.elections.partygenerator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author TondoDev
 *
 */
public class PartyDataLoader {
	
	private Set<String> maleNames = new HashSet<>();
	private Set<String> femaleNames = new HashSet<>();
	private Set<String> surnames = new HashSet<>();

	public PartyDataLoader(String namesSource, String surnamesSource) {
		loadNames(namesSource);
		loadSurnames(surnamesSource);
	}
	
	private void loadNames(String fileWithNames) {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileWithNames), Charset.forName("UTF-8")))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] parts = line.split("\\s+");
				if (parts.length != 2) {
					throw new IllegalStateException("Invalid firstname format");
				}
				String gender = parts[0];
				String name = parts[1].trim();
				
				if ("M".equals(gender)) {
					this.maleNames.add(name);
				} else if ("F".equals(gender)) {
					this.femaleNames.add(name);
				} else {
					throw new IllegalStateException("Invalid gender `"+ gender + "'");
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Can't load names data", e);
		}
	}
	
	private void loadSurnames(String fileWithSurnames ) {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileWithSurnames), Charset.forName("UTF-8")))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				surnames.add(modifyCase(line));
			}
		} catch (IOException e) {
			throw new IllegalStateException("Can't load surnames data", e);
		}
	}
	
	/**
	 * Capitalize first letter of surname
	 */
	private String modifyCase(String name) {
		String[] parts = name.split("\\s+");
		String capitalized = "";
		
		for (String p : parts) {
			capitalized += p.substring(0,1).toUpperCase() + p.substring(1).toLowerCase() + " ";
		}
		
		return capitalized.trim();
	}
	
	public Set<String> getMaleFirstNames() {
		if (this.maleNames == null) {
			throw new IllegalStateException();
		}
		return new HashSet<>(maleNames);
	}
	
	public Set<String> getFemaleFirstNames() {
		if (this.femaleNames == null) {
			throw new IllegalStateException();
		}
		return new HashSet<>(femaleNames);	}
	
	/***
	 * Commong for both males and females
	 */
	public Set<String> getSurnames() {
		if (this.surnames == null) {
			throw new IllegalStateException();
		}
		return new HashSet<>(surnames);
	}
	
	public Set<String> getPartyNames() {
		return new HashSet<>(Arrays.asList(
				"Strana Zelených",
				"Kopáèska strana",
				"Pracovití ¾udia",
				"Modrý svet",
				"Muzikanti",
				"Mládež Slovenska",
				"Gamers",
				"Pontónový most"));
	}
}
