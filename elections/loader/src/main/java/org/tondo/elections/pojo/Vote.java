package org.tondo.elections.pojo;

import java.util.Collections;
import java.util.List;

public class Vote {

	private String partyName;
	private List<Member> votedMembers;
	
	public Vote(String name, List<Member> members) {
		this.partyName = name;
		this.votedMembers = Collections.unmodifiableList(members);
	}
	
	public String getPartyName() {
		return partyName;
	}
	
	public List<Member> getVotedMembers() {
		return votedMembers;
	}
}
