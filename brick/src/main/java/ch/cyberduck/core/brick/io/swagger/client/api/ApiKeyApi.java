package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ApiKeyEntity;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class ApiKeyApi {
  private ApiClient apiClient;

  public ApiKeyApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ApiKeyApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete current API key.  (Requires current API connection to be using an API key.)
   * Delete current API key.  (Requires current API connection to be using an API key.)
   * @throws ApiException if fails to make API call
   */
  public void deleteCurrent() throws ApiException {

    deleteCurrentWithHttpInfo();
  }

  /**
   * Delete current API key.  (Requires current API connection to be using an API key.)
   * Delete current API key.  (Requires current API connection to be using an API key.)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteCurrentWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/api_key";

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
   * Show information about current API key.  (Requires current API connection to be using an API key.)
   * Show information about current API key.  (Requires current API connection to be using an API key.)
   * @return ApiKeyEntity
   * @throws ApiException if fails to make API call
   */
  public ApiKeyEntity findCurrent() throws ApiException {
    return findCurrentWithHttpInfo().getData();
      }

  /**
   * Show information about current API key.  (Requires current API connection to be using an API key.)
   * Show information about current API key.  (Requires current API connection to be using an API key.)
   * @return ApiResponse&lt;ApiKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiKeyEntity> findCurrentWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/api_key";

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

    GenericType<ApiKeyEntity> localVarReturnType = new GenericType<ApiKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update current API key.  (Requires current API connection to be using an API key.)
   * Update current API key.  (Requires current API connection to be using an API key.)
   * @param expiresAt API Key expiration date (optional)
   * @param name Internal name for the API Key.  For your use. (optional)
   * @param permissionSet Permissions for this API Key.  Keys with the &#x60;desktop_app&#x60; permission set only have the ability to do the functions provided in our Desktop App (File and Share Link operations).  Additional permission sets may become available in the future, such as for a Site Admin to give a key with no administrator privileges.  If you have ideas for permission sets, please let us know. (optional)
   * @return ApiKeyEntity
   * @throws ApiException if fails to make API call
   */
  public ApiKeyEntity updateCurrent(DateTime expiresAt, String name, String permissionSet) throws ApiException {
    return updateCurrentWithHttpInfo(expiresAt, name, permissionSet).getData();
      }

  /**
   * Update current API key.  (Requires current API connection to be using an API key.)
   * Update current API key.  (Requires current API connection to be using an API key.)
   * @param expiresAt API Key expiration date (optional)
   * @param name Internal name for the API Key.  For your use. (optional)
   * @param permissionSet Permissions for this API Key.  Keys with the &#x60;desktop_app&#x60; permission set only have the ability to do the functions provided in our Desktop App (File and Share Link operations).  Additional permission sets may become available in the future, such as for a Site Admin to give a key with no administrator privileges.  If you have ideas for permission sets, please let us know. (optional)
   * @return ApiResponse&lt;ApiKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiKeyEntity> updateCurrentWithHttpInfo(DateTime expiresAt, String name, String permissionSet) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/api_key";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (expiresAt != null)
      localVarFormParams.put("expires_at", expiresAt);
if (name != null)
      localVarFormParams.put("name", name);
if (permissionSet != null)
      localVarFormParams.put("permission_set", permissionSet);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ApiKeyEntity> localVarReturnType = new GenericType<ApiKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
