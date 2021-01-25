package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.FileRequest;
import ch.cyberduck.core.box.io.swagger.client.model.FileRequestCopyRequest;
import ch.cyberduck.core.box.io.swagger.client.model.FileRequestUpdateRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class FileRequestsApi {
  private ApiClient apiClient;

  public FileRequestsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FileRequestsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete file request
   * Deletes a file request permanently.
   * @param fileRequestId The unique identifier that represent a file request.  The ID for any file request can be determined by visiting a file request builder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/filerequest/123&#x60; the &#x60;file_request_id&#x60; is &#x60;123&#x60;. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteFileRequestsId(String fileRequestId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileRequestId' is set
    if (fileRequestId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileRequestId' when calling deleteFileRequestsId");
    }
    // create path and map variables
    String localVarPath = "/file_requests/{file_request_id}"
      .replaceAll("\\{" + "file_request_id" + "\\}", apiClient.escapeString(fileRequestId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get file request
   * Retrieves the information about a file request.
   * @param fileRequestId The unique identifier that represent a file request.  The ID for any file request can be determined by visiting a file request builder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/filerequest/123&#x60; the &#x60;file_request_id&#x60; is &#x60;123&#x60;. (required)
   * @return FileRequest
   * @throws ApiException if fails to make API call
   */
  public FileRequest getFileRequestsId(String fileRequestId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileRequestId' is set
    if (fileRequestId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileRequestId' when calling getFileRequestsId");
    }
    // create path and map variables
    String localVarPath = "/file_requests/{file_request_id}"
      .replaceAll("\\{" + "file_request_id" + "\\}", apiClient.escapeString(fileRequestId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<FileRequest> localVarReturnType = new GenericType<FileRequest>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Copy file request
   * Copies an existing file request that is already present on one folder, and applies it to another folder.
   * @param fileRequestId The unique identifier that represent a file request.  The ID for any file request can be determined by visiting a file request builder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/filerequest/123&#x60; the &#x60;file_request_id&#x60; is &#x60;123&#x60;. (required)
   * @param body  (optional)
   * @return FileRequest
   * @throws ApiException if fails to make API call
   */
  public FileRequest postFileRequestsIdCopy(String fileRequestId, FileRequestCopyRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fileRequestId' is set
    if (fileRequestId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileRequestId' when calling postFileRequestsIdCopy");
    }
    // create path and map variables
    String localVarPath = "/file_requests/{file_request_id}/copy"
      .replaceAll("\\{" + "file_request_id" + "\\}", apiClient.escapeString(fileRequestId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<FileRequest> localVarReturnType = new GenericType<FileRequest>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update file request
   * Updates a file request. This can be used to activate or deactivate a file request.
   * @param fileRequestId The unique identifier that represent a file request.  The ID for any file request can be determined by visiting a file request builder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/filerequest/123&#x60; the &#x60;file_request_id&#x60; is &#x60;123&#x60;. (required)
   * @param body  (optional)
   * @param ifMatch Ensures this item hasn&#x27;t recently changed before making changes.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;412 Precondition Failed&#x60; if it has changed since. (optional)
   * @return FileRequest
   * @throws ApiException if fails to make API call
   */
  public FileRequest putFileRequestsId(String fileRequestId, FileRequestUpdateRequest body, String ifMatch) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fileRequestId' is set
    if (fileRequestId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileRequestId' when calling putFileRequestsId");
    }
    // create path and map variables
    String localVarPath = "/file_requests/{file_request_id}"
      .replaceAll("\\{" + "file_request_id" + "\\}", apiClient.escapeString(fileRequestId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (ifMatch != null)
      localVarHeaderParams.put("if-match", apiClient.parameterToString(ifMatch));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<FileRequest> localVarReturnType = new GenericType<FileRequest>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
