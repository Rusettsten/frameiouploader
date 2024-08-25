package org.muny.frameiouploader;

import java.io.File;
import java.io.File.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.Format;
import org.jcodec.common.JCodecUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.containers.mkv.demuxer.MKVDemuxer;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.containers.mp4.demuxer.MP4DemuxerTrack;
import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.api.objects.RemoteFolder;
import org.muny.frameiouploader.api.objects.Project;
import org.muny.frameiouploader.api.objects.RemoteFile;
import org.muny.frameiouploader.api.objects.Team;
import org.muny.frameiouploader.api.objects.User;
import org.muny.frameiouploader.objects.FolderMapping;
import org.muny.frameiouploader.objects.LocalFile;
import org.muny.frameiouploader.objects.LocalFolder;
import org.muny.frameiouploader.objects.Properties;
import org.muny.frameiouploader.observation.FileWatcher;
import org.muny.frameiouploader.observation.FolderWatcher;
import org.muny.frameiouploader.utility.ConsoleHelper;
import org.muny.frameiouploader.utility.FileHelper;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


/*
 *	Welcome to the Muny's Frame Io Uploader source code. 
 * 
 * 	When running this program, the first argument should be a file path to a .txt with the API token inside.
 * 	The program will create a properties.yml
 * 
 *  use: --customPropertiesLocation
 *  
 * 
 */
public class FrameIoUploader {

	/*
	 * VARIABLES
	 */
	public static String version = "1.0.0";
	public static String authors = "Benji Arrigo";
	public static Properties currentProperties;
	
	public static String propertiesLocation;
	
	//to be filled in as the program runs
	public static ApiUtility api;
	public static User currentUser;
	public static Team currentTeam;
	public static Project currentProject;
	public static ExecutorService threadExecutor = Executors.newCachedThreadPool();
	public static ExecutorService uploadThreadExecutor;
	
	/*
	 * METHODS - MAIN
	 */
	public static void main(String[] args) {
		
		//welcome message
		ConsoleHelper.outputSeperator();
		ConsoleHelper.outputTitle("Welcome to FrameIoUploader V" + version);
		ConsoleHelper.outputInformation("Created for the Muny in St. Louis by " + authors);
		ConsoleHelper.outputSeperator();
		
		//get frame.io information
		loadPropertiesFile(args);
		api = new ApiUtility(currentProperties.getApiToken());
		currentUser = new User(api);
		currentTeam = new Team(api, currentProperties.getTeamName());
		currentProject = new Project(api, currentTeam.getTeamId(), currentProperties.getProjectName());
		uploadThreadExecutor = Executors.newFixedThreadPool(FrameIoUploader.currentProperties.getUploadThreadCount());
		ArrayList<RemoteFolder> remoteFolders = currentProject.getFolders();
		
		//make sure the videos in the g-drive match frame.io
		ConsoleHelper.outputSeperator();
		ConsoleHelper.outputInformation("Making sure the files in these folders are currently in frame.io...");
		
		
		//check folders and upload files
		Iterator<FolderMapping> mappingIterator = currentProperties.getFolderMappings().iterator();
		
		while(mappingIterator.hasNext()) {
			FolderMapping workingMapping = mappingIterator.next();
			
			//find remote folder
			boolean foundFolder = false;
			RemoteFolder workingFolder = null;
			for(int x = 0; x < remoteFolders.size(); x++) {
				if(remoteFolders.get(x).getName().equals(workingMapping.getRemoteFolderName())) {
					foundFolder = true;
					workingFolder = remoteFolders.get(x);
				}
			}
			
			if(foundFolder) {
				ConsoleHelper.outputGood("Found remote folder with name: \"" + workingMapping.getRemoteFolderName() + "\".");
			}else {
				ConsoleHelper.outputWarning("Could not find remote folder with name: \"" + workingMapping.getRemoteFolderName() + "\". Skipping.");
				continue;
			}
			
			//get remote child assets
			ArrayList<RemoteFile> remoteFiles = workingFolder.retrieveChildFiles();
			
			//get local folder & child files
			String[] filetypeParts = currentProperties.getFiletype().split("/");
			String fileExtension = filetypeParts[1];
			ArrayList<File> localBaseFiles = FileHelper.getFilesByExtension(workingMapping.getLocalFolderLocation(), "." + fileExtension);
			Iterator<File> fileIterator = localBaseFiles.iterator();
			ArrayList<LocalFile> localFiles = new ArrayList<LocalFile>();
			while(fileIterator.hasNext()) {
				File workingFile = fileIterator.next();
				LocalFile workingLocalFile = new LocalFile(workingFile.getAbsolutePath());
				
				int frameCount = 0;
				nu.pattern.OpenCV.loadShared();
				VideoCapture cap = new VideoCapture(workingLocalFile.getFilePath());
				if(cap.isOpened()) {
					frameCount = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
					cap.release();
				}else {
					ConsoleHelper.outputError("IO Error on detecting format of local video file. ");
				}
				
				workingLocalFile.setFrameCount(frameCount);
				localFiles.add(workingLocalFile);
			}
			
			
			//find out which ones don't exist on the server
			ArrayList<LocalFile> nonRemoteFiles = new ArrayList<LocalFile>();
			int remoteCount = 0;
			Iterator<LocalFile> localFileIterator = localFiles.iterator();
			while(localFileIterator.hasNext()) {
				boolean isRemote = false;
				LocalFile workingLocalFile = localFileIterator.next();
				int frameCount = workingLocalFile.getFrameCount();
				
				for(int x = 0; x < remoteFiles.size(); x++) {
					if(frameCount == remoteFiles.get(x).getFrameCount()) {
						isRemote = true;
					}
				}
				
				if(isRemote) {
					remoteCount++;
				}else {
					nonRemoteFiles.add(workingLocalFile);
				}
			}
			
			//report findings & upload files
			if(nonRemoteFiles.size() != 0) {
				ConsoleHelper.outputInformation("Found " + nonRemoteFiles.size() + " non-remote files. Beginning upload sequence. Please hold.");
				Iterator<LocalFile> nonRemoteIterator = nonRemoteFiles.iterator();
				while(nonRemoteIterator.hasNext()) {
					LocalFile workingLocalFile = nonRemoteIterator.next();
					FileWatcher fw = new FileWatcher(api, workingLocalFile.getFilePath(), workingFolder, false);
					threadExecutor.execute(fw);
				}
				
			} else {
				ConsoleHelper.outputInformation("All " + remoteCount + " remote file(s) found online. No upload necessary.");
			}
			
			/*
			 * WATCH SYSTEM
			 */
			LocalFolder localFolder = new LocalFolder(workingMapping.getLocalFolderLocation());
			FolderWatcher fw = new FolderWatcher(api, workingFolder, localFolder);
			threadExecutor.execute(fw);
			
			ConsoleHelper.outputSeperator();
			
		} //end iterator
		
		/*
		 * THE ~DEV ZONE~
		 */
		ConsoleHelper.outputWarning("DEVELOPMENT STOPPAGE: MONITORING FILESYSTEM.");
		
		try {
		WatchService watchService = FileSystems.getDefault().newWatchService();
		
		Path path = Paths.get(currentProperties.getFolderMappings().get(0).getLocalFolderLocation());
		path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
		
		WatchKey key;
		while ((key = watchService.take()) != null) {
			for(WatchEvent<?> event : key.pollEvents()) {
				if(currentProperties.getDebugOutput()) {
					ConsoleHelper.outputInformation(event.kind() + " \tFile: " + event.context());
				}
				key.reset();
			}
		}
		
		} catch(IOException | InterruptedException ex) {
			ConsoleHelper.outputError("Error on watch system. See below.");
			ex.printStackTrace();
		}
		
		
	}
	
