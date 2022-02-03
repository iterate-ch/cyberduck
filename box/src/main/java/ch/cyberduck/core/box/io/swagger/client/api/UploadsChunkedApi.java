package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.FileIdUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.FilesUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadParts;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSession;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSessionIdCommitBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadedPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadsChunkedApi {
  private ApiClient apiClient;

  public UploadsChunkedApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UploadsChunkedApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove upload session
   * Abort an upload session and discard all data uploaded.  This cannot be reversed.
   * @param uploadSessionId The ID of the upload session. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteFilesUploadSessionsId(String uploadSessionId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'uploadSessionId' is set
    if (uploadSessionId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadSessionId' when calling deleteFilesUploadSessionsId");
    }
    // create path and map variables
    String localVarPath = "/files/upload_sessions/{upload_session_id}"
      .replaceAll("\\{" + "upload_session_id" + "\\}", apiClient.escapeString(uploadSessionId.toString()));

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

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get upload session
   * Return information about an upload session.
   * @param uploadSessionId The ID of the upload session. (required)
   * @return UploadSession
   * @throws ApiException if fails to make API call
   */
  public UploadSession getFilesUploadSessionsId(String uploadSessionId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'uploadSessionId' is set
    if (uploadSessionId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadSessionId' when calling getFilesUploadSessionsId");
    }
    // create path and map variables
    String localVarPath = "/files/upload_sessions/{upload_session_id}"
      .replaceAll("\\{" + "upload_session_id" + "\\}", apiClient.escapeString(uploadSessionId.toString()));

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<UploadSession> localVarReturnType = new GenericType<UploadSession>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List parts
   * Return a list of the chunks uploaded to the upload session so far.
   * @param uploadSessionId The ID of the upload session. (required)
   * @param offset The offset of the item at which to begin the response. (optional, default to 0)
   * @param limit The maximum number of items to return per page. (optional)
   * @return UploadParts
   * @throws ApiException if fails to make API call
   */
  public UploadParts getFilesUploadSessionsIdParts(String uploadSessionId, Long offset, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'uploadSessionId' is set
    if (uploadSessionId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadSessionId' when calling getFilesUploadSessionsIdParts");
    }
    // create path and map variables
    String localVarPath = "/files/upload_sessions/{upload_session_id}/parts"
      .replaceAll("\\{" + "upload_session_id" + "\\}", apiClient.escapeString(uploadSessionId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UploadParts> localVarReturnType = new GenericType<UploadParts>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create upload session for existing file
   * Creates an upload session for an existing file.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param body  (optional)
   * @param ifMatch Ensures this item hasn&#x27;t recently changed before making changes.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;412 Precondition Failed&#x60; if it has changed since. (optional)
   * @return UploadSession
   * @throws ApiException if fails to make API call
   */
  public UploadSession postFilesIdUploadSessions(String fileId, FileIdUploadSessionsBody body, String ifMatch) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling postFilesIdUploadSessions");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/upload_sessions"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<UploadSession> localVarReturnType = new GenericType<UploadSession>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create upload session
   * Creates an upload session for a new file.
   * @param body  (optional)
   * @return UploadSession
   * @throws ApiException if fails to make API call
   */
  public UploadSession postFilesUploadSessions(FilesUploadSessionsBody body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/files/upload_sessions";

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<UploadSession> localVarReturnType = new GenericType<UploadSession>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Commit upload session
   * Close an upload session and create a file from the uploaded chunks.
   * @param digest The [RFC3230][1] message digest of the whole file.  Only SHA1 is supported. The SHA1 digest must be Base64 encoded. The format of this header is as &#x60;sha&#x3D;BASE64_ENCODED_DIGEST&#x60;.  [1]: https://tools.ietf.org/html/rfc3230 (required)
   * @param uploadSessionId The ID of the upload session. (required)
   * @param body  (optional)
   * @param ifMatch Ensures this item hasn&#x27;t recently changed before making changes.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;412 Precondition Failed&#x60; if it has changed since. (optional)
   * @param ifNoneMatch Ensures an item is only returned if it has changed.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;304 Not Modified&#x60; if the item has not changed since. (optional)
   * @return Files
   * @throws ApiException if fails to make API call
   */
  public Files postFilesUploadSessionsIdCommit(String digest, String uploadSessionId, UploadSessionIdCommitBody body, String ifMatch, String ifNoneMatch) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'digest' is set
    if (digest == null) {
      throw new ApiException(400, "Missing the required parameter 'digest' when calling postFilesUploadSessionsIdCommit");
    }
    // verify the required parameter 'uploadSessionId' is set
    if (uploadSessionId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadSessionId' when calling postFilesUploadSessionsIdCommit");
    }
    // create path and map variables
    String localVarPath = "/files/upload_sessions/{upload_session_id}/commit"
      .replaceAll("\\{" + "upload_session_id" + "\\}", apiClient.escapeString(uploadSessionId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (digest != null)
      localVarHeaderParams.put("digest", apiClient.parameterToString(digest));
    if (ifMatch != null)
      localVarHeaderParams.put("if-match", apiClient.parameterToString(ifMatch));
    if (ifNoneMatch != null)
      localVarHeaderParams.put("if-none-match", apiClient.parameterToString(ifNoneMatch));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Files> localVarReturnType = new GenericType<Files>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Upload part of file
   * Updates a chunk of an upload session for a file.
   * @param digest The [RFC3230][1] message digest of the chunk uploaded.  Only SHA1 is supported. The SHA1 digest must be Base64 encoded. The format of this header is as &#x60;sha&#x3D;BASE64_ENCODED_DIGEST&#x60;.  [1]: https://tools.ietf.org/html/rfc3230 (required)
   * @param contentRange The byte range of the chunk.  Must not overlap with the range of a part already uploaded this session. (required)
   * @param uploadSessionId The ID of the upload session. (required)
   * @param body  (optional)
   * @return UploadedPart
   * @throws ApiException if fails to make API call
   */
  public UploadedPart putFilesUploadSessionsId(String digest, String contentRange, String uploadSessionId, Object body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'digest' is set
    if (digest == null) {
      throw new ApiException(400, "Missing the required parameter 'digest' when calling putFilesUploadSessionsId");
    }
    // verify the required parameter 'contentRange' is set
    if (contentRange == null) {
      throw new ApiException(400, "Missing the required parameter 'contentRange' when calling putFilesUploadSessionsId");
    }
    // verify the required parameter 'uploadSessionId' is set
    if (uploadSessionId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadSessionId' when calling putFilesUploadSessionsId");
    }
    // create path and map variables
    String localVarPath = "/files/upload_sessions/{upload_session_id}"
      .replaceAll("\\{" + "upload_session_id" + "\\}", apiClient.escapeString(uploadSessionId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (digest != null)
      localVarHeaderParams.put("digest", apiClient.parameterToString(digest));
    if (contentRange != null)
      localVarHeaderParams.put("content-range", apiClient.parameterToString(contentRange));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/octet-stream"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UploadedPart> localVarReturnType = new GenericType<UploadedPart>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
