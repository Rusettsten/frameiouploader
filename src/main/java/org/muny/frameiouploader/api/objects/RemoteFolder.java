package org.muny.frameiouploader.api.objects;

import java.util.ArrayList;
import java.util.Iterator;

import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.utility.ConsoleHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RemoteFolder {

	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	private String name;
	private String assetId;
	
	/*
	 * METHODS - GETTERS AND SETTERS
	 */
	public String getName() {
		return name;
	}
	
	public String getAssetId() {
		return assetId;
	}
	
	
	/*
	 * METHODS - FIND CHILD FILES
	 */
	public ArrayList<RemoteFile> retrieveChildFiles() {
		
		int filesFound = 0;
		ArrayList<RemoteFile> remoteFiles = new ArrayList<RemoteFile>();
		
		try {
			JsonElement assetsRequest = api.sendApiRequest("https://api.frame.io/v2/assets/" + assetId + "/children");
			JsonArray assets = assetsRequest.getAsJsonArray();
			Iterator<JsonElement> assetsIterator = assets.iterator();
			
			while(assetsIterator.hasNext()) {
				JsonElement workingElement = assetsIterator.next();
				JsonObject workingObj = workingElement.getAsJsonObject();
				
				String type = workingObj.get("type").toString().replaceAll("\"", "");
				String assetName = workingObj.get("name").toString().replaceAll("\"", "");
				String assetId = workingObj.get("id").toString().replaceAll("\"", "");
				int frameCount = Integer.parseInt(workingObj.get("frames").toString().replaceAll("\"", ""));
				
				if(type.equals("file")) {
					RemoteFile workingFile = new RemoteFile(assetName, assetId, frameCount);
					remoteFiles.add(workingFile);
					filesFound++;
				}
			}
			
			
		} catch (Exception ex) {
			ConsoleHelper.outputError("Error updating files from project folder: '" + name + "'. See below.");
			ex.printStackTrace();
		}
		
		//if no files are found...
		if(filesFound == 0) {
			ConsoleHelper.outputWarning("Warning: Found zero folders inside folder '" + name + "'.");
		}else {
			ConsoleHelper.outputGood("Found " + filesFound + " files.");
		}
		
		//return files
		return remoteFiles;
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public RemoteFolder(ApiUtility api, String name, String assetId) {
		this.api = api;
		this.name = name;
		this.assetId = assetId;
	}
}
