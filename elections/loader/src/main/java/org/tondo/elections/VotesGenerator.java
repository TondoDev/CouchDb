package org.tondo.elections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.tondo.elections.pojo.Member;
import org.tondo.elections.pojo.Party;
import org.tondo.elections.pojo.Vote;

public class VotesGenerator {
	
	public static final double WRONG_RATIO = 0.02;
	public static final int MAX_VOTED_MEMBERS = 4;

	private List<Party> parties;
	private Random randomizer;
	
	private final int partyCount;
	
	public VotesGenerator(List<Party> parties) {
		this.parties = Collections.unmodifiableList(parties);
		partyCount = parties.size();
		randomizer = new Random();
	}
	
	public Vote generateVote() {
		if (randomizer.nextDouble() > WRONG_RATIO) {
			return generateCorrectVote();
		} else {
			return generateWrongVote();
		}
	}
	
	public Vote generateCorrectVote() {
		Party party = parties.get(randomizer.nextInt(partyCount));
		int votedMembers = numberOfVotedMembers();
		
		if (votedMembers == 0) {
			return new Vote(party.getName(), Collections.<Member>emptyList());
		}
		
		return new Vote(party.getName(), generateVotedMembers(party, votedMembers));
	}
	
	public Vote generateWrongVote() {
		// vote is wrong, when more than max allowed members are voted
		int votedMembersCount = MAX_VOTED_MEMBERS + randomizer.nextInt(MAX_VOTED_MEMBERS) + 1;
		Party party = parties.get(randomizer.nextInt(partyCount));
		
		return  new Vote(party.getName(), generateVotedMembers(party, votedMembersCount));
	}
	
	private List<Member> generateVotedMembers(Party party, int votedMembers) {
		
		if (votedMembers > party.getMembers().size()) {
			throw new IllegalArgumentException("Party don't have enough members for this vote!");
		}
		
		// selected member will be removed from list for prevention of duplicit
		// selection of the same user
		List<Member> removableMembers = new LinkedList<>(party.getMembers());
		List<Member> generatedMembers = new ArrayList<>();
		for (int i = 0; i < votedMembers; i++) {
			Member voted = removableMembers.remove(randomizer.nextInt(removableMembers.size()));
			generatedMembers.add(voted);
		}

		return generatedMembers;
	}
	
	
	/**
	 * In case when, number of votes is not uniform
	 * @return
	 */
	private int numberOfVotedMembers() {
		return randomizer.nextInt(MAX_VOTED_MEMBERS + 1);
	}
}
