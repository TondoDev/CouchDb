package org.tondo.elections.pojo;

import java.util.List;

/**
 * 
 * @author TondoDev
 *
 */
public class Party {

	private String name;
	
	private List<Member> members;
	
	public String getName() {
		return name;
	}
	
	public List<Member> getMembers() {
		return members;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Nazov: ").append(name).append("\n")
			.append("Clenovia: [\n");
		
		for (Member m : this.members) {
			sb.append(m.toString()).append("\n");
		}
		
		sb.append("]");
		return sb.toString();
	}
}
