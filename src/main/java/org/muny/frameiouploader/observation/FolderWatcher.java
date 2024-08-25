package org.muny.frameiouploader.observation;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.muny.frameiouploader.FrameIoUploader;
import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.api.objects.RemoteFolder;
import org.muny.frameiouploader.objects.LocalFolder;
import org.muny.frameiouploader.utility.ConsoleHelper;

public class FolderWatcher implements Runnable {
	
	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	private RemoteFolder remoteFolder;
	private LocalFolder localFolder;
	private ArrayList<FileWatcher> fileWatchers = new ArrayList<FileWatcher>();
	private String seperatorChar;
	private ExecutorService threadExecutor = Executors.newCachedThreadPool();
	
	
	/*
	 * METHODS - MANAGEMENT
	 */
	
	public boolean doesFileWatcherExistFromPath(String filePath) {
		Iterator<FileWatcher> watcherIterator = fileWatchers.iterator();
		while(watcherIterator.hasNext()) {
			FileWatcher workingWatcher = watcherIterator.next();
			if(workingWatcher.getPath().equals(filePath)) {
				return true;
			}
		}
		return false;
	}
	
	public FileWatcher getFileWatcherFromPath(String filePath) {
		Iterator<FileWatcher> watcherIterator = fileWatchers.iterator();
		while(watcherIterator.hasNext()) {
			FileWatcher workingWatcher = watcherIterator.next();
			if(workingWatcher.getPath().equals(filePath)) {
				return workingWatcher;
			}
		}
		return null;
	}
	
	public void removeFileWatcher(FileWatcher fw) {
		fileWatchers.remove(fw);
	}
	
	/*
	 * METHODS - RUNNING
	 */
	public void run() {
		
		ConsoleHelper.outputInformation("Starting FolderWatcher service on " + localFolder.getLocalPath() + " at " + LocalDateTime.now());
		
		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();
			
			Path path = Paths.get(localFolder.getLocalPath());
			path.register(watchService,  StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
			
			WatchKey key;
			while((key = watchService.take()) != null) {
				for(WatchEvent<?> event: key.pollEvents()){
					if(event.kind().name().equals("ENTRY_CREATE") || event.kind().name().equals("ENTRY_MODIFY")) {
						//on modify or create, make sure one doesn't exist already.
						String filePath = localFolder.getLocalPath() + seperatorChar + event.context().toString();
						if(doesFileWatcherExistFromPath(filePath)) {
							getFileWatcherFromPath(filePath).updateNanotime();
						}else {
							FileWatcher fw = new FileWatcher(api, filePath, remoteFolder, FrameIoUploader.currentProperties.getRealtimeUpload());
							fileWatchers.add(fw);
							threadExecutor.execute(fw);
						}
					}else if(event.kind().name().equals("ENTRY_DELETE")) {
						//stop doing things with a deleted file!
						String filePath = localFolder.getLocalPath() + seperatorChar + event.context().toString();
						if(doesFileWatcherExistFromPath(filePath)) {
							FileWatcher fw = getFileWatcherFromPath(filePath);
							fw.kill();
							fileWatchers.remove(fw);
							//bye bye little file watcher
						}
					}
					Thread.sleep(1000);
					key.reset();
				}
			}
			
		} catch(IOException | InterruptedException ex) {
			ConsoleHelper.outputError("Error on watch system. See below.");
			
		}
		
		ConsoleHelper.outputInformation("Stopped FolderWatcher service on " + localFolder.getLocalPath() + " at " + LocalDateTime.now());
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public FolderWatcher(ApiUtility api, RemoteFolder remoteFolder, LocalFolder localFolder) {
		this.api = api;
		this.remoteFolder = remoteFolder;
		this.localFolder = localFolder;
		
		seperatorChar = "";
		if(System.getProperty("os.name").contains("Win")) {
			seperatorChar = "\\";
		}else {
			seperatorChar = "/";
		}
		
	}
}
