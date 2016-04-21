package org.tondo.elections.pojo;

import java.util.Date;

/**
 * 
 * @author TondoDev
 *
 */
public class Member {

	private String name;
	private String surname;
	private Date birth;
	private boolean chairman;
	private String gender;
	
	public String getName() {
		return name;
	}
	public String getSurname() {
		return surname;
	}
	public Date getBirth() {
		return birth;
	}
	public boolean isChairman() {
		return chairman;
	}
	
	public String getGender() {
		return gender;
	}
	
	@Override
	public String toString() {
		return name + " " + surname + " " + gender +(chairman ? ", predseda" : "");
	}
	
	
}
