package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.LockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class LocksApi {
  private ApiClient apiClient;

  public LocksApi() {
    this(Configuration.getDefaultApiClient());
  }

  public LocksApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Lock
   * Delete Lock
   * @param path Path (required)
   * @param token Lock token (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteLocksPath(String path, String token) throws ApiException {

    deleteLocksPathWithHttpInfo(path, token);
  }

  /**
   * Delete Lock
   * Delete Lock
   * @param path Path (required)
   * @param token Lock token (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteLocksPathWithHttpInfo(String path, String token) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling deleteLocksPath");
    }
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling deleteLocksPath");
    }
    
    // create path and map variables
    String localVarPath = "/locks/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "token", token));

    
    
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
   * List Locks by path
   * List Locks by path
   * @param path Path to operate on. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param includeChildren Include locks from children objects? (optional)
   * @return List&lt;LockEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<LockEntity> listForPath(String path, String cursor, Integer perPage, Boolean includeChildren) throws ApiException {
    return listForPathWithHttpInfo(path, cursor, perPage, includeChildren).getData();
      }

  /**
   * List Locks by path
   * List Locks by path
   * @param path Path to operate on. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param includeChildren Include locks from children objects? (optional)
   * @return ApiResponse&lt;List&lt;LockEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<LockEntity>> listForPathWithHttpInfo(String path, String cursor, Integer perPage, Boolean includeChildren) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling listForPath");
    }
    
    // create path and map variables
    String localVarPath = "/locks/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_children", includeChildren));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<LockEntity>> localVarReturnType = new GenericType<List<LockEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Lock
   * Create Lock
   * @param path Path (required)
   * @param timeout Lock timeout length (optional)
   * @return LockEntity
   * @throws ApiException if fails to make API call
   */
  public LockEntity postLocksPath(String path, Integer timeout) throws ApiException {
    return postLocksPathWithHttpInfo(path, timeout).getData();
      }

  /**
   * Create Lock
   * Create Lock
   * @param path Path (required)
   * @param timeout Lock timeout length (optional)
   * @return ApiResponse&lt;LockEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<LockEntity> postLocksPathWithHttpInfo(String path, Integer timeout) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling postLocksPath");
    }
    
    // create path and map variables
    String localVarPath = "/locks/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (timeout != null)
      localVarFormParams.put("timeout", timeout);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<LockEntity> localVarReturnType = new GenericType<LockEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