	/*
	 * METHODS - STARTUP
	 */
	
	public static void loadPropertiesFile(String[] args) {
		//define base path
		String basePath = new File("").getAbsolutePath();
		propertiesLocation = basePath + File.separator + "properties.yml";
		ConsoleHelper.outputInformation("Base properties file set to : " + propertiesLocation);
		
		//see if an arg specifying a new properties file path has been specified
		for(int x = 0; x < args.length; x++) {
			if(args[x].equalsIgnoreCase("--customPropertiesLocation")) {
				try {
					propertiesLocation = args[x+1];
					ConsoleHelper.outputInformation("Detected custom properties location, reset to : " + propertiesLocation);
				} catch (Exception ex) {
					ConsoleHelper.outputError("Error when inputing custom properties location. Program will now exit.");
					System.exit(0);
				}
			}
		}
		
		//check to see if a project file exists
		final File propertiesFile;
		InputStream yamlStream = null;
		try {
			propertiesFile = new File(propertiesLocation);
			
			//if the file doesn't exist, copy the template and ask the user to fill it out.
			if(!propertiesFile.exists()) {
				URL templateUrl = FrameIoUploader.class.getResource("/properties.yml");
				FileUtils.copyURLToFile(templateUrl, propertiesFile);
				
				ConsoleHelper.outputWarning("No properties.yml detected. A template has been copied for you. Please fill it out and re-launch the program.");
				ConsoleHelper.outputError("The program will now exit.");
				System.exit(0);
			}
			
			yamlStream = new FileInputStream(propertiesFile);
		} catch (IOException ex) {
			ConsoleHelper.outputError("IOException when reading properties file. Program will now exit.");
			ex.printStackTrace();
			System.exit(0);
		}
		
		LoaderOptions loaderConfig = new LoaderOptions();
		Yaml yaml = new Yaml(new Constructor(Properties.class, loaderConfig));
		currentProperties = yaml.load(yamlStream);
		System.out.println("");
		ConsoleHelper.outputGood("Successfully loaded properties file!");
		ConsoleHelper.outputInformation("\t apiToken: " + currentProperties.getApiToken());
		ConsoleHelper.outputInformation("\t project name: " + currentProperties.getProjectName());
		ConsoleHelper.outputInformation("\t realtime upload: " + currentProperties.getRealtimeUpload());
		ConsoleHelper.outputInformation("\t filetype: " + currentProperties.getFiletype());
		ConsoleHelper.outputInformation("\t upload thread count: " + currentProperties.getUploadThreadCount());
		ConsoleHelper.outputInformation("\t live chunk size (kb): " + currentProperties.getLiveChunkSizeKb());
		ConsoleHelper.outputInformation("\t live final timeout (ms): " + currentProperties.getLiveFinalTimeoutMs());
		ConsoleHelper.outputInformation("\t http request timeout (ms): " + currentProperties.getHttpRequestTimeoutMs());
		ConsoleHelper.outputInformation("\t upload retry count: " + currentProperties.getUploadRetryCount());
		ConsoleHelper.outputInformation("\t debug output: " +  currentProperties.getDebugOutput());
		
		ConsoleHelper.outputInformation("\t folder mappings:");
		Iterator<FolderMapping> folderMappingIterator = currentProperties.getFolderMappings().iterator();
		while(folderMappingIterator.hasNext()) {
			FolderMapping workingFolderMapping = folderMappingIterator.next();
			ConsoleHelper.outputInformation("\t\t" + workingFolderMapping.getLocalFolderLocation() + " >>> " + workingFolderMapping.getRemoteFolderName());
		}
	}

}
