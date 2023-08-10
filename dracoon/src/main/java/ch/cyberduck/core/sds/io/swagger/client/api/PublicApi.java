package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ActiveDirectoryAuthInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.ChunkUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteS3ShareUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateShareUploadChannelRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateShareUploadChannelResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import java.io.File;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneratePresignedUrlsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.OpenIdAuthInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.PresignedUrlList;
import ch.cyberduck.core.sds.io.swagger.client.model.PublicDownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.PublicDownloadTokenGenerateRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.PublicDownloadTokenGenerateResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.PublicUploadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.PublicUploadedFileData;
import ch.cyberduck.core.sds.io.swagger.client.model.S3ShareUploadStatus;
import ch.cyberduck.core.sds.io.swagger.client.model.SdsServerTime;
import ch.cyberduck.core.sds.io.swagger.client.model.SoftwareVersionData;
import ch.cyberduck.core.sds.io.swagger.client.model.SystemInfo;
import ch.cyberduck.core.sds.io.swagger.client.model.ThirdPartyDependenciesData;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicApi {
  private ApiClient apiClient;

  public PublicApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PublicApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Cancel file upload
   * ### Description: Abort (chunked) upload via Upload Share.  ### Precondition: Valid Upload ID.  ### Postcondition: Aborts upload and invalidates upload ID / token.  ### Further Information: None.
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @throws ApiException if fails to make API call
   */
  public void cancelFileUploadViaShare(String accessKey, String uploadId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling cancelFileUploadViaShare");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling cancelFileUploadViaShare");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}/{upload_id}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

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
   * Check public Download Share password
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.36.0&lt;/h3&gt;  ### Description: Check password for a public Download Share  ### Precondition: None.  ### Postcondition: None.  ### Further Information: None.
   * @param accessKey Access key (required)
   * @param password Download share password (optional)
   * @throws ApiException if fails to make API call
   */
  public void checkPublicDownloadSharePassword(String accessKey, String password) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling checkPublicDownloadSharePassword");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/downloads/{access_key}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "password", password));


    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Complete file upload
   * ### Description: Finalize (chunked) upload via Upload Share.  ### Precondition: Valid upload ID.   Only returns users that owns one of the following permissions: &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt;  ### Postcondition: Finalizes upload.  ### Further Information: Chunked uploads (range requests) are supported.    Please ensure that all chunks have been transferred correctly before finishing the upload.   If file hash has been created in time a &#x60;201 Created&#x60; will be responded and hash will be part of response, otherwise it will be a &#x60;202 Accepted&#x60; without it. 
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @param body  (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return PublicUploadedFileData
   * @throws ApiException if fails to make API call
   * Range Requests
   * @see <a href="https://tools.ietf.org/html/rfc7233">Complete file upload Documentation</a>
   */
  public PublicUploadedFileData completeFileUploadViaShare(String accessKey, String uploadId, UserFileKeyList body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling completeFileUploadViaShare");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling completeFileUploadViaShare");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}/{upload_id}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicUploadedFileData> localVarReturnType = new GenericType<PublicUploadedFileData>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Complete S3 file upload
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.15.0&lt;/h3&gt;  ### Description: Finishes a S3 file upload and closes the corresponding upload channel.  ### Precondition: Valid upload ID.   Only returns users that owns one of the following permissions: &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt;  ### Postcondition: Upload channel is closed. S3 multipart upload request is completed.  ### Further Information: None. 
   * @param body  (required)
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @throws ApiException if fails to make API call
   */
  public void completeS3FileUploadViaShare(CompleteS3ShareUploadRequest body, String accessKey, String uploadId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling completeS3FileUploadViaShare");
    }
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling completeS3FileUploadViaShare");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling completeS3FileUploadViaShare");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}/{upload_id}/s3"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

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

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Create new file upload channel
   * ### Description:   Create a new upload channel.  ### Precondition: None.  ### Postcondition: Upload channel is created and corresponding upload URL, token &amp; upload ID are returned.  ### Further Information: Use &#x60;uploadUrl&#x60; the upload &#x60;token&#x60; is deprecated.    Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.  ### Node naming convention: * Node (room, folder, file) names are limited to **150** characters. * Illegal names:   &#x60;&#x27;CON&#x27;, &#x27;PRN&#x27;, &#x27;AUX&#x27;, &#x27;NUL&#x27;, &#x27;COM1&#x27;, &#x27;COM2&#x27;, &#x27;COM3&#x27;, &#x27;COM4&#x27;, &#x27;COM5&#x27;, &#x27;COM6&#x27;, &#x27;COM7&#x27;, &#x27;COM8&#x27;, &#x27;COM9&#x27;, &#x27;LPT1&#x27;, &#x27;LPT2&#x27;, &#x27;LPT3&#x27;, &#x27;LPT4&#x27;, &#x27;LPT5&#x27;, &#x27;LPT6&#x27;, &#x27;LPT7&#x27;, &#x27;LPT8&#x27;, &#x27;LPT9&#x27;, (and any of those with an extension)&#x60; * Illegal characters in names:   &#x60;&#x27;\\\\&#x27;, &#x27;&lt;&#x27;,&#x27;&gt;&#x27;, &#x27;:&#x27;, &#x27;\\\&quot;&#x27;, &#x27;|&#x27;, &#x27;?&#x27;, &#x27;*&#x27;, &#x27;/&#x27;, leading &#x27;-&#x27;, trailing &#x27;.&#x27; &#x60; 
   * @param body  (required)
   * @param accessKey Access key (required)
   * @return CreateShareUploadChannelResponse
   * @throws ApiException if fails to make API call
   */
  public CreateShareUploadChannelResponse createShareUploadChannel(CreateShareUploadChannelRequest body, String accessKey) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createShareUploadChannel");
    }
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling createShareUploadChannel");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()));

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

    GenericType<CreateShareUploadChannelResponse> localVarReturnType = new GenericType<CreateShareUploadChannelResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Download file with token
   * ### Description:   Download a file (or zip archive if target is a folder or room).  ### Precondition: Valid download token.  ### Postcondition: Stream is returned.  ### Further Information: Range requests are supported.   Range requests are illegal for zip archive download.
   * @param accessKey Access key (required)
   * @param token Download token (required)
   * @param range Range   e.g. &#x60;bytes&#x3D;0-999&#x60; (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @throws ApiException if fails to make API call
   * Range Requests
   * @see <a href="https://tools.ietf.org/html/rfc7233">Download file with token Documentation</a>
   */
  public void downloadFileViaTokenPublic(String accessKey, String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling downloadFileViaTokenPublic");
    }
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling downloadFileViaTokenPublic");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/downloads/{access_key}/{token}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "generic_mimetype", genericMimetype));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "inline", inline));

    if (range != null)
      localVarHeaderParams.put("Range", apiClient.parameterToString(range));

    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Download file with token
   * ### Description:   Download a file (or zip archive if target is a folder or room).  ### Precondition: Valid download token.  ### Postcondition: Stream is returned.  ### Further Information: Range requests are supported.   Range requests are illegal for zip archive download.
   * @param accessKey Access key (required)
   * @param token Download token (required)
   * @param range Range   e.g. &#x60;bytes&#x3D;0-999&#x60; (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @throws ApiException if fails to make API call
   * Range Requests
   * @see <a href="https://tools.ietf.org/html/rfc7233">Download file with token Documentation</a>
   */
  public void downloadFileViaTokenPublic1(String accessKey, String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling downloadFileViaTokenPublic1");
    }
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling downloadFileViaTokenPublic1");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/downloads/{access_key}/{token}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "generic_mimetype", genericMimetype));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "inline", inline));

    if (range != null)
      localVarHeaderParams.put("Range", apiClient.parameterToString(range));

    final String[] localVarAccepts = {
      "application/octet-stream"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Generate download URL
   * ### Description: Generate a download URL to retrieve a shared file.  ### Precondition: None.  ### Postcondition: Download URL and token are generated and returned.  ### Further Information: Use &#x60;downloadUrl&#x60; the download &#x60;token&#x60; is deprecated.
   * @param accessKey Access key (required)
   * @param body  (optional)
   * @return PublicDownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public PublicDownloadTokenGenerateResponse generateDownloadUrlPublic(String accessKey, PublicDownloadTokenGenerateRequest body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling generateDownloadUrlPublic");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/downloads/{access_key}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()));

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

    GenericType<PublicDownloadTokenGenerateResponse> localVarReturnType = new GenericType<PublicDownloadTokenGenerateResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Generate presigned URLs for S3 file upload
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.15.0&lt;/h3&gt;  ### Description: Generate presigned URLs for S3 file upload.  ### Precondition: Valid upload ID  ### Postcondition: List of presigned URLs is returned.  ### Further Information: The size for each part must be &gt;&#x3D; 5 MB, except for the last part.   The part number of the first part in S3 is 1 (not 0).   Use HTTP method &#x60;PUT&#x60; for uploading bytes via presigned URL.
   * @param body  (required)
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return PresignedUrlList
   * @throws ApiException if fails to make API call
   */
  public PresignedUrlList generatePresignedUrlsPublic(GeneratePresignedUrlsRequest body, String accessKey, String uploadId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling generatePresignedUrlsPublic");
    }
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling generatePresignedUrlsPublic");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling generatePresignedUrlsPublic");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}/{upload_id}/s3_urls"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PresignedUrlList> localVarReturnType = new GenericType<PresignedUrlList>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Active Directory authentication information
   * ### Description:   Provides information about Active Directory authentication options.  ### Precondition: None.  ### Postcondition: Active Directory authentication options information is returned.  ### Further Information: None.
   * @param isGlobalAvailable Show only global available items (optional)
   * @return ActiveDirectoryAuthInfo
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryAuthInfo requestActiveDirectoryAuthInfo(Boolean isGlobalAvailable) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/public/system/info/auth/ad";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "is_global_available", isGlobalAvailable));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ActiveDirectoryAuthInfo> localVarReturnType = new GenericType<ActiveDirectoryAuthInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request OpenID Connect provider authentication information
   * ### Description:   Provides information about OpenID Connect authentication options.  ### Precondition: None.  ### Postcondition: OpenID Connect authentication options information is returned.  ### Further Information: None.
   * @param isGlobalAvailable Show only global available items (optional)
   * @return OpenIdAuthInfo
   * @throws ApiException if fails to make API call
   */
  public OpenIdAuthInfo requestOpenIdAuthInfo(Boolean isGlobalAvailable) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/public/system/info/auth/openid";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "is_global_available", isGlobalAvailable));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<OpenIdAuthInfo> localVarReturnType = new GenericType<OpenIdAuthInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request public Download Share information
   * ### Description:   Retrieve the public information of a Download Share.  ### Precondition: None.  ### Postcondition: Download Share information is returned.  ### Further Information: None.
   * @param accessKey Access key (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return PublicDownloadShare
   * @throws ApiException if fails to make API call
   */
  public PublicDownloadShare requestPublicDownloadShareInfo(String accessKey, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling requestPublicDownloadShareInfo");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/downloads/{access_key}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicDownloadShare> localVarReturnType = new GenericType<PublicDownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request public Upload Share information
   * ### Description:   Provides information about the desired Upload Share.  ### Precondition: Only &#x60;userUserPublicKeyList&#x60; is returned to the users who owns one of the following permissions: &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; read&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt;, &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt;  ### Postcondition: None.  ### Further Information: If no password is set, the returned information is reduced to the following attributes (if available):  * &#x60;name&#x60; * &#x60;createdAt&#x60; * &#x60;isProtected&#x60; * &#x60;isEncrypted&#x60; * &#x60;showUploadedFiles&#x60; * &#x60;userUserPublicKeyList&#x60; (if parent is end-to-end encrypted)  Only if the password is transmitted as &#x60;X-Sds-Share-Password&#x60; header, all values are returned. 
   * @param accessKey Access key (required)
   * @param xSdsSharePassword Upload share password. Should be base64-encoded.  Plain X-Sds-Share-Passwords are *deprecated* and will be removed in the future (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return PublicUploadShare
   * @throws ApiException if fails to make API call
   */
  public PublicUploadShare requestPublicUploadShareInfo(String accessKey, String xSdsSharePassword, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling requestPublicUploadShareInfo");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsSharePassword != null)
      localVarHeaderParams.put("X-Sds-Share-Password", apiClient.parameterToString(xSdsSharePassword));
    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicUploadShare> localVarReturnType = new GenericType<PublicUploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request software version information
   * ### Description:   Public software version information.  ### Precondition: None.  ### Postcondition: Sofware version information is returned.  ### Further Information: The version of DRACOON Server consists of two components: * **API** * **Core** (referred to as _\&quot;Server\&quot;_)  which are versioned individually.
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return SoftwareVersionData
   * @throws ApiException if fails to make API call
   */
  public SoftwareVersionData requestSoftwareVersion(String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/public/software/version";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<SoftwareVersionData> localVarReturnType = new GenericType<SoftwareVersionData>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request system information
   * ### Description:   Provides information about system.  ### Precondition: None.  ### Postcondition: System information is returned.  ### Further Information: Authentication methods are sorted by **priority** attribute.   Smaller values have higher priority.   Authentication method with highest priority is considered as default.  ### System information: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Setting | Description | Value | | :--- | :--- | :--- | | &#x60;languageDefault&#x60; | Defines which language should be default. | &#x60;ISO 639-1 code&#x60; | | &#x60;hideLoginInputFields&#x60; | Defines if login fields should be hidden. | &#x60;true or false&#x60; | | &#x60;s3Hosts&#x60; | List of available S3 hosts. | &#x60;String array&#x60; | | &#x60;s3EnforceDirectUpload&#x60; | Determines whether S3 direct upload is enforced or not. | &#x60;true or false&#x60; | | &#x60;useS3Storage&#x60; | Determines whether S3 Storage enabled and used. | &#x60;true or false&#x60; |  &lt;/details&gt;  ### Authentication methods: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Authentication Method | Description | | :--- | :--- | | &#x60;basic&#x60; | **Basic** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their credentials stored in the database.&lt;br&gt;Formerly known as &#x60;sql&#x60;. | | &#x60;active_directory&#x60; | **Active Directory** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their Active Directory credentials. | | &#x60;radius&#x60; | **RADIUS** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their RADIUS username, their PIN and a token password. | | &#x60;openid&#x60; | **OpenID Connect** authentication globally allowed.This option **MUST** be activated to allow users to log in with their OpenID Connect identity. | | &#x60;hideLoginInputFields&#x60; | Determines whether input fields for login should be enabled | &#x60;true or false&#x60; |  &lt;/details&gt;
   * @param isEnabled Show only enabled authentication methods (optional)
   * @return SystemInfo
   * @throws ApiException if fails to make API call
   * Tags for Identifying Languages
   * @see <a href="https://tools.ietf.org/html/rfc5646">Request system information Documentation</a>
   */
  public SystemInfo requestSystemInfo(Boolean isEnabled) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/public/system/info";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "is_enabled", isEnabled));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<SystemInfo> localVarReturnType = new GenericType<SystemInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request system time
   * ### Description:   Retrieve the actual server time.  ### Precondition: None.  ### Postcondition: Server time is returned.  ### Further Information: None.
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return SdsServerTime
   * @throws ApiException if fails to make API call
   */
  public SdsServerTime requestSystemTime(String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/public/time";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<SdsServerTime> localVarReturnType = new GenericType<SdsServerTime>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request third-party software dependencies
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.9.0&lt;/h3&gt;  ### Description:   Provides information about used third-party software dependencies.  ### Precondition: None.  ### Postcondition: List of the third-party software dependencies used by **DRACOON Core** (referred to as _\&quot;Server\&quot;_) is returned.  ### Further Information: None.  
   * @return List&lt;ThirdPartyDependenciesData&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ThirdPartyDependenciesData> requestThirdPartyDependencies() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/public/software/third_party_dependencies";

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

    GenericType<List<ThirdPartyDependenciesData>> localVarReturnType = new GenericType<List<ThirdPartyDependenciesData>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request status of S3 file upload
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.15.0&lt;/h3&gt;  ### Description: Request status of a S3 file upload.  ### Precondition: An upload channel has been created and the user has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; create&lt;/span&gt; permissions in the parent container (room or folder).  ### Postcondition: Status of S3 multipart upload request is returned.  ### Further Information: None.  ### Possible errors: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Http Status | Error Code | Description | | :--- | :--- | :--- | | &#x60;400 Bad Request&#x60; | &#x60;-80000&#x60; | Mandatory fields cannot be empty | | &#x60;400 Bad Request&#x60; | &#x60;-80001&#x60; | Invalid positive number | | &#x60;400 Bad Request&#x60; | &#x60;-80002&#x60; | Invalid number | | &#x60;400 Bad Request&#x60; | &#x60;-40001&#x60; | (Target) room is not encrypted | | &#x60;400 Bad Request&#x60; | &#x60;-40755&#x60; | Bad file name | | &#x60;400 Bad Request&#x60; | &#x60;-40763&#x60; | File key must be set for an upload into encrypted room | | &#x60;400 Bad Request&#x60; | &#x60;-50506&#x60; | Exceeds the number of files for this Upload Share | | &#x60;403 Forbidden&#x60; |  | Access denied | | &#x60;404 Not Found&#x60; | &#x60;-20501&#x60; | Upload not found | | &#x60;404 Not Found&#x60; | &#x60;-40000&#x60; | Container not found | | &#x60;404 Not Found&#x60; | &#x60;-41000&#x60; | Node not found | | &#x60;404 Not Found&#x60; | &#x60;-70501&#x60; | User not found | | &#x60;409 Conflict&#x60; | &#x60;-40010&#x60; | Container cannot be overwritten | | &#x60;409 Conflict&#x60; |  | File cannot be overwritten | | &#x60;500 Internal Server Error&#x60; |  | System Error | | &#x60;502 Bad Gateway&#x60; |  | S3 Error | | &#x60;502 Insufficient Storage&#x60; | &#x60;-50504&#x60; | Exceeds the quota for this Upload Share | | &#x60;502 Insufficient Storage&#x60; | &#x60;-40200&#x60; | Exceeds the free node quota in room | | &#x60;502 Insufficient Storage&#x60; | &#x60;-90200&#x60; | Exceeds the free customer quota | | &#x60;502 Insufficient Storage&#x60; | &#x60;-90201&#x60; | Exceeds the free customer physical disk space |  &lt;/details&gt;
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @return S3ShareUploadStatus
   * @throws ApiException if fails to make API call
   */
  public S3ShareUploadStatus requestUploadStatusPublic(String accessKey, String uploadId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling requestUploadStatusPublic");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling requestUploadStatusPublic");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}/{upload_id}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

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

    GenericType<S3ShareUploadStatus> localVarReturnType = new GenericType<S3ShareUploadStatus>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Upload file
   * ### Description:   Chunked upload of files via Upload Share.  ### Precondition: Valid upload ID.  ### Postcondition: Chunk of file is uploaded.  ### Further Information: Chunked uploads (range requests) are supported.  Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;    For both file upload types set the correct &#x60;Content-Type&#x60; header and body.    ### Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/public/shares/uploads/{access_key}{upload_id} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;   &#x60;&#x60;&#x60; POST /api/v4/public/shares/uploads/{access_key}{upload_id} HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60;
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @param file  (optional)
   * @param contentRange Content-Range   e.g. &#x60;bytes 0-999/3980&#x60; (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ChunkUploadResponse
   * @throws ApiException if fails to make API call
   * Range Requests
   * @see <a href="https://tools.ietf.org/html/rfc7233">Upload file Documentation</a>
   */
  public ChunkUploadResponse uploadFileAsMultipartPublic1(String accessKey, String uploadId, File file, String contentRange, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling uploadFileAsMultipartPublic1");
    }
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadFileAsMultipartPublic1");
    }
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}/{upload_id}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (contentRange != null)
      localVarHeaderParams.put("Content-Range", apiClient.parameterToString(contentRange));
    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (file != null)
      localVarFormParams.put("file", file);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ChunkUploadResponse> localVarReturnType = new GenericType<ChunkUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
