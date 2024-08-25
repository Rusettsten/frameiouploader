package org.muny.frameiouploader.objects;

import java.util.List;

public class Properties {
	
	/*
	 * VARIABLES
	 */
	private String apiToken;
	private String teamName;
	private String projectName;
	private boolean realtimeUpload;
	private String filetype;
	private int uploadThreadCount;
	private int liveChunkSizeKb;
	private int liveFinalTimeoutMs;
	private int httpRequestTimeoutMs;
	private int uploadRetryCount;
	private boolean debugOutput;
	private List<FolderMapping> folderMappings;
	
	/*
	 * METHODS - GETTERS
	 */
	public String getApiToken() {
		return apiToken;
	}
	
	public String getTeamName() {
		return teamName;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public boolean getRealtimeUpload() {
		return realtimeUpload;
	}
	
	public String getFiletype() {
		return filetype;
	}
	
	public int getUploadThreadCount() {
		return uploadThreadCount;
	}
	
	public int getLiveChunkSizeKb() {
		return liveChunkSizeKb;
	}
	
	public int getLiveFinalTimeoutMs() {
		return liveFinalTimeoutMs;
	}
	
	public int getHttpRequestTimeoutMs() {
		return httpRequestTimeoutMs;
	}
	
	public int getUploadRetryCount() {
		return uploadRetryCount;
	}
	
	public boolean getDebugOutput() {
		return debugOutput;
	}
	
	public List<FolderMapping> getFolderMappings() {
		return folderMappings;
	}
	
	
	/*
	 * METHODS - SETTERS
	 */
	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}
	
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public void setRealtimeUpload(boolean realtimeUpload) {
		this.realtimeUpload = realtimeUpload;
	}
	
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	
	public void setUploadThreadCount(int uploadThreadCount) {
		this.uploadThreadCount = uploadThreadCount;
	}
	
	public void setLiveChunkSizeKb(int liveChunkSizeKb) {
		this.liveChunkSizeKb = liveChunkSizeKb;
	}
	
	public void setLiveFinalTimeoutMs(int liveFinalTimeoutMs) {
		this.liveFinalTimeoutMs = liveFinalTimeoutMs;
	}
	
	public void setHttpRequestTimeoutMs(int httpRequestTimeoutMs) {
		this.httpRequestTimeoutMs = httpRequestTimeoutMs;
	}
	
	public void setUploadRetryCount(int uploadRetryCount) {
		this.uploadRetryCount = uploadRetryCount;
	}
	
	public void setDebugOutput(boolean debugOutput) {
		this.debugOutput = debugOutput;
	}
	
	public void setFolderMappings(List<FolderMapping> folderMappings) {
		this.folderMappings = folderMappings;
	}
	
}
