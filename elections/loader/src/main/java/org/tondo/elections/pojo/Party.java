package org.tondo.elections.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author TondoDev
 *
 */
public class Party {

	private String name;
	private List<Member> members;
	
	/**
	 * For deserialization usage
	 */
	public Party() {
		
	}
	
	public Party(String name, List<Member> members) {
		this.name = name;
		this.members = new ArrayList<>(members);
	}
	
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
