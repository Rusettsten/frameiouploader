package org.muny.frameiouploader.objects;

import java.util.ArrayList;

public class LocalFolder {

	/*
	 * VARIABLES
	 */
	private String localPath;
	private ArrayList<LocalFile> localFiles;
	
	
	/*
	 * METHODS
	 */
	public String getLocalPath() {
		return localPath;
	}
	
	public ArrayList<LocalFile> getLocalFiles() {
		return localFiles;
	}
	
	public void addFile(LocalFile localFile) {
		localFiles.add(localFile);
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public LocalFolder(String localPath) {
		this.localPath = localPath;
	}
}
