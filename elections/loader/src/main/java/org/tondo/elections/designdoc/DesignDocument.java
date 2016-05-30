package org.tondo.elections.designdoc;

/**
 * Groups design document name and its content
 * 
 * @author TondoDev
 *
 */
public class DesignDocument {

	private String name;
	private String content;
	
	public DesignDocument(String name, String content) {
		this.name = name;
		this.content = content;
	}
	
	public String getName() {
		return name;
	}
	
	public String getContent() {
		return content;
	}
}
