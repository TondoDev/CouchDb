package org.tondo.elections.designdoc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads Design documents from file system
 * 
 * @author TondoDev
 *
 */
public class DesignDocFileSystemLoader {
	
	private String designDocLoction;
	
	public DesignDocFileSystemLoader(String location) {
		this.designDocLoction = location;
	}

	
	
	public List<DesignDocument> load() throws IOException {
		
		try(DirectoryStream<Path> docs = Files.newDirectoryStream(Paths.get(designDocLoction), "*.json")) {
			List<DesignDocument> designDocs = new ArrayList<>();
			for (Path design : docs) {
				String content = readFileAsString(design);
				designDocs.add(new DesignDocument(pathToDesignName(design), content));
			}
			
			return designDocs;
		}
	}
	
	private String pathToDesignName(Path path) {
		String withSuffix = path.getFileName().toString();
		// .json - we are relying on this suffix
		return withSuffix.substring(0, withSuffix.length() - 5);
	}
	
	private String readFileAsString(Path file) throws FileNotFoundException, IOException {
		
		StringBuilder buffer = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			
			return buffer.toString();
		}
	}
}
