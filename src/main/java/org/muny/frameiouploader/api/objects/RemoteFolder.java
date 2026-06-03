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
	private String accountId;
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
			JsonElement assetsRequest = api.sendApiRequest("https://api.frame.io/v4/accounts/" + accountId + "/folders/" + assetId + "/children");
			JsonArray assets = assetsRequest.getAsJsonArray();
			Iterator<JsonElement> assetsIterator = assets.iterator();

			while(assetsIterator.hasNext()) {
				JsonElement workingElement = assetsIterator.next();
				JsonObject workingObj = workingElement.getAsJsonObject();

				String type = workingObj.get("type").toString().replaceAll("\"", "");
				String assetName = workingObj.get("name").toString().replaceAll("\"", "");
				String childAssetId = workingObj.get("id").toString().replaceAll("\"", "");

				JsonElement framesEl = workingObj.get("frame_count");
				if (framesEl == null || framesEl.isJsonNull()) {
					framesEl = workingObj.get("frames");
				}
				int frameCount = (framesEl != null && !framesEl.isJsonNull()) ? framesEl.getAsInt() : 0;

				if(type.equals("file")) {
					RemoteFile workingFile = new RemoteFile(assetName, childAssetId, frameCount);
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
	public RemoteFolder(ApiUtility api, String accountId, String name, String assetId) {
		this.api = api;
		this.accountId = accountId;
		this.name = name;
		this.assetId = assetId;
	}
}
