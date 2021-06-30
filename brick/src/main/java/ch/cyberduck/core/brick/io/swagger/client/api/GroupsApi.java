package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import org.joda.time.DateTime;
import java.io.File;
import ch.cyberduck.core.brick.io.swagger.client.model.GroupEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.GroupUserEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.PermissionEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.UserEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class GroupsApi {
  private ApiClient apiClient;

  public GroupsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public GroupsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Group User
   * Delete Group User
   * @param groupId Group ID from which to remove user. (required)
   * @param userId User ID to remove from group. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteGroupsGroupIdMembershipsUserId(Integer groupId, Integer userId) throws ApiException {

    deleteGroupsGroupIdMembershipsUserIdWithHttpInfo(groupId, userId);
  }

  /**
   * Delete Group User
   * Delete Group User
   * @param groupId Group ID from which to remove user. (required)
   * @param userId User ID to remove from group. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteGroupsGroupIdMembershipsUserIdWithHttpInfo(Integer groupId, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling deleteGroupsGroupIdMembershipsUserId");
    }
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling deleteGroupsGroupIdMembershipsUserId");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{group_id}/memberships/{user_id}"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()))
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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
   * Delete Group
   * Delete Group
   * @param id Group ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteGroupsId(Integer id) throws ApiException {

    deleteGroupsIdWithHttpInfo(id);
  }

  /**
   * Delete Group
   * Delete Group
   * @param id Group ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteGroupsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteGroupsId");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{id}"
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
   * List Groups
   * List Groups
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;name&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param ids Comma-separated list of group ids to include in results. (optional)
   * @return List&lt;GroupEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<GroupEntity> getGroups(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String ids) throws ApiException {
    return getGroupsWithHttpInfo(cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, ids).getData();
      }

  /**
   * List Groups
   * List Groups
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;name&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;name&#x60;. (optional)
   * @param ids Comma-separated list of group ids to include in results. (optional)
   * @return ApiResponse&lt;List&lt;GroupEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<GroupEntity>> getGroupsWithHttpInfo(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String ids) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/groups";

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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "ids", ids));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<GroupEntity>> localVarReturnType = new GenericType<List<GroupEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List Permissions
   * List Permissions
   * @param groupId DEPRECATED: Group ID.  If provided, will scope permissions to this group. Use &#x60;filter[group_id]&#x60; instead.&#x60; (required)
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
   * @param userId DEPRECATED: User ID.  If provided, will scope permissions to this user. Use &#x60;filter[user_id]&#x60; instead.&#x60; (optional)
   * @param includeGroups If searching by user or group, also include user&#39;s permissions that are inherited from its groups? (optional)
   * @return List&lt;PermissionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PermissionEntity> getGroupsGroupIdPermissions(String groupId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String path, String userId, Boolean includeGroups) throws ApiException {
    return getGroupsGroupIdPermissionsWithHttpInfo(groupId, cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, path, userId, includeGroups).getData();
      }

  /**
   * List Permissions
   * List Permissions
   * @param groupId DEPRECATED: Group ID.  If provided, will scope permissions to this group. Use &#x60;filter[group_id]&#x60; instead.&#x60; (required)
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
   * @param userId DEPRECATED: User ID.  If provided, will scope permissions to this user. Use &#x60;filter[user_id]&#x60; instead.&#x60; (optional)
   * @param includeGroups If searching by user or group, also include user&#39;s permissions that are inherited from its groups? (optional)
   * @return ApiResponse&lt;List&lt;PermissionEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<PermissionEntity>> getGroupsGroupIdPermissionsWithHttpInfo(String groupId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String path, String userId, Boolean includeGroups) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroupsGroupIdPermissions");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{group_id}/permissions"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

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
   * List Group Users
   * List Group Users
   * @param groupId Group ID.  If provided, will return group_users of this group. (required)
   * @param userId User ID.  If provided, will return group_users of this user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;GroupUserEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<GroupUserEntity> getGroupsGroupIdUsers(Integer groupId, Integer userId, String cursor, Integer perPage) throws ApiException {
    return getGroupsGroupIdUsersWithHttpInfo(groupId, userId, cursor, perPage).getData();
      }

  /**
   * List Group Users
   * List Group Users
   * @param groupId Group ID.  If provided, will return group_users of this group. (required)
   * @param userId User ID.  If provided, will return group_users of this user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;GroupUserEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<GroupUserEntity>> getGroupsGroupIdUsersWithHttpInfo(Integer groupId, Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroupsGroupIdUsers");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{group_id}/users"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<GroupUserEntity>> localVarReturnType = new GenericType<List<GroupUserEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Group
   * Show Group
   * @param id Group ID. (required)
   * @return GroupEntity
   * @throws ApiException if fails to make API call
   */
  public GroupEntity getGroupsId(Integer id) throws ApiException {
    return getGroupsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Group
   * Show Group
   * @param id Group ID. (required)
   * @return ApiResponse&lt;GroupEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GroupEntity> getGroupsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getGroupsId");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{id}"
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

    GenericType<GroupEntity> localVarReturnType = new GenericType<GroupEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Group User
   * Update Group User
   * @param groupId Group ID to add user to. (required)
   * @param userId User ID to add to group. (required)
   * @param admin Is the user a group administrator? (optional)
   * @return GroupUserEntity
   * @throws ApiException if fails to make API call
   */
  public GroupUserEntity patchGroupsGroupIdMembershipsUserId(Integer groupId, Integer userId, Boolean admin) throws ApiException {
    return patchGroupsGroupIdMembershipsUserIdWithHttpInfo(groupId, userId, admin).getData();
      }

  /**
   * Update Group User
   * Update Group User
   * @param groupId Group ID to add user to. (required)
   * @param userId User ID to add to group. (required)
   * @param admin Is the user a group administrator? (optional)
   * @return ApiResponse&lt;GroupUserEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GroupUserEntity> patchGroupsGroupIdMembershipsUserIdWithHttpInfo(Integer groupId, Integer userId, Boolean admin) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling patchGroupsGroupIdMembershipsUserId");
    }
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling patchGroupsGroupIdMembershipsUserId");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{group_id}/memberships/{user_id}"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()))
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (admin != null)
      localVarFormParams.put("admin", admin);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<GroupUserEntity> localVarReturnType = new GenericType<GroupUserEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Group
   * Update Group
   * @param id Group ID. (required)
   * @param name Group name. (optional)
   * @param notes Group notes. (optional)
   * @param userIds A list of user ids. If sent as a string, should be comma-delimited. (optional)
   * @param adminIds A list of group admin user ids. If sent as a string, should be comma-delimited. (optional)
   * @return GroupEntity
   * @throws ApiException if fails to make API call
   */
  public GroupEntity patchGroupsId(Integer id, String name, String notes, String userIds, String adminIds) throws ApiException {
    return patchGroupsIdWithHttpInfo(id, name, notes, userIds, adminIds).getData();
      }

  /**
   * Update Group
   * Update Group
   * @param id Group ID. (required)
   * @param name Group name. (optional)
   * @param notes Group notes. (optional)
   * @param userIds A list of user ids. If sent as a string, should be comma-delimited. (optional)
   * @param adminIds A list of group admin user ids. If sent as a string, should be comma-delimited. (optional)
   * @return ApiResponse&lt;GroupEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GroupEntity> patchGroupsIdWithHttpInfo(Integer id, String name, String notes, String userIds, String adminIds) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchGroupsId");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (name != null)
      localVarFormParams.put("name", name);
if (notes != null)
      localVarFormParams.put("notes", notes);
if (userIds != null)
      localVarFormParams.put("user_ids", userIds);
if (adminIds != null)
      localVarFormParams.put("admin_ids", adminIds);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<GroupEntity> localVarReturnType = new GenericType<GroupEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Group
   * Create Group
   * @param name Group name. (optional)
   * @param notes Group notes. (optional)
   * @param userIds A list of user ids. If sent as a string, should be comma-delimited. (optional)
   * @param adminIds A list of group admin user ids. If sent as a string, should be comma-delimited. (optional)
   * @return GroupEntity
   * @throws ApiException if fails to make API call
   */
  public GroupEntity postGroups(String name, String notes, String userIds, String adminIds) throws ApiException {
    return postGroupsWithHttpInfo(name, notes, userIds, adminIds).getData();
      }

  /**
   * Create Group
   * Create Group
   * @param name Group name. (optional)
   * @param notes Group notes. (optional)
   * @param userIds A list of user ids. If sent as a string, should be comma-delimited. (optional)
   * @param adminIds A list of group admin user ids. If sent as a string, should be comma-delimited. (optional)
   * @return ApiResponse&lt;GroupEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GroupEntity> postGroupsWithHttpInfo(String name, String notes, String userIds, String adminIds) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/groups";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (name != null)
      localVarFormParams.put("name", name);
if (notes != null)
      localVarFormParams.put("notes", notes);
if (userIds != null)
      localVarFormParams.put("user_ids", userIds);
if (adminIds != null)
      localVarFormParams.put("admin_ids", adminIds);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<GroupEntity> localVarReturnType = new GenericType<GroupEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create User
   * Create User
   * @param groupId Group ID to associate this user with. (required)
   * @param avatarFile An image file for your user avatar. (optional)
   * @param avatarDelete If true, the avatar will be deleted. (optional)
   * @param changePassword Used for changing a password on an existing user. (optional)
   * @param changePasswordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;change_password&#x60;. (optional)
   * @param email User&#39;s email. (optional)
   * @param grantPermission Permission to grant on the user root.  Can be blank or &#x60;full&#x60;, &#x60;read&#x60;, &#x60;write&#x60;, &#x60;list&#x60;, or &#x60;history&#x60;. (optional)
   * @param groupIds A list of group ids to associate this user with.  Comma delimited. (optional)
   * @param password User password. (optional)
   * @param passwordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;password&#x60;. (optional)
   * @param announcementsRead Signifies that the user has read all the announcements in the UI. (optional)
   * @param allowedIps A list of allowed IPs if applicable.  Newline delimited (optional)
   * @param attachmentsPermission DEPRECATED: Can the user create Bundles (aka Share Links)? Use the bundle permission instead. (optional)
   * @param authenticateUntil Scheduled Date/Time at which user will be deactivated (optional)
   * @param authenticationMethod How is this user authenticated? (optional)
   * @param billingPermission Allow this user to perform operations on the account, payments, and invoices? (optional)
   * @param bypassInactiveDisable Exempt this user from being disabled based on inactivity? (optional)
   * @param bypassSiteAllowedIps Allow this user to skip site-wide IP blacklists? (optional)
   * @param davPermission Can the user connect with WebDAV? (optional)
   * @param disabled Is user disabled? Disabled users cannot log in, and do not count for billing purposes.  Users can be automatically disabled after an inactivity period via a Site setting. (optional)
   * @param ftpPermission Can the user access with FTP/FTPS? (optional)
   * @param headerText Text to display to the user in the header of the UI (optional)
   * @param language Preferred language (optional)
   * @param notificationDailySendTime Hour of the day at which daily notifications should be sent. Can be in range 0 to 23 (optional)
   * @param name User&#39;s full name (optional)
   * @param company User&#39;s company (optional)
   * @param notes Any internal notes on the user (optional)
   * @param officeIntegrationEnabled Enable integration with Office for the web? (optional)
   * @param passwordValidityDays Number of days to allow user to use the same password (optional)
   * @param receiveAdminAlerts Should the user receive admin alerts such a certificate expiration notifications and overages? (optional)
   * @param requirePasswordChange Is a password change required upon next user login? (optional)
   * @param restapiPermission Can this user access the REST API? (optional)
   * @param selfManaged Does this user manage it&#39;s own credentials or is it a shared/bot user? (optional)
   * @param sftpPermission Can the user access with SFTP? (optional)
   * @param siteAdmin Is the user an administrator for this site? (optional)
   * @param skipWelcomeScreen Skip Welcome page in the UI? (optional)
   * @param sslRequired SSL required setting (optional)
   * @param ssoStrategyId SSO (Single Sign On) strategy ID for the user, if applicable. (optional)
   * @param subscribeToNewsletter Is the user subscribed to the newsletter? (optional)
   * @param require2fa 2FA required setting (optional)
   * @param timeZone User time zone (optional)
   * @param userRoot Root folder for FTP (and optionally SFTP if the appropriate site-wide setting is set.)  Note that this is not used for API, Desktop, or Web interface. (optional)
   * @param username User&#39;s username (optional)
   * @return UserEntity
   * @throws ApiException if fails to make API call
   */
  public UserEntity postGroupsGroupIdUsers(Integer groupId, File avatarFile, Boolean avatarDelete, String changePassword, String changePasswordConfirmation, String email, String grantPermission, String groupIds, String password, String passwordConfirmation, Boolean announcementsRead, String allowedIps, Boolean attachmentsPermission, DateTime authenticateUntil, String authenticationMethod, Boolean billingPermission, Boolean bypassInactiveDisable, Boolean bypassSiteAllowedIps, Boolean davPermission, Boolean disabled, Boolean ftpPermission, String headerText, String language, Integer notificationDailySendTime, String name, String company, String notes, Boolean officeIntegrationEnabled, Integer passwordValidityDays, Boolean receiveAdminAlerts, Boolean requirePasswordChange, Boolean restapiPermission, Boolean selfManaged, Boolean sftpPermission, Boolean siteAdmin, Boolean skipWelcomeScreen, String sslRequired, Integer ssoStrategyId, Boolean subscribeToNewsletter, String require2fa, String timeZone, String userRoot, String username) throws ApiException {
    return postGroupsGroupIdUsersWithHttpInfo(groupId, avatarFile, avatarDelete, changePassword, changePasswordConfirmation, email, grantPermission, groupIds, password, passwordConfirmation, announcementsRead, allowedIps, attachmentsPermission, authenticateUntil, authenticationMethod, billingPermission, bypassInactiveDisable, bypassSiteAllowedIps, davPermission, disabled, ftpPermission, headerText, language, notificationDailySendTime, name, company, notes, officeIntegrationEnabled, passwordValidityDays, receiveAdminAlerts, requirePasswordChange, restapiPermission, selfManaged, sftpPermission, siteAdmin, skipWelcomeScreen, sslRequired, ssoStrategyId, subscribeToNewsletter, require2fa, timeZone, userRoot, username).getData();
      }

  /**
   * Create User
   * Create User
   * @param groupId Group ID to associate this user with. (required)
   * @param avatarFile An image file for your user avatar. (optional)
   * @param avatarDelete If true, the avatar will be deleted. (optional)
   * @param changePassword Used for changing a password on an existing user. (optional)
   * @param changePasswordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;change_password&#x60;. (optional)
   * @param email User&#39;s email. (optional)
   * @param grantPermission Permission to grant on the user root.  Can be blank or &#x60;full&#x60;, &#x60;read&#x60;, &#x60;write&#x60;, &#x60;list&#x60;, or &#x60;history&#x60;. (optional)
   * @param groupIds A list of group ids to associate this user with.  Comma delimited. (optional)
   * @param password User password. (optional)
   * @param passwordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;password&#x60;. (optional)
   * @param announcementsRead Signifies that the user has read all the announcements in the UI. (optional)
   * @param allowedIps A list of allowed IPs if applicable.  Newline delimited (optional)
   * @param attachmentsPermission DEPRECATED: Can the user create Bundles (aka Share Links)? Use the bundle permission instead. (optional)
   * @param authenticateUntil Scheduled Date/Time at which user will be deactivated (optional)
   * @param authenticationMethod How is this user authenticated? (optional)
   * @param billingPermission Allow this user to perform operations on the account, payments, and invoices? (optional)
   * @param bypassInactiveDisable Exempt this user from being disabled based on inactivity? (optional)
   * @param bypassSiteAllowedIps Allow this user to skip site-wide IP blacklists? (optional)
   * @param davPermission Can the user connect with WebDAV? (optional)
   * @param disabled Is user disabled? Disabled users cannot log in, and do not count for billing purposes.  Users can be automatically disabled after an inactivity period via a Site setting. (optional)
   * @param ftpPermission Can the user access with FTP/FTPS? (optional)
   * @param headerText Text to display to the user in the header of the UI (optional)
   * @param language Preferred language (optional)
   * @param notificationDailySendTime Hour of the day at which daily notifications should be sent. Can be in range 0 to 23 (optional)
   * @param name User&#39;s full name (optional)
   * @param company User&#39;s company (optional)
   * @param notes Any internal notes on the user (optional)
   * @param officeIntegrationEnabled Enable integration with Office for the web? (optional)
   * @param passwordValidityDays Number of days to allow user to use the same password (optional)
   * @param receiveAdminAlerts Should the user receive admin alerts such a certificate expiration notifications and overages? (optional)
   * @param requirePasswordChange Is a password change required upon next user login? (optional)
   * @param restapiPermission Can this user access the REST API? (optional)
   * @param selfManaged Does this user manage it&#39;s own credentials or is it a shared/bot user? (optional)
   * @param sftpPermission Can the user access with SFTP? (optional)
   * @param siteAdmin Is the user an administrator for this site? (optional)
   * @param skipWelcomeScreen Skip Welcome page in the UI? (optional)
   * @param sslRequired SSL required setting (optional)
   * @param ssoStrategyId SSO (Single Sign On) strategy ID for the user, if applicable. (optional)
   * @param subscribeToNewsletter Is the user subscribed to the newsletter? (optional)
   * @param require2fa 2FA required setting (optional)
   * @param timeZone User time zone (optional)
   * @param userRoot Root folder for FTP (and optionally SFTP if the appropriate site-wide setting is set.)  Note that this is not used for API, Desktop, or Web interface. (optional)
   * @param username User&#39;s username (optional)
   * @return ApiResponse&lt;UserEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserEntity> postGroupsGroupIdUsersWithHttpInfo(Integer groupId, File avatarFile, Boolean avatarDelete, String changePassword, String changePasswordConfirmation, String email, String grantPermission, String groupIds, String password, String passwordConfirmation, Boolean announcementsRead, String allowedIps, Boolean attachmentsPermission, DateTime authenticateUntil, String authenticationMethod, Boolean billingPermission, Boolean bypassInactiveDisable, Boolean bypassSiteAllowedIps, Boolean davPermission, Boolean disabled, Boolean ftpPermission, String headerText, String language, Integer notificationDailySendTime, String name, String company, String notes, Boolean officeIntegrationEnabled, Integer passwordValidityDays, Boolean receiveAdminAlerts, Boolean requirePasswordChange, Boolean restapiPermission, Boolean selfManaged, Boolean sftpPermission, Boolean siteAdmin, Boolean skipWelcomeScreen, String sslRequired, Integer ssoStrategyId, Boolean subscribeToNewsletter, String require2fa, String timeZone, String userRoot, String username) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling postGroupsGroupIdUsers");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{group_id}/users"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (avatarFile != null)
      localVarFormParams.put("avatar_file", avatarFile);
if (avatarDelete != null)
      localVarFormParams.put("avatar_delete", avatarDelete);
if (changePassword != null)
      localVarFormParams.put("change_password", changePassword);
if (changePasswordConfirmation != null)
      localVarFormParams.put("change_password_confirmation", changePasswordConfirmation);
if (email != null)
      localVarFormParams.put("email", email);
if (grantPermission != null)
      localVarFormParams.put("grant_permission", grantPermission);
if (groupIds != null)
      localVarFormParams.put("group_ids", groupIds);
if (password != null)
      localVarFormParams.put("password", password);
if (passwordConfirmation != null)
      localVarFormParams.put("password_confirmation", passwordConfirmation);
if (announcementsRead != null)
      localVarFormParams.put("announcements_read", announcementsRead);
if (allowedIps != null)
      localVarFormParams.put("allowed_ips", allowedIps);
if (attachmentsPermission != null)
      localVarFormParams.put("attachments_permission", attachmentsPermission);
if (authenticateUntil != null)
      localVarFormParams.put("authenticate_until", authenticateUntil);
if (authenticationMethod != null)
      localVarFormParams.put("authentication_method", authenticationMethod);
if (billingPermission != null)
      localVarFormParams.put("billing_permission", billingPermission);
if (bypassInactiveDisable != null)
      localVarFormParams.put("bypass_inactive_disable", bypassInactiveDisable);
if (bypassSiteAllowedIps != null)
      localVarFormParams.put("bypass_site_allowed_ips", bypassSiteAllowedIps);
if (davPermission != null)
      localVarFormParams.put("dav_permission", davPermission);
if (disabled != null)
      localVarFormParams.put("disabled", disabled);
if (ftpPermission != null)
      localVarFormParams.put("ftp_permission", ftpPermission);
if (headerText != null)
      localVarFormParams.put("header_text", headerText);
if (language != null)
      localVarFormParams.put("language", language);
if (notificationDailySendTime != null)
      localVarFormParams.put("notification_daily_send_time", notificationDailySendTime);
if (name != null)
      localVarFormParams.put("name", name);
if (company != null)
      localVarFormParams.put("company", company);
if (notes != null)
      localVarFormParams.put("notes", notes);
if (officeIntegrationEnabled != null)
      localVarFormParams.put("office_integration_enabled", officeIntegrationEnabled);
if (passwordValidityDays != null)
      localVarFormParams.put("password_validity_days", passwordValidityDays);
if (receiveAdminAlerts != null)
      localVarFormParams.put("receive_admin_alerts", receiveAdminAlerts);
if (requirePasswordChange != null)
      localVarFormParams.put("require_password_change", requirePasswordChange);
if (restapiPermission != null)
      localVarFormParams.put("restapi_permission", restapiPermission);
if (selfManaged != null)
      localVarFormParams.put("self_managed", selfManaged);
if (sftpPermission != null)
      localVarFormParams.put("sftp_permission", sftpPermission);
if (siteAdmin != null)
      localVarFormParams.put("site_admin", siteAdmin);
if (skipWelcomeScreen != null)
      localVarFormParams.put("skip_welcome_screen", skipWelcomeScreen);
if (sslRequired != null)
      localVarFormParams.put("ssl_required", sslRequired);
if (ssoStrategyId != null)
      localVarFormParams.put("sso_strategy_id", ssoStrategyId);
if (subscribeToNewsletter != null)
      localVarFormParams.put("subscribe_to_newsletter", subscribeToNewsletter);
if (require2fa != null)
      localVarFormParams.put("require_2fa", require2fa);
if (timeZone != null)
      localVarFormParams.put("time_zone", timeZone);
if (userRoot != null)
      localVarFormParams.put("user_root", userRoot);
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

    GenericType<UserEntity> localVarReturnType = new GenericType<UserEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
