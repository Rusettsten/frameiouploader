package org.muny.frameiouploader;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.http.HttpTimeoutException;

import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.objects.LocalFile;
import org.muny.frameiouploader.utility.ConsoleHelper;

public class UploadThread implements Runnable {

	/*
	 * VARIABLES
	 */
	private String assetId;
	private String uploadUrl;
	private long startByte;
	private long byteSize;
	private ApiUtility api;
	private LocalFile fileToUpload;
	private int retryCount = 0;
	
	/*
	 * METHODS
	 */
	public void run() {
		boolean retry = true;
		
		while(retry) {
			
			if(retryCount >= FrameIoUploader.currentProperties.getUploadThreadCount()) {
				ConsoleHelper.outputError("Reached max retrys. FAILED UPLOAD!");
				retry = false;
			}
			
			retryCount++;
			
			try {
				final FileInputStream fis = new FileInputStream(fileToUpload.getFile());
				fis.skip(startByte);
				ConsoleHelper.outputInformation("Reading bytes of " + fileToUpload.getFileName() + " " + startByte + " / " + fileToUpload.getFileSize());
				byte[] data = fis.readNBytes((int) byteSize);
				ConsoleHelper.outputInformation("Beginning upload of " + fileToUpload.getFileName() + " " + startByte + " / " + fileToUpload.getFileSize());
				api.uploadFile(uploadUrl, FrameIoUploader.currentProperties.getFiletype(), data);
				retry = false;
				ConsoleHelper.outputGood("Uploaded " + fileToUpload.getFileName() + " " + startByte + " / " + fileToUpload.getFileSize() + " successfully!");
			} catch (HttpTimeoutException ex) {
				ConsoleHelper.outputWarning("Upload timed out! Let's try that again. Retry: " + retryCount);
				retry = true;
	        } catch (IOException ex) {
				ConsoleHelper.outputError("IO Exception on upload thread. Non recoverable. See below");
				ex.printStackTrace();
				retry = false;
			}
		}
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public UploadThread(ApiUtility api, String assetId, String uploadUrl, long startByte, long byteSize, LocalFile localFile) {
		this.api = api;
		this.assetId = assetId;
		this.uploadUrl = uploadUrl;
		this.startByte = startByte;
		this.byteSize = byteSize;
		this.fileToUpload = localFile;
	}
	
	
}
