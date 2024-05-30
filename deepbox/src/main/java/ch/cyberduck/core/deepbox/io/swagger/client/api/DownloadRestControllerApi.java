package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.Download;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DownloadAdd;
import ch.cyberduck.core.deepbox.io.swagger.client.model.LogEntry;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DownloadRestControllerApi {
  private ApiClient apiClient;

  public DownloadRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DownloadRestControllerApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * 
   * Get current download status
   * @param downloadId  (required)
   * @param acceptMessageIds  (optional)
   * @return Download
   * @throws ApiException if fails to make API call
   */
  public Download downloadStatus(UUID downloadId, String acceptMessageIds) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'downloadId' is set
    if (downloadId == null) {
      throw new ApiException(400, "Missing the required parameter 'downloadId' when calling downloadStatus");
    }
    // create path and map variables
    String localVarPath = "/api/v1/downloads/{downloadId}/status"
      .replaceAll("\\{" + "downloadId" + "\\}", apiClient.escapeString(downloadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "acceptMessageIds", acceptMessageIds));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Download> localVarReturnType = new GenericType<Download>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Presigned short living resource url for services (expires in 30 minutes, only for files). For the client download feature (multiple files and folders) use async /downloads endpoints instead.
   * @param nodeId  (required)
   * @param acceptMessageIds Accepted MessageIds to get a DownloadUrl in case of an error/warning. &#x27;*&#x27; to accept everything and bypass AVScan. (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadUrl(UUID nodeId, String acceptMessageIds) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling downloadUrl");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/downloadUrl"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "acceptMessageIds", acceptMessageIds));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Initiate a new download (multiple files and folder nodes supported). Check the response and use /downloads/{downloadId}/status if the download is not yet ready.
   * @param body  (required)
   * @return Download
   * @throws ApiException if fails to make API call
   */
  public Download requestDownload(DownloadAdd body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling requestDownload");
    }
    // create path and map variables
    String localVarPath = "/api/v1/downloads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Download> localVarReturnType = new GenericType<Download>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
