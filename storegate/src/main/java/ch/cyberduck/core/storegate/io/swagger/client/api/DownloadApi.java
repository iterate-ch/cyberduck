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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
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
   * Download or stream a file or file version              HEAD only returns headers, no data
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
   * Download or stream a file or file version              HEAD only returns headers, no data
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
    String localVarPath = "/v4.2/download/files/{id}"
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
   * Download or stream a file from a share              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param stream Stream the file data instead of download (optional)
   * @param toPDF Try to convert the file to PDF (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadFileShare(String shareid, String id, Boolean stream, Boolean toPDF) throws ApiException {
    return downloadDownloadFileShareWithHttpInfo(shareid, id, stream, toPDF).getData();
      }

  /**
   * Download or stream a file from a share              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param stream Stream the file data instead of download (optional)
   * @param toPDF Try to convert the file to PDF (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadFileShareWithHttpInfo(String shareid, String id, Boolean stream, Boolean toPDF) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadFileShare");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadDownloadFileShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/files/{id}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toPDF", toPDF));

    
    
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
   * Download or stream a file from a share              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param stream Stream the file data instead of download (optional)
   * @param toPDF Try to convert the file to PDF (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadFileShare_0(String shareid, String id, Boolean stream, Boolean toPDF) throws ApiException {
    return downloadDownloadFileShare_0WithHttpInfo(shareid, id, stream, toPDF).getData();
      }

  /**
   * Download or stream a file from a share              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param id The file id (required)
   * @param stream Stream the file data instead of download (optional)
   * @param toPDF Try to convert the file to PDF (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadFileShare_0WithHttpInfo(String shareid, String id, Boolean stream, Boolean toPDF) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadFileShare_0");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadDownloadFileShare_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/files/{id}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toPDF", toPDF));

    
    
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
   * Download or stream a file or file version              HEAD only returns headers, no data
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
   * Download or stream a file or file version              HEAD only returns headers, no data
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
    String localVarPath = "/v4.2/download/files/{id}"
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
   * Download or stream an image (media) from an album              HEAD only returns headers, no data
   * 
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMedia(String mediaid, Boolean stream) throws ApiException {
    return downloadDownloadMediaWithHttpInfo(mediaid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an album              HEAD only returns headers, no data
   * 
   * @param mediaid The image (media) id (required)
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
    String localVarPath = "/v4.2/download/media/{mediaid}"
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
   * Download or stream an image (media) from an shared album              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMediaShare(String shareid, String mediaid, Boolean stream) throws ApiException {
    return downloadDownloadMediaShareWithHttpInfo(shareid, mediaid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an shared album              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadMediaShareWithHttpInfo(String shareid, String mediaid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadMediaShare");
    }
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadDownloadMediaShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/media/{mediaid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
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
   * Download or stream an image (media) from an shared album              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMediaShare_0(String shareid, String mediaid, Boolean stream) throws ApiException {
    return downloadDownloadMediaShare_0WithHttpInfo(shareid, mediaid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an shared album              HEAD only returns headers, no data
   * 
   * @param shareid The share id (required)
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadMediaShare_0WithHttpInfo(String shareid, String mediaid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadMediaShare_0");
    }
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadDownloadMediaShare_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/media/{mediaid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
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
   * Download or stream an image (media) from an album              HEAD only returns headers, no data
   * 
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadMedia_0(String mediaid, Boolean stream) throws ApiException {
    return downloadDownloadMedia_0WithHttpInfo(mediaid, stream).getData();
      }

  /**
   * Download or stream an image (media) from an album              HEAD only returns headers, no data
   * 
   * @param mediaid The image (media) id (required)
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
    String localVarPath = "/v4.2/download/media/{mediaid}"
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
   * Download or stream using a one time download id
   * 
   * @param downloadid The one time download id (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadWithToken(String downloadid) throws ApiException {
    return downloadDownloadWithTokenWithHttpInfo(downloadid).getData();
      }

  /**
   * Download or stream using a one time download id
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
    String localVarPath = "/v4.2/download/{downloadid}"
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
   * Download a share as a zip file
   * 
   * @param shareid The share id (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadDownloadZipShare(String shareid) throws ApiException {
    return downloadDownloadZipShareWithHttpInfo(shareid).getData();
      }

  /**
   * Download a share as a zip file
   * 
   * @param shareid The share id (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadDownloadZipShareWithHttpInfo(String shareid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadDownloadZipShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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
   * Get one time download id for multiple albums zipped
   * 
   * @param request The download request (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadAlbumToken(DownloadRequest request) throws ApiException {
    return downloadGetDownloadAlbumTokenWithHttpInfo(request).getData();
      }

  /**
   * Get one time download id for multiple albums zipped
   * 
   * @param request The download request (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadAlbumTokenWithHttpInfo(DownloadRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling downloadGetDownloadAlbumToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/album";

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
    String localVarPath = "/v4.2/download/files/{id}"
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
   * Get one time download id for a file
   * 
   * @param shareid The file id (required)
   * @param id The file id (required)
   * @param stream Stream the file data instead of download (optional)
   * @param toPDF Try to convert file to PDF (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadFileTokenShare(String shareid, String id, Boolean stream, Boolean toPDF) throws ApiException {
    return downloadGetDownloadFileTokenShareWithHttpInfo(shareid, id, stream, toPDF).getData();
      }

  /**
   * Get one time download id for a file
   * 
   * @param shareid The file id (required)
   * @param id The file id (required)
   * @param stream Stream the file data instead of download (optional)
   * @param toPDF Try to convert file to PDF (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadFileTokenShareWithHttpInfo(String shareid, String id, Boolean stream, Boolean toPDF) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadGetDownloadFileTokenShare");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling downloadGetDownloadFileTokenShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/files/{id}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream", stream));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toPDF", toPDF));

    
    
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
    String localVarPath = "/v4.2/download/files";

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
   * @param request The download request (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadFilesTokenShare(String shareid, DownloadRequest request) throws ApiException {
    return downloadGetDownloadFilesTokenShareWithHttpInfo(shareid, request).getData();
      }

  /**
   * Get one time download id for zipped files and folders
   * 
   * @param shareid The share id (required)
   * @param request The download request (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadFilesTokenShareWithHttpInfo(String shareid, DownloadRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadGetDownloadFilesTokenShare");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling downloadGetDownloadFilesTokenShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/files"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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
   * Get one time download id for an image (media)
   * 
   * @param mediaid The image (media) id (required)
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
   * @param mediaid The image (media) id (required)
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
    String localVarPath = "/v4.2/download/media/{mediaid}"
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
   * Get one time download id for an image (media)
   * 
   * @param shareid The image (media) id (required)
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadMediaTokenShare(String shareid, String mediaid, Boolean stream) throws ApiException {
    return downloadGetDownloadMediaTokenShareWithHttpInfo(shareid, mediaid, stream).getData();
      }

  /**
   * Get one time download id for an image (media)
   * 
   * @param shareid The image (media) id (required)
   * @param mediaid The image (media) id (required)
   * @param stream Stream the file data instead of download (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadMediaTokenShareWithHttpInfo(String shareid, String mediaid, Boolean stream) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadGetDownloadMediaTokenShare");
    }
    
    // verify the required parameter 'mediaid' is set
    if (mediaid == null) {
      throw new ApiException(400, "Missing the required parameter 'mediaid' when calling downloadGetDownloadMediaTokenShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/media/{mediaid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()))
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
   * @param shareid The share id (required)
   * @param request The download request (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadMediaTokenShare_0(String shareid, DownloadRequest request) throws ApiException {
    return downloadGetDownloadMediaTokenShare_0WithHttpInfo(shareid, request).getData();
      }

  /**
   * Get one time download id for zipped images (media)
   * 
   * @param shareid The share id (required)
   * @param request The download request (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadMediaTokenShare_0WithHttpInfo(String shareid, DownloadRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadGetDownloadMediaTokenShare_0");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling downloadGetDownloadMediaTokenShare_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}/media"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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
    String localVarPath = "/v4.2/download/media";

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
   * Get one time download id for a share as a zip file
   * 
   * @param shareid The share id (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadGetDownloadZipTokenShare(String shareid) throws ApiException {
    return downloadGetDownloadZipTokenShareWithHttpInfo(shareid).getData();
      }

  /**
   * Get one time download id for a share as a zip file
   * 
   * @param shareid The share id (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> downloadGetDownloadZipTokenShareWithHttpInfo(String shareid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling downloadGetDownloadZipTokenShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/download/shares/{shareid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
