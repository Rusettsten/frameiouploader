package org.muny.frameiouploader.objects;

public class FolderMapping {

	/*
	 * VARIABLES
	 */
	private String localFolderLocation;
	private String remoteFolderName;
	
	
	/*
	 * METHODS
	 */
	public String getLocalFolderLocation() {
		return localFolderLocation;
	}
	
	public String getRemoteFolderName() {
		return remoteFolderName;
	}
	
	public void setLocalFolderLocation(String localFolderLocation) {
		this.localFolderLocation = localFolderLocation;
	}
	
	public void setRemoteFolderName(String remoteFolderName) {
		this.remoteFolderName = remoteFolderName;
	}
	
}
