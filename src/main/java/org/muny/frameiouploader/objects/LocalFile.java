package org.muny.frameiouploader.objects;

import java.io.File;
import java.util.regex.Pattern;

public class LocalFile {

	/*
	 * VARIABLES
	 */
	private String filePath;
	private String fileName;
	private String fileExtension;
	private long lastModifiedTime;
	private File file;
	private int frameCount;
	
	
	/*
	 * METHODS - GETTERS AND SETTERS
	 */
	public String getFilePath() {
		return filePath;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}
	
	public File getFile() {
		return file;
	}
	
	public int getFrameCount() {
		return frameCount;
	}
	
	public void setFrameCount(int frameCount) {
		this.frameCount = frameCount;
	}
	
	
	/*
	 * METHODS - TIME MODIFIERS
	 */
	public void markModified() {
		lastModifiedTime = System.nanoTime();
	}
	
	public boolean isRecentlyModified() {
		long notRecentTime = lastModifiedTime + 10000000000L;
		if(System.nanoTime() >= notRecentTime) {
			return false;
		}else {
			return true;
		}
	}
	
	
	/*
	 * METHODS - OTHER
	 */
	public long getFileSize() {
		return file.length();
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public LocalFile(String filePath) {
		this.filePath = filePath;
		
		String seperatorChar = "";
		String[] fileIntermediate = null;
		
		if(System.getProperty("os.name").contains("Win")) {
			seperatorChar = "\\";
			fileIntermediate = filePath.replaceAll(Pattern.quote(seperatorChar), "\\\\").split("\\\\");
		}else {
			seperatorChar = "/";
			fileIntermediate = filePath.split(seperatorChar);
		}
		
		this.file = new File(filePath);
		this.fileName = fileIntermediate[fileIntermediate.length-1];
		String[] extIntermediate = fileName.split("\\.");
		this.fileExtension = extIntermediate[extIntermediate.length-1];
		
		this.lastModifiedTime = System.nanoTime();
	}
}
