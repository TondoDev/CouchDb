package org.tondo.elections.partygenerator;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.tondo.elections.pojo.Member;
import org.tondo.elections.pojo.Party;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author TondoDev
 *
 */
public class PartyGenerator {
	
	private static final int MIN_MEMBERS = 5;
	private static final int MAX_MEMBERS = 100;
	private static final int NAME_RETRY = 5;
	private static final int MIN_BIRTH_YEAR = 1950;
	private static final int MAX_BIRTH_YEAR = Calendar.getInstance().get(Calendar.YEAR) - 18;
	
	private String[] males;
	private String[] females;
	private String[] srunames;
	private String[] parties;
	
	private Random randomizer;
	
	public PartyGenerator(PartyDataLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException("Party data loader can't be null!");
		}
		
		Set<String> femaleNames = loader.getFemaleFirstNames();
		this.females = femaleNames.toArray(new String[femaleNames.size()]);
		Set<String> maleNames = loader.getMaleFirstNames();
		this.males = maleNames.toArray(new String[maleNames.size()]);
		Set<String> surnamesForBoth = loader.getSurnames();
		this.srunames = surnamesForBoth.toArray(new String[surnamesForBoth.size()]);
		Set<String> partyNames = loader.getPartyNames();
		this.parties = partyNames.toArray(new String[partyNames.size()]);		
	}

	
	public List<Party> generate() {
		List<Party> partiesList = new ArrayList<>();
		this.randomizer = new Random();
		
		for (String partyName : this.parties) {
			int membersCount = MIN_MEMBERS + randomizer.nextInt(MAX_MEMBERS - MIN_MEMBERS + 1);
			List<Member> membersList = new ArrayList<>(membersCount);
			Set<String> usedNames = new HashSet<>();
			for (int mi = 0; mi < membersCount; mi++) {
				membersList.add(generateMember(usedNames, mi == 0));
			}
			
			partiesList.add(new Party(partyName, membersList));
		}
		
		return partiesList;
	}
	
	
	public void exportParties(List<Party> parties, String outFileName) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writer(new DefaultPrettyPrinter()).writeValue(new FileOutputStream(outFileName), parties);
		} catch (Exception e) {
			throw new IllegalStateException("Can't store parties into file!", e);
		}
	}
	
	private Member generateMember(Set<String> usedNames, boolean chairman) {
		String gender = randomizer.nextBoolean() ? "M" : "F";
		String name = "M".equals(gender) ? this.males[randomizer.nextInt(this.males.length)] :  this.females[randomizer.nextInt(this.females.length)];
		String surname = this.srunames[randomizer.nextInt(this.srunames.length)];
		
		String duplicityKey = name + surname;
		int retry = 0;
		while (!usedNames.add(duplicityKey) && retry < NAME_RETRY) {
			gender = randomizer.nextBoolean() ? "M" : "F";
			name = "M".equals(gender) ? this.males[randomizer.nextInt(this.males.length)] :  this.females[randomizer.nextInt(this.females.length)];
			duplicityKey = name + surname;
			retry++;
		}
		
		if (retry == NAME_RETRY) {
			throw new IllegalStateException("Can't generate any more disctinct names for members!");
		}
		return new Member(name, surname, genDate(), gender, chairman);
	}
	
	private Date genDate() {
		int year = MIN_BIRTH_YEAR + randomizer.nextInt(MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1);
		Calendar birthDate = Calendar.getInstance();
		birthDate.set(Calendar.YEAR, year);
		birthDate.set(Calendar.MONTH, randomizer.nextInt(11));
		// I hope it will somehow overflow to next month if more days has been chosen than
		// actual month have
		birthDate.set(Calendar.DAY_OF_MONTH, 1 + randomizer.nextInt(31));
		
		return birthDate.getTime();
	}
	
	
	public static void main(String[] args) {
		PartyDataLoader loader = new PartyDataLoader("../names.txt", "../surnames.txt");
		PartyGenerator generator = new PartyGenerator(loader);
		List<Party> parties = generator.generate();
		generator.exportParties(parties, "../parties.json");
		
	}
}
