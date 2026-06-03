package org.muny.frameiouploader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.muny.frameiouploader.api.ApiUtility;
import org.muny.frameiouploader.objects.LocalFile;
import org.muny.frameiouploader.utility.ConsoleHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LiveUploadProcessor {

	/*
	 * VARIABLES
	 */
	private ApiUtility api;
	private LocalFile fileToUpload;
	private String parentFolderId;
	private String assetId;
	private ArrayList<String> uploadUrls = new ArrayList<String>();
	private int nextChunkIndex = 0;


	/*
	 * METHODS - CREATE ASSET
	 */
	public boolean createLiveAsset() {
		JsonObject assetInfo = new JsonObject();
		assetInfo.addProperty("name", fileToUpload.getFileName());
		assetInfo.addProperty("filetype", FrameIoUploader.currentProperties.getFiletype());
		assetInfo.addProperty("filesize", fileToUpload.getFileSize());
		assetInfo.addProperty("parent_id", parentFolderId);
		assetInfo.addProperty("is_realtime_upload", true);

		String requestUrl = "https://api.frame.io/v4/accounts/" + FrameIoUploader.accountId + "/files";
		JsonElement response = api.sendApiRequest(requestUrl, assetInfo.toString());
		if (response == null || !response.isJsonObject()) {
			ConsoleHelper.outputError("Failed to create live asset for " + fileToUpload.getFileName());
			return false;
		}

		JsonObject createdAsset = response.getAsJsonObject();
		assetId = createdAsset.get("id").toString().replaceAll("\"", "");
		JsonArray urls = createdAsset.getAsJsonArray("upload_urls");
		for (int i = 0; i < urls.size(); i++) {
			uploadUrls.add(urls.get(i).toString().replaceAll("\"", ""));
		}

		ConsoleHelper.outputGood("Created live asset for " + fileToUpload.getFileName() + " with " + uploadUrls.size() + " upload slots.");
		return true;
	}


	/*
	 * METHODS - UPLOAD
	 */
	public boolean uploadChunk(long startByte, long byteCount) {
		if (nextChunkIndex >= uploadUrls.size()) {
			ConsoleHelper.outputError("Ran out of upload URLs at chunk " + nextChunkIndex + " for " + fileToUpload.getFileName());
			return false;
		}

		String url = uploadUrls.get(nextChunkIndex);

		try (FileInputStream fis = new FileInputStream(fileToUpload.getFile())) {
			fis.skip(startByte);
			byte[] data = fis.readNBytes((int) byteCount);
			boolean success = api.uploadFile(url, FrameIoUploader.currentProperties.getFiletype(), data);
			if (success) {
				ConsoleHelper.outputGood("Uploaded live chunk " + nextChunkIndex + " (" + byteCount + " bytes) for " + fileToUpload.getFileName());
				nextChunkIndex++;
			}
			return success;
		} catch (IOException ex) {
			ConsoleHelper.outputError("IO error reading chunk " + nextChunkIndex + " for live upload of " + fileToUpload.getFileName());
			ex.printStackTrace();
			return false;
		}
	}


	/*
	 * CONSTRUCTOR
	 */
	public LiveUploadProcessor(ApiUtility api, LocalFile localFile, String parentFolderId) {
		this.api = api;
		this.fileToUpload = localFile;
		this.parentFolderId = parentFolderId;
	}

}
