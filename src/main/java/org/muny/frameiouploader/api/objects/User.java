package org.muny.frameiouploader.api.objects;

import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.utility.ConsoleHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class User implements Refreshable {

	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	
	private String userId;
	private String accountId;
	private String email;
	private String name;
	private String uploadUrl;
	
	
	/*
	 * METHODS - GETTERS AND SETTERS
	 */
	public String getUserId() {
		return userId;
	}
	
	public String getAccountId() {
		return accountId;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUploadUrl() {
		return uploadUrl;
	}
	
	
	/*
	 * METHODS - REFRESH DATA
	 */
	public void refreshData() {
		try {
			JsonElement userRequest = api.sendApiRequest("https://api.frame.io/v2/me");
			JsonObject userJson = userRequest.getAsJsonObject();
			userId = userJson.get("id").toString().replaceAll("\"", "");
			accountId = userJson.get("account_id").toString().replaceAll("\"", "");
			email = userJson.get("email").toString().replaceAll("\"", "");
			name = userJson.get("name").toString().replaceAll("\"", "");
			uploadUrl = userJson.get("upload_url").toString().replaceAll("\"", "");
			
			ConsoleHelper.outputGood("Successfully updated currently authenticated user.");
			ConsoleHelper.outputInformation("\t account id: " + accountId);
			ConsoleHelper.outputInformation("\t user id: " + userId);
			ConsoleHelper.outputInformation("\t name: " + name);
			ConsoleHelper.outputInformation("\t email: " + email);
			ConsoleHelper.outputInformation("\t upload url: " + uploadUrl);
			
		} catch (Exception ex) {
			ConsoleHelper.outputError("Error updating current authenticated user. See below.");
			ex.printStackTrace();
		}
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public User(ApiUtility api) {
		this.api = api;
		refreshData();
	}
}
