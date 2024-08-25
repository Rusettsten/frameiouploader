package org.muny.frameiouploader.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.muny.frameiouploader.FrameIoUploader;
import org.muny.frameiouploader.utility.ConsoleHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ApiUtility {

	/*
	 * VARIABLES
	 */
	private String apiToken;
	private HttpClient httpClient;
	private JsonParser jp;
	
	/*
	 * METHODS
	 */
	public JsonElement sendApiRequest(String url, String body) { //API REQUEST WITH BODY
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder()
				.header("Authorization", "Bearer " + apiToken)
				.header("content-type", "application/json")
				.uri(new URI(url))
				.POST(BodyPublishers.ofString(body))
		        .build();
			
			return sendRequest(url, request); //Make the request
		
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JsonElement sendApiRequest(String url) { //API REQUEST WITH NO BODY
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder()
			  .header("Authorization", "Bearer " + apiToken)
			  .uri(new URI(url)).build();
			
			return sendRequest(url, request); //Make the request
		
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean uploadFile(String url, String contentType, byte[] data) {
		//sleep, to reduce issues
		try {
			Thread.sleep(700);
		} catch (InterruptedException ex) {
			ConsoleHelper.outputError("Error sleeping while making an API call to " + url);
			ex.printStackTrace();
		}
		
		//create header & stream
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder()
					.header("content-type", FrameIoUploader.currentProperties.getFiletype())
					.header("x-amz-acl", "private")
					.uri(new URI(url))
					.expectContinue(false)
					.timeout(Duration.ofMillis(FrameIoUploader.currentProperties.getHttpRequestTimeoutMs()))
					.PUT(BodyPublishers.ofByteArray(data))
					.build();
			
			HttpResponse<String> rawResponse;
			rawResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(rawResponse.body());
			
			if(rawResponse.statusCode() == 200 && FrameIoUploader.currentProperties.getDebugOutput()) {
				ConsoleHelper.outputGood("Status code 200 received for request at " + url);
				return true;
			}else if(rawResponse.statusCode() != 200){
				ConsoleHelper.outputError("API ERROR: Http responded with code " + rawResponse.statusCode());
				return false;
			}
			
		} catch(URISyntaxException | IOException | InterruptedException ex) {
			ConsoleHelper.outputError("Error when uploading file. See below.");
			ex.printStackTrace();
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private JsonElement sendRequest(String url, HttpRequest request) {
		try {
			Thread.sleep(700);
		} catch (InterruptedException ex) {
			ConsoleHelper.outputError("Error sleeping while making an API call to " + url);
			ex.printStackTrace();
		}
		
		HttpResponse<String> rawResponse;
		try {
			rawResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if(rawResponse.statusCode() == 200 && FrameIoUploader.currentProperties.getDebugOutput()) {
				ConsoleHelper.outputGood("Status code 200 received for request at " + url);
			}else if(rawResponse.statusCode() != 200) {
				ConsoleHelper.outputError("API ERROR: Http responded with code " + rawResponse.statusCode());
			}
			JsonElement root = jp.parse(rawResponse.body());
			
			return root;
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		return null;
	}
	
	
	/*
	 * METHODS - GETTERS & SETTERS
	 */
	public String getApiToken() {
		return apiToken;
	}
	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}	
	
	
	/*
	 * CONSTRUCTOR
	 */
	@SuppressWarnings("deprecation")
	public ApiUtility(String apiToken) {
		this.apiToken = apiToken;
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(FrameIoUploader.currentProperties.getHttpRequestTimeoutMs())).build();
		this.jp = new JsonParser();
	}
}
