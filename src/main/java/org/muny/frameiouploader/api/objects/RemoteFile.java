package org.muny.frameiouploader.api.objects;

public class RemoteFile {
	/*
	 * VARIABLES
	 */
	private String name;
	private String assetId;
	private int frameCount;
	
	
	/*
	 * METHODS - GETTERS AND SETTERS
	 */
	public String getName() {
		return name;
	}
	
	public String getAssetId() {
		return assetId;
	}
	
	public int getFrameCount() {
		return frameCount;
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public RemoteFile(String name, String assetId, int frameCount) {
		this.name = name;
		this.assetId = assetId;
		this.frameCount = frameCount;
	}
}
