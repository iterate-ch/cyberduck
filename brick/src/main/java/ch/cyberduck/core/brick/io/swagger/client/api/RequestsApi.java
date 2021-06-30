package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.RequestEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class RequestsApi {
  private ApiClient apiClient;

  public RequestsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RequestsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Request
   * Delete Request
   * @param id Request ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteRequestsId(Integer id) throws ApiException {

    deleteRequestsIdWithHttpInfo(id);
  }

  /**
   * Delete Request
   * Delete Request
   * @param id Request ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteRequestsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteRequestsId");
    }
    
    // create path and map variables
    String localVarPath = "/requests/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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
   * List Requests
   * List Requests
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;destination&#x60;. (optional)
   * @param mine Only show requests of the current user?  (Defaults to true if current user is not a site admin.) (optional)
   * @param path Path to show requests for.  If omitted, shows all paths. Send &#x60;/&#x60; to represent the root directory. (optional)
   * @return List&lt;RequestEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<RequestEntity> getRequests(String cursor, Integer perPage, Map<String, String> sortBy, Boolean mine, String path) throws ApiException {
    return getRequestsWithHttpInfo(cursor, perPage, sortBy, mine, path).getData();
      }

  /**
   * List Requests
   * List Requests
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;destination&#x60;. (optional)
   * @param mine Only show requests of the current user?  (Defaults to true if current user is not a site admin.) (optional)
   * @param path Path to show requests for.  If omitted, shows all paths. Send &#x60;/&#x60; to represent the root directory. (optional)
   * @return ApiResponse&lt;List&lt;RequestEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<RequestEntity>> getRequestsWithHttpInfo(String cursor, Integer perPage, Map<String, String> sortBy, Boolean mine, String path) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/requests";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "mine", mine));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "path", path));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<RequestEntity>> localVarReturnType = new GenericType<List<RequestEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List Requests
   * List Requests
   * @param path Path to show requests for.  If omitted, shows all paths. Send &#x60;/&#x60; to represent the root directory. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;destination&#x60;. (optional)
   * @param mine Only show requests of the current user?  (Defaults to true if current user is not a site admin.) (optional)
   * @return List&lt;RequestEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<RequestEntity> getRequestsFoldersPath(String path, String cursor, Integer perPage, Map<String, String> sortBy, Boolean mine) throws ApiException {
    return getRequestsFoldersPathWithHttpInfo(path, cursor, perPage, sortBy, mine).getData();
      }

  /**
   * List Requests
   * List Requests
   * @param path Path to show requests for.  If omitted, shows all paths. Send &#x60;/&#x60; to represent the root directory. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;destination&#x60;. (optional)
   * @param mine Only show requests of the current user?  (Defaults to true if current user is not a site admin.) (optional)
   * @return ApiResponse&lt;List&lt;RequestEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<RequestEntity>> getRequestsFoldersPathWithHttpInfo(String path, String cursor, Integer perPage, Map<String, String> sortBy, Boolean mine) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling getRequestsFoldersPath");
    }
    
    // create path and map variables
    String localVarPath = "/requests/folders/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "mine", mine));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<RequestEntity>> localVarReturnType = new GenericType<List<RequestEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Request
   * Create Request
   * @param path Folder path on which to request the file. (required)
   * @param destination Destination filename (without extension) to request. (required)
   * @param userIds A list of user IDs to request the file from. If sent as a string, it should be comma-delimited. (optional)
   * @param groupIds A list of group IDs to request the file from. If sent as a string, it should be comma-delimited. (optional)
   * @return RequestEntity
   * @throws ApiException if fails to make API call
   */
  public RequestEntity postRequests(String path, String destination, String userIds, String groupIds) throws ApiException {
    return postRequestsWithHttpInfo(path, destination, userIds, groupIds).getData();
      }

  /**
   * Create Request
   * Create Request
   * @param path Folder path on which to request the file. (required)
   * @param destination Destination filename (without extension) to request. (required)
   * @param userIds A list of user IDs to request the file from. If sent as a string, it should be comma-delimited. (optional)
   * @param groupIds A list of group IDs to request the file from. If sent as a string, it should be comma-delimited. (optional)
   * @return ApiResponse&lt;RequestEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RequestEntity> postRequestsWithHttpInfo(String path, String destination, String userIds, String groupIds) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling postRequests");
    }
    
    // verify the required parameter 'destination' is set
    if (destination == null) {
      throw new ApiException(400, "Missing the required parameter 'destination' when calling postRequests");
    }
    
    // create path and map variables
    String localVarPath = "/requests";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (path != null)
      localVarFormParams.put("path", path);
if (destination != null)
      localVarFormParams.put("destination", destination);
if (userIds != null)
      localVarFormParams.put("user_ids", userIds);
if (groupIds != null)
      localVarFormParams.put("group_ids", groupIds);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<RequestEntity> localVarReturnType = new GenericType<RequestEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
