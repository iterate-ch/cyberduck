package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.PermissionEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class PermissionsApi {
  private ApiClient apiClient;

  public PermissionsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PermissionsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Permission
   * Delete Permission
   * @param id Permission ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deletePermissionsId(Integer id) throws ApiException {

    deletePermissionsIdWithHttpInfo(id);
  }

  /**
   * Delete Permission
   * Delete Permission
   * @param id Permission ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deletePermissionsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deletePermissionsId");
    }
    
    // create path and map variables
    String localVarPath = "/permissions/{id}"
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
   * List Permissions
   * List Permissions
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;group_id&#x60;, &#x60;path&#x60;, &#x60;user_id&#x60; or &#x60;permission&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param path DEPRECATED: Permission path.  If provided, will scope permissions to this path. Use &#x60;filter[path]&#x60; instead. (optional)
   * @param groupId DEPRECATED: Group ID.  If provided, will scope permissions to this group. Use &#x60;filter[group_id]&#x60; instead.&#x60; (optional)
   * @param userId DEPRECATED: User ID.  If provided, will scope permissions to this user. Use &#x60;filter[user_id]&#x60; instead.&#x60; (optional)
   * @param includeGroups If searching by user or group, also include user&#39;s permissions that are inherited from its groups? (optional)
   * @return List&lt;PermissionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PermissionEntity> getPermissions(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String path, String groupId, String userId, Boolean includeGroups) throws ApiException {
    return getPermissionsWithHttpInfo(cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, path, groupId, userId, includeGroups).getData();
      }

  /**
   * List Permissions
   * List Permissions
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;group_id&#x60;, &#x60;path&#x60;, &#x60;user_id&#x60; or &#x60;permission&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param path DEPRECATED: Permission path.  If provided, will scope permissions to this path. Use &#x60;filter[path]&#x60; instead. (optional)
   * @param groupId DEPRECATED: Group ID.  If provided, will scope permissions to this group. Use &#x60;filter[group_id]&#x60; instead.&#x60; (optional)
   * @param userId DEPRECATED: User ID.  If provided, will scope permissions to this user. Use &#x60;filter[user_id]&#x60; instead.&#x60; (optional)
   * @param includeGroups If searching by user or group, also include user&#39;s permissions that are inherited from its groups? (optional)
   * @return ApiResponse&lt;List&lt;PermissionEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<PermissionEntity>> getPermissionsWithHttpInfo(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String path, String groupId, String userId, Boolean includeGroups) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/permissions";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "path", path));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "group_id", groupId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_groups", includeGroups));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<PermissionEntity>> localVarReturnType = new GenericType<List<PermissionEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Permission
   * Create Permission
   * @param groupId Group ID (optional)
   * @param path Folder path (optional)
   * @param permission  Permission type.  Can be &#x60;admin&#x60;, &#x60;full&#x60;, &#x60;readonly&#x60;, &#x60;writeonly&#x60;, &#x60;list&#x60;, or &#x60;history&#x60; (optional)
   * @param recursive Apply to subfolders recursively? (optional)
   * @param userId User ID.  Provide &#x60;username&#x60; or &#x60;user_id&#x60; (optional)
   * @param username User username.  Provide &#x60;username&#x60; or &#x60;user_id&#x60; (optional)
   * @return PermissionEntity
   * @throws ApiException if fails to make API call
   */
  public PermissionEntity postPermissions(Integer groupId, String path, String permission, Boolean recursive, Integer userId, String username) throws ApiException {
    return postPermissionsWithHttpInfo(groupId, path, permission, recursive, userId, username).getData();
      }

  /**
   * Create Permission
   * Create Permission
   * @param groupId Group ID (optional)
   * @param path Folder path (optional)
   * @param permission  Permission type.  Can be &#x60;admin&#x60;, &#x60;full&#x60;, &#x60;readonly&#x60;, &#x60;writeonly&#x60;, &#x60;list&#x60;, or &#x60;history&#x60; (optional)
   * @param recursive Apply to subfolders recursively? (optional)
   * @param userId User ID.  Provide &#x60;username&#x60; or &#x60;user_id&#x60; (optional)
   * @param username User username.  Provide &#x60;username&#x60; or &#x60;user_id&#x60; (optional)
   * @return ApiResponse&lt;PermissionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PermissionEntity> postPermissionsWithHttpInfo(Integer groupId, String path, String permission, Boolean recursive, Integer userId, String username) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/permissions";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (groupId != null)
      localVarFormParams.put("group_id", groupId);
if (path != null)
      localVarFormParams.put("path", path);
if (permission != null)
      localVarFormParams.put("permission", permission);
if (recursive != null)
      localVarFormParams.put("recursive", recursive);
if (userId != null)
      localVarFormParams.put("user_id", userId);
if (username != null)
      localVarFormParams.put("username", username);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PermissionEntity> localVarReturnType = new GenericType<PermissionEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
