package org.muny.frameiouploader.observation;

import java.util.concurrent.atomic.AtomicLong;

import org.muny.frameiouploader.FrameIoUploader;
import org.muny.frameiouploader.UploadProcessor;
import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.api.objects.RemoteFolder;
import org.muny.frameiouploader.objects.LocalFile;
import org.muny.frameiouploader.utility.ConsoleHelper;

public class FileWatcher implements Runnable {

	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	private String path;
	private LocalFile localFile;
	private RemoteFolder remoteFolder;
	private boolean keepAlive;
	private AtomicLong lastUpdatedNanotime;
	private boolean realtime;
	
	/*
	 * METHODS - AUXILLARY
	 */
	public String getPath() {
		return path;
	}
	
	public void kill() {
		this.keepAlive = false;
	}
	
	public void updateNanotime() {
		this.lastUpdatedNanotime.set(System.nanoTime());
	}
	
	public long getLastUpdatedNanotime() {
		return lastUpdatedNanotime.get();
	}
	
	/*
	 * METHODS - RUNNING
	 */
	public void run() {
		
		keepAlive = true;
		
		if(realtime) {
			realtimeUploadLoop();
		}else if (realtime) {
			classicUploadLoop();
		}
		
	}
	
	private void realtimeUploadLoop() {
		
	}
	
	private void classicUploadLoop() {
		ConsoleHelper.outputInformation("Starting non-realtime FileWatcher on " + localFile.getFileName());
		
		long lastKnownFilesize = 0;
		boolean waitingSuccess = false;
		
		while(keepAlive) {
			if(localFile.getFileSize() != lastKnownFilesize && getLastUpdatedNanotime() < (System.nanoTime() - (1000000L * (long) FrameIoUploader.currentProperties.getLiveFinalTimeoutMs()))) {
				lastKnownFilesize = localFile.getFileSize();
				updateNanotime();
			}else if(localFile.getFileSize() == lastKnownFilesize && getLastUpdatedNanotime() < (System.nanoTime() - (1000000L * (long) FrameIoUploader.currentProperties.getLiveFinalTimeoutMs()))) {
				waitingSuccess = true;
				break;
			}
			
		}
		
		if(waitingSuccess) {
			ConsoleHelper.outputInformation(localFile.getFileName() + " looks to be finished! Uploading now...");
			UploadProcessor up = new UploadProcessor(api, localFile, remoteFolder.getAssetId());
			up.createAsset();
			up.createUploadThreads();
			up.executeUploadThreads();
		} else {
			ConsoleHelper.outputWarning("Something went wrong when monitoring file: " + localFile.getFileName() + " Did not wait correctly.");
		}
		
		ConsoleHelper.outputInformation("Stopping non-realtime FileWatcher on " + localFile.getFileName());
	}
	
	
	
	
	/*
	 * CONSTRUCTOR
	 */
	public FileWatcher(ApiUtility api, String path, RemoteFolder remoteFolder, boolean realtime) {
		this.api = api;
		this.path = path;
		this.localFile = new LocalFile(path);
		this.remoteFolder = remoteFolder;
		this.keepAlive = true;
		this.realtime = realtime;
		this.lastUpdatedNanotime = new AtomicLong(System.nanoTime());
	}
	
}
