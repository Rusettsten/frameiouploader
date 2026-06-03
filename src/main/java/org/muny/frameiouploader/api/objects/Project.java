package org.muny.frameiouploader.api.objects;

import java.util.ArrayList;
import java.util.Iterator;

import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.utility.ConsoleHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Project implements Refreshable {

	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	private String accountId;
	private String projectNameLookup;
	private String projectId;
	private String projectName;
	private String projectRootAssetId;
	
	
	/*
	 * METHODS - GETTERS AND SETTERS
	 */
	public String getProjectId() {
		return projectId;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getRootAssetId() {
		return projectRootAssetId;
	}
	
	/*
	 * METHODS - REFRESH DATA
	 */
	public void refreshData() {
		try {
			JsonElement projectsRequest = api.sendApiRequest("https://api.frame.io/v4/accounts/" + accountId + "/projects");
			JsonArray projects = projectsRequest.getAsJsonArray();

			boolean foundProject = false;
			Iterator<JsonElement> projectsIterator = projects.iterator();
			while(projectsIterator.hasNext()) {
				JsonElement workingElement = projectsIterator.next();
				JsonObject workingObj = workingElement.getAsJsonObject();

				String checkName = workingObj.get("name").toString().replaceAll("\"", "");

				if(checkName.equals(projectNameLookup)) {
					projectId = workingObj.get("id").toString().replaceAll("\"", "");
					projectName = workingObj.get("name").toString().replaceAll("\"", "");

					JsonElement rootEl = workingObj.get("root_folder_id");
					if (rootEl == null || rootEl.isJsonNull()) {
						rootEl = workingObj.get("root_asset_id");
					}
					projectRootAssetId = rootEl.toString().replaceAll("\"", "");

					ConsoleHelper.outputGood("Successfully updated project: " + projectNameLookup + ".");
					ConsoleHelper.outputInformation("\t project id: " + projectId);
					ConsoleHelper.outputInformation("\t name: " + projectName);
					ConsoleHelper.outputInformation("\t root folder id: " + projectRootAssetId);
					foundProject = true;
				}

			}

			if(!foundProject) {
				ConsoleHelper.outputWarning("Project '" + projectNameLookup + "' not found!");
			}
		} catch (Exception ex) {
			ConsoleHelper.outputError("Error updating projects. See below.");
			ex.printStackTrace();
		}
	}
	
	
	/*
	 * METHODS - FIND FOLDERS
	 */
	public ArrayList<RemoteFolder> getFolders() {

		int foldersFound = 0;
		ArrayList<RemoteFolder> remoteFolders = new ArrayList<RemoteFolder>();

		try {
			JsonElement assetsRequest = api.sendApiRequest("https://api.frame.io/v4/accounts/" + accountId + "/folders/" + projectRootAssetId + "/children");
			JsonArray assets = assetsRequest.getAsJsonArray();
			Iterator<JsonElement> assetsIterator = assets.iterator();

			while(assetsIterator.hasNext()) {
				JsonElement workingElement = assetsIterator.next();
				JsonObject workingObj = workingElement.getAsJsonObject();

				String type = workingObj.get("type").toString().replaceAll("\"", "");
				String name = workingObj.get("name").toString().replaceAll("\"", "");
				String assetId = workingObj.get("id").toString().replaceAll("\"", "");

				if(type.equals("folder")) {
					RemoteFolder workingFolder = new RemoteFolder(api, accountId, name, assetId);
					remoteFolders.add(workingFolder);
					foldersFound++;
				}
			}

		}catch (Exception ex) {
			ConsoleHelper.outputError("Error updating folders from project: '" + projectName + "'. See below.");
			ex.printStackTrace();
		}
		
		//if no folders are found...
		if(foldersFound == 0) {
			ConsoleHelper.outputWarning("Warning: Found zero folders inside project '" + projectName + "'.");
		}else {
			ConsoleHelper.outputGood("Found " + foldersFound + " folders.");
		}
		
		//return folders
		return remoteFolders;
	}
	
	/*
	 * CONSTRUCTOR
	 */
	public Project(ApiUtility api, String accountId, String projectNameLookup) {
		this.api = api;
		this.accountId = accountId;
		this.projectNameLookup = projectNameLookup;
		refreshData();
	}
}
