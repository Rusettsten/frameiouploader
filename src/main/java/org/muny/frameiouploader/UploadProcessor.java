package org.muny.frameiouploader;

import java.util.ArrayList;

import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.objects.LocalFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UploadProcessor {

	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	private LocalFile fileToUpload;
	private String parentFolderId;
	private String assetId;
	private ArrayList<UploadThread> uploadThreads = new ArrayList<UploadThread>();
	private ArrayList<String> uploadUrls = new ArrayList<String>();
	
	
	/*
	 * METHODS - GETTERS AND SETTERS
	 */
	
	
	
	/*
	 * METHODS - CREATE ASSET
	 */
	public void createAsset() {
		JsonObject assetInfo = new JsonObject();
		assetInfo.addProperty("name", fileToUpload.getFileName());
		assetInfo.addProperty("type", "file");
		assetInfo.addProperty("filetype", FrameIoUploader.currentProperties.getFiletype());
		assetInfo.addProperty("is_realtime_upload", false);
		assetInfo.addProperty("filesize", fileToUpload.getFileSize());
		
		String requestUrl = "https://api.frame.io/v2/assets/" + parentFolderId + "/children";
		String body = assetInfo.toString();
		JsonElement createAssetRequest = api.sendApiRequest(requestUrl, body);
		JsonObject createdAsset = createAssetRequest.getAsJsonObject();
		
		//get id and upload urls
		assetId = createdAsset.get("id").toString().replaceAll("\"", "");
		JsonArray uploadUrlsJson = createdAsset.getAsJsonArray("upload_urls");
		for(int x = 0; x < uploadUrlsJson.size(); x++) {
			JsonElement workingElement = uploadUrlsJson.get(x);
			uploadUrls.add(workingElement.toString().replaceAll("\"", ""));
		}
	}
	
	/*
	 * METHODS - THREAD MANAGEMENT
	 */
	public void createUploadThreads() {
		
		//calculate byte lengths
		long byteLength = fileToUpload.getFileSize() / uploadUrls.size();
		long lastByteLength;
		if(fileToUpload.getFileSize() % uploadUrls.size() != 0) {
			lastByteLength = byteLength + (fileToUpload.getFileSize() % uploadUrls.size());
		} else {
			lastByteLength = byteLength;
		}
		
		//create upload threads - last one separate
		if(uploadUrls.size() > 1) {
			for(int x = 0; x < (uploadUrls.size() - 1); x++) {
				long startByte = byteLength * x;
				UploadThread ut = new UploadThread(api, assetId, uploadUrls.get(x), startByte, byteLength, fileToUpload);
				uploadThreads.add(ut);
			}
		}
		
		long lastStartByte = (uploadUrls.size() - 1) * byteLength;
		UploadThread ut = new UploadThread(api, assetId, uploadUrls.get(uploadUrls.size() - 1), lastStartByte, lastByteLength, fileToUpload);
		uploadThreads.add(ut);
	}
	
	public void executeUploadThreads() {
		for(int x = 0; x < uploadThreads.size(); x++) {
			FrameIoUploader.uploadThreadExecutor.execute(uploadThreads.get(x));
		}
	}
	
	
	/*
	 * CONSTRUCTOR
	 */
	public UploadProcessor(ApiUtility api, LocalFile localFile, String parentFolderId) {
		this.fileToUpload = localFile;
		this.api = api;
		this.parentFolderId = parentFolderId;
		FrameIoUploader.currentUser.getUploadUrl();
	}
	

}
