package org.muny.frameiouploader.api.objects;

import java.util.Iterator;

import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.utility.ConsoleHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Team implements Refreshable {

	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	private String teamNameLookup;
	private String teamId;
	private String name;
	
	/*
	 * METHODS - GETTERS AND SETTERS
	 */
	public String getTeamId() {
		return teamId;
	}
	
	public String getName() {
		return name;
	}
	
	
	/*
	 * METHODS - REFRESH
	 */
	public void refreshData() {
		try {
			JsonElement teamsRequest = api.sendApiRequest("https://api.frame.io/v2/teams");
			JsonArray teams = teamsRequest.getAsJsonArray();
			
			//find team matching team name lookup
			boolean foundTeam = false;
			Iterator<JsonElement> teamsIterator = teams.iterator();
			while(teamsIterator.hasNext()) {
				JsonElement workingElement = teamsIterator.next();
				JsonObject workingObj = workingElement.getAsJsonObject();
				
				String checkName = workingObj.get("name").toString().replaceAll("\"", "");
				
				if(checkName.equals(teamNameLookup)) {
					teamId = workingObj.get("id").toString().replaceAll("\"", "");
					name = workingObj.get("name").toString().replaceAll("\"", "");
					
					ConsoleHelper.outputGood("Successfully updated team: " + teamNameLookup  + ".");
					ConsoleHelper.outputInformation("\t team id: " + teamId);
					ConsoleHelper.outputInformation("\t name: " + name);
					foundTeam = true;
				}
			}
			
			//if the team is not found, output warning
			if(!foundTeam) {
				ConsoleHelper.outputWarning("Team '" + teamNameLookup + "' not found!");
			}
		} catch (Exception ex) {
			ConsoleHelper.outputError("Error updating teams. See below.");
			ex.printStackTrace();
		}
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public Team(ApiUtility api, String teamNameLookup) {
		this.api = api;
		this.teamNameLookup = teamNameLookup;
		refreshData();
	}
}
