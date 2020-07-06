package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class DownloadsApi {
  private ApiClient apiClient;

  public DownloadsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DownloadsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Download avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description:   Download avatar for given UserID and UUID.  ### Precondition: Valid UUID.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param userId User ID (required)
   * @param uuid UUID of the avatar (required)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer downloadAvatar(Long userId, String uuid) throws ApiException {
    return downloadAvatarWithHttpInfo(userId, uuid).getData();
      }

  /**
   * Download avatar
   * ### &amp;#128640; Since version 4.11.0  ### Functional Description:   Download avatar for given UserID and UUID.  ### Precondition: Valid UUID.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param userId User ID (required)
   * @param uuid UUID of the avatar (required)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> downloadAvatarWithHttpInfo(Long userId, String uuid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling downloadAvatar");
    }
    
    // verify the required parameter 'uuid' is set
    if (uuid == null) {
      throw new ApiException(400, "Missing the required parameter 'uuid' when calling downloadAvatar");
    }
    
    // create path and map variables
    String localVarPath = "/v4/downloads/avatar/{user_id}/{uuid}"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()))
      .replaceAll("\\{" + "uuid" + "\\}", apiClient.escapeString(uuid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
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
   * Download file
   * ### Functional Description:   Download a file.  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getFileDataByToken(String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    return getFileDataByTokenWithHttpInfo(token, range, genericMimetype, inline).getData();
      }

  /**
   * Download file
   * ### Functional Description:   Download a file.  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> getFileDataByTokenWithHttpInfo(String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling getFileDataByToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/downloads/{token}"
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
   * Download file
   * ### Functional Description:   Download a file.  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getFileDataByToken1(String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    return getFileDataByToken1WithHttpInfo(token, range, genericMimetype, inline).getData();
      }

  /**
   * Download file
   * ### Functional Description:   Download a file.  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).
   * @param token Download token (required)
   * @param range Range e.g. &#x60;bytes&#x3D;0-999&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> getFileDataByToken1WithHttpInfo(String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling getFileDataByToken1");
    }
    
    // create path and map variables
    String localVarPath = "/v4/downloads/{token}"
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
   * Download ZIP file(s)
   * ### Functional Description:   Download multiple files in a ZIP archive.  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Create a download token with &#x60;POST /nodes/zip&#x60; API.
   * @param token Download token (required)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer getZipFileByToken(String token) throws ApiException {
    return getZipFileByTokenWithHttpInfo(token).getData();
      }

  /**
   * Download ZIP file(s)
   * ### Functional Description:   Download multiple files in a ZIP archive.  ### Precondition: Valid download token.  ### Effects: None.  ### &amp;#9432; Further Information: Create a download token with &#x60;POST /nodes/zip&#x60; API.
   * @param token Download token (required)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> getZipFileByTokenWithHttpInfo(String token) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling getZipFileByToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/downloads/zip/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
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
}
