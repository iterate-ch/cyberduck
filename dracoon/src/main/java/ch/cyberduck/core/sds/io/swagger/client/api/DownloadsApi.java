package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.11.0&lt;/h3&gt;  ### Description: Download avatar for given user ID and UUID.  ### Precondition: Valid UUID.  ### Postcondition: Stream is returned.  ### Further Information: None.
   * @param userId User ID (required)
   * @param uuid UUID of the avatar (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String downloadAvatar(Long userId, String uuid) throws ApiException {
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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Download file
   * ### Description: Download a file.  ### Precondition: Valid download token.  ### Postcondition: Stream is returned.  ### Further Information: Range requests are supported.
   * @param token Download token (required)
   * @param range Range   e.g. &#x60;bytes&#x3D;0-999&#x60; (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @throws ApiException if fails to make API call
   * Range Requests
   * @see <a href="https://tools.ietf.org/html/rfc7233">Download file Documentation</a>
   */
  public void downloadFileViaToken(String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling downloadFileViaToken");
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

    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Download file
   * ### Description: Download a file.  ### Precondition: Valid download token.  ### Postcondition: Stream is returned.  ### Further Information: Range requests are supported.
   * @param token Download token (required)
   * @param range Range   e.g. &#x60;bytes&#x3D;0-999&#x60; (optional)
   * @param genericMimetype Always return &#x60;application/octet-stream&#x60; instead of specific mimetype (optional)
   * @param inline Use Content-Disposition: &#x60;inline&#x60; instead of &#x60;attachment&#x60; (optional)
   * @throws ApiException if fails to make API call
   * Range Requests
   * @see <a href="https://tools.ietf.org/html/rfc7233">Download file Documentation</a>
   */
  public void downloadFileViaToken1(String token, String range, Boolean genericMimetype, Boolean inline) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling downloadFileViaToken1");
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

    apiClient.invokeAPI(localVarPath, "HEAD", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Download ZIP archive
   * ### Description: Download multiple files in a ZIP archive.  ### Precondition: Valid download token.  ### Postcondition: Stream is returned.  ### Further Information: Create a download token with &#x60;POST /nodes/zip&#x60; API.
   * @param token Download token (required)
   * @throws ApiException if fails to make API call
   */
  public void downloadZipArchiveViaToken(String token) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling downloadZipArchiveViaToken");
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

    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
