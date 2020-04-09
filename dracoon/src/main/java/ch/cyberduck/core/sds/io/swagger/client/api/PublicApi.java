package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
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
   * ### Functional Description: Abort (chunked) upload via Upload Share.  ### Precondition: Valid Upload ID.  ### Effects: Aborts upload and invalidates upload ID / token.  ### &amp;#9432; Further Information: None.
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @throws ApiException if fails to make API call
   */
  public void cancelShareUpload(String accessKey, String uploadId) throws ApiException {

    cancelShareUploadWithHttpInfo(accessKey, uploadId);
  }

  /**
   * Cancel file upload
   * ### Functional Description: Abort (chunked) upload via Upload Share.  ### Precondition: Valid Upload ID.  ### Effects: Aborts upload and invalidates upload ID / token.  ### &amp;#9432; Further Information: None.
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> cancelShareUploadWithHttpInfo(String accessKey, String uploadId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling cancelShareUpload");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling cancelShareUpload");
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


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Complete S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Finishes a S3 file upload and closes the corresponding upload channel.  ### Precondition: * An upload channel has been created and data has been transmitted * Valid upload ID    If container is encrypted, the fileKeys can only be set for users that have permission _\&quot;manage\&quot;_, _\&quot;read\&quot;_, _\&quot;manageDownloadShare\&quot;_ or _\&quot;manageUploadShare\&quot;_.  ### Effects: Upload channel is closed. S3 multipart upload request is completed.  ### &amp;#9432; Further Information: None. 
   * @param accessKey Access key (required)
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @throws ApiException if fails to make API call
   */
  public void completeS3ShareUpload(String accessKey, CompleteS3ShareUploadRequest body, String uploadId) throws ApiException {

    completeS3ShareUploadWithHttpInfo(accessKey, body, uploadId);
  }

  /**
   * Complete S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Finishes a S3 file upload and closes the corresponding upload channel.  ### Precondition: * An upload channel has been created and data has been transmitted * Valid upload ID    If container is encrypted, the fileKeys can only be set for users that have permission _\&quot;manage\&quot;_, _\&quot;read\&quot;_, _\&quot;manageDownloadShare\&quot;_ or _\&quot;manageUploadShare\&quot;_.  ### Effects: Upload channel is closed. S3 multipart upload request is completed.  ### &amp;#9432; Further Information: None. 
   * @param accessKey Access key (required)
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> completeS3ShareUploadWithHttpInfo(String accessKey, CompleteS3ShareUploadRequest body, String uploadId) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling completeS3ShareUpload");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling completeS3ShareUpload");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling completeS3ShareUpload");
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


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Complete file upload
   * ### Functional Description: Finalize (chunked) upload via Upload Share.  ### Precondition: * Valid upload ID.    If container is encrypted, the fileKeys can only be set for users that have permission _\&quot;manage\&quot;_, _\&quot;read\&quot;_, _\&quot;manageDownloadShare\&quot;_ or _\&quot;manageUploadShare\&quot;_.  ### Effects: Finalizes upload.  ### &amp;#9432; Further Information: Chunked uploads (range requests) are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).    Please ensure that all chunks have been transferred correctly before finishing the upload.   If file hash has been created in time a &#x60;201 Created&#x60; will be responded and hash will be part of response, otherwise it will be a &#x60;202 Accepted&#x60; without it.  ### 200 OK is NOT used by this API 
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @param body Mandatory for encrypted shares (optional)
   * @return PublicUploadedFileData
   * @throws ApiException if fails to make API call
   */
  public PublicUploadedFileData completeShareUpload(String accessKey, String uploadId, UserFileKeyList body) throws ApiException {
    return completeShareUploadWithHttpInfo(accessKey, uploadId, body).getData();
      }

  /**
   * Complete file upload
   * ### Functional Description: Finalize (chunked) upload via Upload Share.  ### Precondition: * Valid upload ID.    If container is encrypted, the fileKeys can only be set for users that have permission _\&quot;manage\&quot;_, _\&quot;read\&quot;_, _\&quot;manageDownloadShare\&quot;_ or _\&quot;manageUploadShare\&quot;_.  ### Effects: Finalizes upload.  ### &amp;#9432; Further Information: Chunked uploads (range requests) are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).    Please ensure that all chunks have been transferred correctly before finishing the upload.   If file hash has been created in time a &#x60;201 Created&#x60; will be responded and hash will be part of response, otherwise it will be a &#x60;202 Accepted&#x60; without it.  ### 200 OK is NOT used by this API 
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @param body Mandatory for encrypted shares (optional)
   * @return ApiResponse&lt;PublicUploadedFileData&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicUploadedFileData> completeShareUploadWithHttpInfo(String accessKey, String uploadId, UserFileKeyList body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling completeShareUpload");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling completeShareUpload");
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicUploadedFileData> localVarReturnType = new GenericType<PublicUploadedFileData>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Generate download URL
   * ### Functional Description: Generate a download URL to retrieve a shared file.  ### Precondition: None.  ### Effects: Download URL and token are generated and returned.  ### &amp;#9432; Further Information: Use **&#x60;downloadUrl&#x60;** the download **&#x60;token&#x60;** is **&#x60;DEPRECATED&#x60;**.
   * @param accessKey Access key (required)
   * @param body body (required)
   * @return PublicDownloadTokenGenerateResponse
   * @throws ApiException if fails to make API call
   */
  public PublicDownloadTokenGenerateResponse createPublicDownloadShareToken(String accessKey, PublicDownloadTokenGenerateRequest body) throws ApiException {
    return createPublicDownloadShareTokenWithHttpInfo(accessKey, body).getData();
      }

  /**
   * Generate download URL
   * ### Functional Description: Generate a download URL to retrieve a shared file.  ### Precondition: None.  ### Effects: Download URL and token are generated and returned.  ### &amp;#9432; Further Information: Use **&#x60;downloadUrl&#x60;** the download **&#x60;token&#x60;** is **&#x60;DEPRECATED&#x60;**.
   * @param accessKey Access key (required)
   * @param body body (required)
   * @return ApiResponse&lt;PublicDownloadTokenGenerateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicDownloadTokenGenerateResponse> createPublicDownloadShareTokenWithHttpInfo(String accessKey, PublicDownloadTokenGenerateRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling createPublicDownloadShareToken");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createPublicDownloadShareToken");
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
   * Create new file upload channel
   * ### Functional Description:   Create a new upload channel.  ### Precondition: None.  ### Effects: Upload channel is created and corresponding upload URL, token &amp; upload ID are returned.  ### &amp;#9432; Further Information: Use **&#x60;uploadUrl&#x60;** the upload **&#x60;token&#x60;** is **&#x60;DEPRECATED&#x60;**.    Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param accessKey Access key (required)
   * @param body body (required)
   * @return CreateShareUploadChannelResponse
   * @throws ApiException if fails to make API call
   */
  public CreateShareUploadChannelResponse createShareUpload(String accessKey, CreateShareUploadChannelRequest body) throws ApiException {
    return createShareUploadWithHttpInfo(accessKey, body).getData();
      }

  /**
   * Create new file upload channel
   * ### Functional Description:   Create a new upload channel.  ### Precondition: None.  ### Effects: Upload channel is created and corresponding upload URL, token &amp; upload ID are returned.  ### &amp;#9432; Further Information: Use **&#x60;uploadUrl&#x60;** the upload **&#x60;token&#x60;** is **&#x60;DEPRECATED&#x60;**.    Please provide the size of the intended upload so that the quota can be checked in advanced and no data is transferred unnecessarily.  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param accessKey Access key (required)
   * @param body body (required)
   * @return ApiResponse&lt;CreateShareUploadChannelResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CreateShareUploadChannelResponse> createShareUploadWithHttpInfo(String accessKey, CreateShareUploadChannelRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling createShareUpload");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createShareUpload");
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
   * Generate presigned URLs for S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Generate presigned URLs for S3 file upload.  ### Precondition: Valid upload ID  ### Effects: List of presigned URLs is returned.  ### &amp;#9432; Further Information: The size for each part must be &gt;&#x3D; 5 MB, except for the last part.   The part number of the first part in S3 is 1 (not 0).   Use HTTP method &#x60;PUT&#x60; for uploading bytes via presigned URL.
   * @param accessKey Access key (required)
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return PresignedUrlList
   * @throws ApiException if fails to make API call
   */
  public PresignedUrlList generatePresignedUrlsShares(String accessKey, GeneratePresignedUrlsRequest body, String uploadId, String xSdsAuthToken) throws ApiException {
    return generatePresignedUrlsSharesWithHttpInfo(accessKey, body, uploadId, xSdsAuthToken).getData();
      }

  /**
   * Generate presigned URLs for S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Generate presigned URLs for S3 file upload.  ### Precondition: Valid upload ID  ### Effects: List of presigned URLs is returned.  ### &amp;#9432; Further Information: The size for each part must be &gt;&#x3D; 5 MB, except for the last part.   The part number of the first part in S3 is 1 (not 0).   Use HTTP method &#x60;PUT&#x60; for uploading bytes via presigned URL.
   * @param accessKey Access key (required)
   * @param body body (required)
   * @param uploadId Upload channel ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;PresignedUrlList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PresignedUrlList> generatePresignedUrlsSharesWithHttpInfo(String accessKey, GeneratePresignedUrlsRequest body, String uploadId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling generatePresignedUrlsShares");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling generatePresignedUrlsShares");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling generatePresignedUrlsShares");
    }
    
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}/{upload_id}/s3_urls"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()))
      .replaceAll("\\{" + "upload_id" + "\\}", apiClient.escapeString(uploadId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
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
   * Get Active Directory authentication information
   * ### Functional Description:   Provides information about Active Directory authentication options.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param isGlobalAvailable Show only global available items (optional)
   * @return ActiveDirectoryAuthInfo
   * @throws ApiException if fails to make API call
   */
  public ActiveDirectoryAuthInfo getActiveDirectoryAuthInfo(Boolean isGlobalAvailable) throws ApiException {
    return getActiveDirectoryAuthInfoWithHttpInfo(isGlobalAvailable).getData();
      }

  /**
   * Get Active Directory authentication information
   * ### Functional Description:   Provides information about Active Directory authentication options.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param isGlobalAvailable Show only global available items (optional)
   * @return ApiResponse&lt;ActiveDirectoryAuthInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ActiveDirectoryAuthInfo> getActiveDirectoryAuthInfoWithHttpInfo(Boolean isGlobalAvailable) throws ApiException {
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
   * Get OpenID Connect provider authentication information
   * ### Functional Description:   Provides information about OpenID Connect authentication options.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param isGlobalAvailable Show only global available items (optional)
   * @return OpenIdAuthInfo
   * @throws ApiException if fails to make API call
   */
  public OpenIdAuthInfo getOpenIdAuthInfo(Boolean isGlobalAvailable) throws ApiException {
    return getOpenIdAuthInfoWithHttpInfo(isGlobalAvailable).getData();
      }

  /**
   * Get OpenID Connect provider authentication information
   * ### Functional Description:   Provides information about OpenID Connect authentication options.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param isGlobalAvailable Show only global available items (optional)
   * @return ApiResponse&lt;OpenIdAuthInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OpenIdAuthInfo> getOpenIdAuthInfoWithHttpInfo(Boolean isGlobalAvailable) throws ApiException {
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
   * Get public Download Share information
   * ### Functional Description:   Retrieve the public information of a Download Share.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param accessKey Access key (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return PublicDownloadShare
   * @throws ApiException if fails to make API call
   */
  public PublicDownloadShare getPublicDownloadShare(String accessKey, String xSdsDateFormat) throws ApiException {
    return getPublicDownloadShareWithHttpInfo(accessKey, xSdsDateFormat).getData();
      }

  /**
   * Get public Download Share information
   * ### Functional Description:   Retrieve the public information of a Download Share.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param accessKey Access key (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;PublicDownloadShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicDownloadShare> getPublicDownloadShareWithHttpInfo(String accessKey, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling getPublicDownloadShare");
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
   * Download file with token
   * ### Functional Description:   Download a file (or zip archive if target is a folder or room).  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).   Range requests are not allowed for zip archive download.
   * @param accessKey Access key (required)
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getPublicFileData(String accessKey, String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    return getPublicFileDataWithHttpInfo(accessKey, token, range, genericMimetype, inline).getData();
      }

  /**
   * Download file with token
   * ### Functional Description:   Download a file (or zip archive if target is a folder or room).  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).   Range requests are not allowed for zip archive download.
   * @param accessKey Access key (required)
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> getPublicFileDataWithHttpInfo(String accessKey, String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling getPublicFileData");
    }
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling getPublicFileData");
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

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download file with token
   * ### Functional Description:   Download a file (or zip archive if target is a folder or room).  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).   Range requests are not allowed for zip archive download.
   * @param accessKey Access key (required)
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getPublicFileData1(String accessKey, String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    return getPublicFileData1WithHttpInfo(accessKey, token, range, genericMimetype, inline).getData();
      }

  /**
   * Download file with token
   * ### Functional Description:   Download a file (or zip archive if target is a folder or room).  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).   Range requests are not allowed for zip archive download.
   * @param accessKey Access key (required)
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> getPublicFileData1WithHttpInfo(String accessKey, String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling getPublicFileData1");
    }
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling getPublicFileData1");
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get public Upload Share information
   * ### Functional Description:   Provides information about the desired Upload Share.  ### Precondition: Only userUserPublicKeyList is returned to the users who owns one of the following permissions * *&#x60;manage&#x60;* * *&#x60;read&#x60;* * *&#x60;manageDownloadShare&#x60;* * *&#x60;manageUploadShare&#x60;*  ### Effects: None.  ### &amp;#9432; Further Information: If no password is set, the returned information is reduced to the following attributes (if available):  * **&#x60;name&#x60;** * **&#x60;maxSlots&#x60;** * **&#x60;createdAt&#x60;** * **&#x60;isProtected&#x60;** * **&#x60;isEncrypted&#x60;** * **&#x60;showUploadedFiles&#x60;** * **&#x60;userUserPublicKeyList&#x60;** (if parent is end-to-end encrypted)  Only if the password is transmitted as &#x60;X-Sds-Share-Password&#x60; header, all values are returned. 
   * @param accessKey Access key (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsSharePassword Upload share password. Should be base64-encoded. Plain X-Sds-Share-Passwords are *deprecated* and will be removed in the future (optional)
   * @return PublicUploadShare
   * @throws ApiException if fails to make API call
   */
  public PublicUploadShare getPublicUploadShare(String accessKey, String xSdsDateFormat, String xSdsSharePassword) throws ApiException {
    return getPublicUploadShareWithHttpInfo(accessKey, xSdsDateFormat, xSdsSharePassword).getData();
      }

  /**
   * Get public Upload Share information
   * ### Functional Description:   Provides information about the desired Upload Share.  ### Precondition: Only userUserPublicKeyList is returned to the users who owns one of the following permissions * *&#x60;manage&#x60;* * *&#x60;read&#x60;* * *&#x60;manageDownloadShare&#x60;* * *&#x60;manageUploadShare&#x60;*  ### Effects: None.  ### &amp;#9432; Further Information: If no password is set, the returned information is reduced to the following attributes (if available):  * **&#x60;name&#x60;** * **&#x60;maxSlots&#x60;** * **&#x60;createdAt&#x60;** * **&#x60;isProtected&#x60;** * **&#x60;isEncrypted&#x60;** * **&#x60;showUploadedFiles&#x60;** * **&#x60;userUserPublicKeyList&#x60;** (if parent is end-to-end encrypted)  Only if the password is transmitted as &#x60;X-Sds-Share-Password&#x60; header, all values are returned. 
   * @param accessKey Access key (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsSharePassword Upload share password. Should be base64-encoded. Plain X-Sds-Share-Passwords are *deprecated* and will be removed in the future (optional)
   * @return ApiResponse&lt;PublicUploadShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicUploadShare> getPublicUploadShareWithHttpInfo(String accessKey, String xSdsDateFormat, String xSdsSharePassword) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling getPublicUploadShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/public/shares/uploads/{access_key}"
      .replaceAll("\\{" + "access_key" + "\\}", apiClient.escapeString(accessKey.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsSharePassword != null)
      localVarHeaderParams.put("X-Sds-Share-Password", apiClient.parameterToString(xSdsSharePassword));

    
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
   * Get system time
   * ### Functional Description:   Retrieve the actual server time.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return SdsServerTime
   * @throws ApiException if fails to make API call
   */
  public SdsServerTime getSdsServerTime(String xSdsDateFormat) throws ApiException {
    return getSdsServerTimeWithHttpInfo(xSdsDateFormat).getData();
      }

  /**
   * Get system time
   * ### Functional Description:   Retrieve the actual server time.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;SdsServerTime&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SdsServerTime> getSdsServerTimeWithHttpInfo(String xSdsDateFormat) throws ApiException {
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
   * Get software version information
   * ### Functional Description:   Public software version information.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: The version of DRACOON Server consists of two components: * **API** * **Core** (referred to as _\&quot;Server\&quot;_)  that are versioned individually.
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return SoftwareVersionData
   * @throws ApiException if fails to make API call
   */
  public SoftwareVersionData getSoftwareVersion(String xSdsDateFormat) throws ApiException {
    return getSoftwareVersionWithHttpInfo(xSdsDateFormat).getData();
      }

  /**
   * Get software version information
   * ### Functional Description:   Public software version information.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: The version of DRACOON Server consists of two components: * **API** * **Core** (referred to as _\&quot;Server\&quot;_)  that are versioned individually.
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;SoftwareVersionData&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SoftwareVersionData> getSoftwareVersionWithHttpInfo(String xSdsDateFormat) throws ApiException {
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
   * Get system information
   * ### Functional Description:   Provides information about system.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: Authentication methods are sorted by **priority** attribute.   Smaller values have higher priority.   Authentication method with highest priority is considered as default.  ### System information  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; |  ### Authentication methods  | Authentication Method | Description | | :--- | :--- | | **&#x60;basic&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their credentials stored in the database.&lt;br&gt;Formerly known as **&#x60;sql&#x60;**. | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their Active Directory credentials. | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their RADIUS username, their PIN and a token password. | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.This option **MUST** be activated to allow users to log in with their OpenID Connect identity. | | **&#x60;hideLoginInputFields&#x60;** | Determines whether input fields for login should be enabled | &#x60;true or false&#x60; | 
   * @param isEnabled Show only enabled authentication methods (optional)
   * @return SystemInfo
   * @throws ApiException if fails to make API call
   */
  public SystemInfo getSystemInfo(Boolean isEnabled) throws ApiException {
    return getSystemInfoWithHttpInfo(isEnabled).getData();
      }

  /**
   * Get system information
   * ### Functional Description:   Provides information about system.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: Authentication methods are sorted by **priority** attribute.   Smaller values have higher priority.   Authentication method with highest priority is considered as default.  ### System information  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;languageDefault&#x60;** | Defines which language should be default.&lt;br&gt;cf. [RFC 5646](https://tools.ietf.org/html/rfc5646) for ISO 639-1 codes | &#x60;ISO 639-1 code&#x60; |  ### Authentication methods  | Authentication Method | Description | | :--- | :--- | | **&#x60;basic&#x60;** | **Basic** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their credentials stored in the database.&lt;br&gt;Formerly known as **&#x60;sql&#x60;**. | | **&#x60;active_directory&#x60;** | **Active Directory** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their Active Directory credentials. | | **&#x60;radius&#x60;** | **RADIUS** authentication globally allowed.&lt;br&gt;This option **MUST** be activated to allow users to log in with their RADIUS username, their PIN and a token password. | | **&#x60;openid&#x60;** | **OpenID Connect** authentication globally allowed.This option **MUST** be activated to allow users to log in with their OpenID Connect identity. | | **&#x60;hideLoginInputFields&#x60;** | Determines whether input fields for login should be enabled | &#x60;true or false&#x60; | 
   * @param isEnabled Show only enabled authentication methods (optional)
   * @return ApiResponse&lt;SystemInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SystemInfo> getSystemInfoWithHttpInfo(Boolean isEnabled) throws ApiException {
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
   * Get third-party software dependencies
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Provides information about used third-party software dependencies.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: List of the third-party software dependencies used by **DRACOON Core** (referred to as _\&quot;Server\&quot;_):  
   * @return List&lt;ThirdPartyDependenciesData&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ThirdPartyDependenciesData> getThirdPartyDependencies() throws ApiException {
    return getThirdPartyDependenciesWithHttpInfo().getData();
      }

  /**
   * Get third-party software dependencies
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Provides information about used third-party software dependencies.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: List of the third-party software dependencies used by **DRACOON Core** (referred to as _\&quot;Server\&quot;_):  
   * @return ApiResponse&lt;List&lt;ThirdPartyDependenciesData&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<ThirdPartyDependenciesData>> getThirdPartyDependenciesWithHttpInfo() throws ApiException {
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
   * Request status of S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Request status of a S3 file upload.  ### Precondition: An upload channel has been created. * User has _\&quot;create\&quot;_ permissions in the parent container (room or folder). * Valid upload ID.  ### Effects: None.  ### &amp;#9432; Further Information:  ### Possible errors:  | Http Status | Error Code | Description | | :--- | :--- | :--- | | **&#x60;400 Bad Request&#x60;** | &#x60;-80000&#x60; | Mandatory fields cannot be empty | | **&#x60;400 Bad Request&#x60;** | &#x60;-80001&#x60; | Invalid positive number | | **&#x60;400 Bad Request&#x60;** | &#x60;-80002&#x60; | Invalid number | | **&#x60;400 Bad Request&#x60;** | &#x60;-40001&#x60; | (Target) room is not encrypted | | **&#x60;400 Bad Request&#x60;** | &#x60;-40755&#x60; | Bad file name | | **&#x60;400 Bad Request&#x60;** | &#x60;-40763&#x60; | File key must be set for an upload into encrypted room | | **&#x60;400 Bad Request&#x60;** | &#x60;-50506&#x60; | Exceeds the number of files for this Upload Share | | **&#x60;403 Forbidden&#x60;** |  | Access denied | | **&#x60;404 Not Found&#x60;** | &#x60;-20501&#x60; | Upload not found | | **&#x60;404 Not Found&#x60;** | &#x60;-40000&#x60; | Container not found | | **&#x60;404 Not Found&#x60;** | &#x60;-41000&#x60; | Node not found | | **&#x60;404 Not Found&#x60;** | &#x60;-70501&#x60; | User not found | | **&#x60;409 Conflict&#x60;** | &#x60;-40010&#x60; | Container cannot be overwritten | | **&#x60;409 Conflict&#x60;** |  | File cannot be overwritten | | **&#x60;500 Internal Server Error&#x60;** |  | System Error | | **&#x60;502 Bad Gateway&#x60;** |  | S3 Error | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-50504&#x60; | Exceeds the quota for this Upload Share | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-40200&#x60; | Exceeds the free node quota in room | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90200&#x60; | Exceeds the free customer quota | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90201&#x60; | Exceeds the free customer physical disk space | 
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @return S3ShareUploadStatus
   * @throws ApiException if fails to make API call
   */
  public S3ShareUploadStatus getUploadStatusShares(String accessKey, String uploadId) throws ApiException {
    return getUploadStatusSharesWithHttpInfo(accessKey, uploadId).getData();
      }

  /**
   * Request status of S3 file upload 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.15.0  ### Functional Description: Request status of a S3 file upload.  ### Precondition: An upload channel has been created. * User has _\&quot;create\&quot;_ permissions in the parent container (room or folder). * Valid upload ID.  ### Effects: None.  ### &amp;#9432; Further Information:  ### Possible errors:  | Http Status | Error Code | Description | | :--- | :--- | :--- | | **&#x60;400 Bad Request&#x60;** | &#x60;-80000&#x60; | Mandatory fields cannot be empty | | **&#x60;400 Bad Request&#x60;** | &#x60;-80001&#x60; | Invalid positive number | | **&#x60;400 Bad Request&#x60;** | &#x60;-80002&#x60; | Invalid number | | **&#x60;400 Bad Request&#x60;** | &#x60;-40001&#x60; | (Target) room is not encrypted | | **&#x60;400 Bad Request&#x60;** | &#x60;-40755&#x60; | Bad file name | | **&#x60;400 Bad Request&#x60;** | &#x60;-40763&#x60; | File key must be set for an upload into encrypted room | | **&#x60;400 Bad Request&#x60;** | &#x60;-50506&#x60; | Exceeds the number of files for this Upload Share | | **&#x60;403 Forbidden&#x60;** |  | Access denied | | **&#x60;404 Not Found&#x60;** | &#x60;-20501&#x60; | Upload not found | | **&#x60;404 Not Found&#x60;** | &#x60;-40000&#x60; | Container not found | | **&#x60;404 Not Found&#x60;** | &#x60;-41000&#x60; | Node not found | | **&#x60;404 Not Found&#x60;** | &#x60;-70501&#x60; | User not found | | **&#x60;409 Conflict&#x60;** | &#x60;-40010&#x60; | Container cannot be overwritten | | **&#x60;409 Conflict&#x60;** |  | File cannot be overwritten | | **&#x60;500 Internal Server Error&#x60;** |  | System Error | | **&#x60;502 Bad Gateway&#x60;** |  | S3 Error | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-50504&#x60; | Exceeds the quota for this Upload Share | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-40200&#x60; | Exceeds the free node quota in room | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90200&#x60; | Exceeds the free customer quota | | **&#x60;502 Insufficient Storage&#x60;** | &#x60;-90201&#x60; | Exceeds the free customer physical disk space | 
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @return ApiResponse&lt;S3ShareUploadStatus&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3ShareUploadStatus> getUploadStatusSharesWithHttpInfo(String accessKey, String uploadId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling getUploadStatusShares");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling getUploadStatusShares");
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
   * ### Functional Description:   Chunked upload of files via Upload Share.  ### Precondition: Valid upload ID.  ### Effects: Chunk of file is uploaded.  ### &amp;#9432; Further Information: Chunked uploads (range requests) are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).  Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;    For both file upload types set the correct &#x60;Content-Type&#x60; header and body.   Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/public/shares/uploads/{access_key}{upload_id} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;   &#x60;&#x60;&#x60; POST /api/v4/public/shares/uploads/{access_key}{upload_id} HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60;
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @param contentRange Content-Range e.g. &#x60;bytes 0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param file File (optional)
   * @return ChunkUploadResponse
   * @throws ApiException if fails to make API call
   */
  public ChunkUploadResponse uploadShare(String accessKey, String uploadId, String contentRange, String xSdsDateFormat, File file) throws ApiException {
    return uploadShareWithHttpInfo(accessKey, uploadId, contentRange, xSdsDateFormat, file).getData();
      }

  /**
   * Upload file
   * ### Functional Description:   Chunked upload of files via Upload Share.  ### Precondition: Valid upload ID.  ### Effects: Chunk of file is uploaded.  ### &amp;#9432; Further Information: Chunked uploads (range requests) are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).  Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;    For both file upload types set the correct &#x60;Content-Type&#x60; header and body.   Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/public/shares/uploads/{access_key}{upload_id} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;   &#x60;&#x60;&#x60; POST /api/v4/public/shares/uploads/{access_key}{upload_id} HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60;
   * @param accessKey Access key (required)
   * @param uploadId Upload channel ID (required)
   * @param contentRange Content-Range e.g. &#x60;bytes 0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param file File (optional)
   * @return ApiResponse&lt;ChunkUploadResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ChunkUploadResponse> uploadShareWithHttpInfo(String accessKey, String uploadId, String contentRange, String xSdsDateFormat, File file) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'accessKey' is set
    if (accessKey == null) {
      throw new ApiException(400, "Missing the required parameter 'accessKey' when calling uploadShare");
    }
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadShare");
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
