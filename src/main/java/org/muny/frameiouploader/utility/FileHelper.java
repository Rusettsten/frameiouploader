package org.muny.frameiouploader.utility;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class FileHelper {
	/*
	 * METHODS
	 */
	
	public static ArrayList<File> getFilesByExtension(String path, String extension) {
		ArrayList<File> files = new ArrayList<File>();
		File directory = new File(path);
		
		if(directory.isDirectory()) {
			FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(extension);
			File[] matches = directory.listFiles(filter);
			if(matches != null) {
				files.addAll(Arrays.asList(matches));
			}
		}else {
			ConsoleHelper.outputError("Provided path of '" + path + "' is not a directory.");
		}
		return files;
	}
}
