package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.DownloadRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-03-25T11:22:03.779+03:00")
public class DownloadApi {
  private ApiClient apiClient;

  public DownloadApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DownloadApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Download or stream a file or file version.              HEAD only returns headers, no data.
   * 
   * @param id The file id (required)
   * @param version The file version. 0 is current version, 1 is previous version (optional)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadFile(String id, Integer version, Boolean stream) throws ApiException {
    return downloadDownloadFileWithHttpInfo(id, version, stream).getData();
      }

  /**
   * Download or stream a file or file version.              HEAD only returns headers, no data.
   * 
   * @param id The file id (required)
   * @param version The file version. 0 is current version, 1 is previous version (optional)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadFileWithHttpInfo(String id, Integer version, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadDownloadFile");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/files/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream a file from a share.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadFileShare(String shareid, String id, String accessid, Boolean stream) throws ApiException {
    return downloadDownloadFileShareWithHttpInfo(shareid, id, accessid, stream).getData();
      }

  /**
   * Download or stream a file from a share.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadFileShareWithHttpInfo(String shareid, String id, String accessid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadFileShare");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadDownloadFileShare");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling downloadDownloadFileShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/shares/{shareid}/files/{id}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream a file from a share.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadFileShare_0(String shareid, String id, String accessid, Boolean stream) throws ApiException {
    return downloadDownloadFileShare_0WithHttpInfo(shareid, id, accessid, stream).getData();
      }

  /**
   * Download or stream a file from a share.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadFileShare_0WithHttpInfo(String shareid, String id, String accessid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadFileShare_0");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadDownloadFileShare_0");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling downloadDownloadFileShare_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/shares/{shareid}/files/{id}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream a file or file version.              HEAD only returns headers, no data.
   * 
   * @param id The file id (required)
   * @param version The file version. 0 is current version, 1 is previous version (optional)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadFile_0(String id, Integer version, Boolean stream) throws ApiException {
    return downloadDownloadFile_0WithHttpInfo(id, version, stream).getData();
      }

  /**
   * Download or stream a file or file version.              HEAD only returns headers, no data.
   * 
   * @param id The file id (required)
   * @param version The file version. 0 is current version, 1 is previous version (optional)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadFile_0WithHttpInfo(String id, Integer version, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadDownloadFile_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/files/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream an image (media) from an album.              HEAD only returns headers, no data.
   * 
   * @param mediaid The imgae (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMedia(String mediaid, Boolean stream) throws ApiException {
    return downloadDownloadMediaWithHttpInfo(mediaid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an album.              HEAD only returns headers, no data.
   * 
   * @param mediaid The imgae (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadMediaWithHttpInfo(String mediaid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadDownloadMedia");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/media/{mediaid}"
      .replaceAll("\\{" + "mediaid" + "\\}", apiClient.escapeString(mediaid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream an image (media) from an shared album.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param mediaid The imgae (media) id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMediaShare(String shareid, String mediaid, String accessid, Boolean stream) throws ApiException {
    return downloadDownloadMediaShareWithHttpInfo(shareid, mediaid, accessid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an shared album.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param mediaid The imgae (media) id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadMediaShareWithHttpInfo(String shareid, String mediaid, String accessid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadMediaShare");
    }
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadDownloadMediaShare");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling downloadDownloadMediaShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/shares/{shareid}/media/{mediaid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "mediaid" + "\\}", apiClient.escapeString(mediaid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream an image (media) from an shared album.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param mediaid The imgae (media) id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMediaShare_0(String shareid, String mediaid, String accessid, Boolean stream) throws ApiException {
    return downloadDownloadMediaShare_0WithHttpInfo(shareid, mediaid, accessid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an shared album.              HEAD only returns headers, no data.
   * 
   * @param shareid The share id (required)
   * @param mediaid The imgae (media) id (required)
   * @param accessid The access id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadMediaShare_0WithHttpInfo(String shareid, String mediaid, String accessid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadMediaShare_0");
    }
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadDownloadMediaShare_0");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling downloadDownloadMediaShare_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/shares/{shareid}/media/{mediaid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "mediaid" + "\\}", apiClient.escapeString(mediaid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream an image (media) from an album.              HEAD only returns headers, no data.
   * 
   * @param mediaid The imgae (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMedia_0(String mediaid, Boolean stream) throws ApiException {
    return downloadDownloadMedia_0WithHttpInfo(mediaid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an album.              HEAD only returns headers, no data.
   * 
   * @param mediaid The imgae (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadMedia_0WithHttpInfo(String mediaid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadDownloadMedia_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/media/{mediaid}"
      .replaceAll("\\{" + "mediaid" + "\\}", apiClient.escapeString(mediaid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download or stream using a one time download id.
   * 
   * @param downloadid The one time download id (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadWithToken(String downloadid) throws ApiException {
    return downloadDownloadWithTokenWithHttpInfo(downloadid).getData();
      }

  /**
   * Download or stream using a one time download id.
   * 
   * @param downloadid The one time download id (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadWithTokenWithHttpInfo(String downloadid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'downloadid' is set
    if (downloadid == null) {
      throw new ApiException(400, "Missing the required parameter 'downloadid' when calling downloadDownloadWithToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/{downloadid}"
      .replaceAll("\\{" + "downloadid" + "\\}", apiClient.escapeString(downloadid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download a share as a zip file.
   * 
   * @param shareid The share id (required)
   * @param accessid The access id (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadZipShare(String shareid, String accessid) throws ApiException {
    return downloadDownloadZipShareWithHttpInfo(shareid, accessid).getData();
      }

  /**
   * Download a share as a zip file.
   * 
   * @param shareid The share id (required)
   * @param accessid The access id (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadZipShareWithHttpInfo(String shareid, String accessid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadZipShare");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling downloadDownloadZipShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/shares/{shareid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get one time download id for a file
   * 
   * @param id The file id (required)
   * @param version The file version. 0 is current version, 1 is previous version (optional)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadFileToken(String id, Integer version, Boolean stream) throws ApiException {
    return downloadGetDownloadFileTokenWithHttpInfo(id, version, stream).getData();
      }

  /**
   * Get one time download id for a file
   * 
   * @param id The file id (required)
   * @param version The file version. 0 is current version, 1 is previous version (optional)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadFileTokenWithHttpInfo(String id, Integer version, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadGetDownloadFileToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/files/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "version", version));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get one time download id for zipped files and folders
   * 
   * @param request The download request (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadFilesToken(DownloadRequest request) throws ApiException {
    return downloadGetDownloadFilesTokenWithHttpInfo(request).getData();
      }

  /**
   * Get one time download id for zipped files and folders
   * 
   * @param request The download request (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadFilesTokenWithHttpInfo(DownloadRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling downloadGetDownloadFilesToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/files";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get one time download id for zipped files and folders
   * 
   * @param shareid The share id (required)
   * @param accessid The access id (required)
   * @param request The download request (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadFilesToken_0(String shareid, String accessid, DownloadRequest request) throws ApiException {
    return downloadGetDownloadFilesToken_0WithHttpInfo(shareid, accessid, request).getData();
      }

  /**
   * Get one time download id for zipped files and folders
   * 
   * @param shareid The share id (required)
   * @param accessid The access id (required)
   * @param request The download request (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadFilesToken_0WithHttpInfo(String shareid, String accessid, DownloadRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadGetDownloadFilesToken_0");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling downloadGetDownloadFilesToken_0");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling downloadGetDownloadFilesToken_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/shares/{shareid}/files"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get one time download id for an image (media)
   * 
   * @param mediaid The imgae (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadMediaToken(String mediaid, Boolean stream) throws ApiException {
    return downloadGetDownloadMediaTokenWithHttpInfo(mediaid, stream).getData();
      }

  /**
   * Get one time download id for an image (media)
   * 
   * @param mediaid The imgae (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadMediaTokenWithHttpInfo(String mediaid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadGetDownloadMediaToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/media/{mediaid}"
      .replaceAll("\\{" + "mediaid" + "\\}", apiClient.escapeString(mediaid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get one time download id for zipped images (media)
   * 
   * @param request The download request (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadMediaToken_0(DownloadRequest request) throws ApiException {
    return downloadGetDownloadMediaToken_0WithHttpInfo(request).getData();
      }

  /**
   * Get one time download id for zipped images (media)
   * 
   * @param request The download request (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadMediaToken_0WithHttpInfo(DownloadRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling downloadGetDownloadMediaToken_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/media";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get one time download id for zipped images (media)
   * 
   * @param shareid The share id (required)
   * @param accessid The access id (required)
   * @param request The download request (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadMediaToken_1(String shareid, String accessid, DownloadRequest request) throws ApiException {
    return downloadGetDownloadMediaToken_1WithHttpInfo(shareid, accessid, request).getData();
      }

  /**
   * Get one time download id for zipped images (media)
   * 
   * @param shareid The share id (required)
   * @param accessid The access id (required)
   * @param request The download request (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadMediaToken_1WithHttpInfo(String shareid, String accessid, DownloadRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadGetDownloadMediaToken_1");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling downloadGetDownloadMediaToken_1");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling downloadGetDownloadMediaToken_1");
    }
    
    // create path and map variables
    String localVarPath = "/v4/download/shares/{shareid}/media"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
