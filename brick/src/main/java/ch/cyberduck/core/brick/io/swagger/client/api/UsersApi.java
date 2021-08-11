package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ApiKeyEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.As2KeyEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.GroupUserEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.PermissionEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.PublicKeyEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.UserCipherUseEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.UserEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.UserIdApiKeysBody;
import ch.cyberduck.core.brick.io.swagger.client.model.UserIdAs2KeysBody;
import ch.cyberduck.core.brick.io.swagger.client.model.UserIdPublicKeysBody;
import ch.cyberduck.core.brick.io.swagger.client.model.UsersBody;
import ch.cyberduck.core.brick.io.swagger.client.model.UsersIdBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")public class UsersApi {
  private ApiClient apiClient;

  public UsersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UsersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete User
   * Delete User
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteUsersId(Integer id) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteUsersId");
    }
    // create path and map variables
    String localVarPath = "/users/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List Users
   * List Users
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;authenticate_until&#x60;, &#x60;email&#x60;, &#x60;last_desktop_login_at&#x60;, &#x60;last_login_at&#x60;, &#x60;username&#x60;, &#x60;company&#x60;, &#x60;name&#x60;, &#x60;site_admin&#x60;, &#x60;receive_admin_alerts&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60; or &#x60;not_site_admin&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;username&#x60;, &#x60;email&#x60;, &#x60;company&#x60;, &#x60;site_admin&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60;, &#x60;last_login_at&#x60;, &#x60;authenticate_until&#x60; or &#x60;not_site_admin&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;username&#x60;, &#x60;email&#x60;, &#x60;company&#x60;, &#x60;site_admin&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60;, &#x60;last_login_at&#x60;, &#x60;authenticate_until&#x60; or &#x60;not_site_admin&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;username&#x60;, &#x60;email&#x60;, &#x60;company&#x60;, &#x60;site_admin&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60;, &#x60;last_login_at&#x60;, &#x60;authenticate_until&#x60; or &#x60;not_site_admin&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;username&#x60;, &#x60;email&#x60;, &#x60;company&#x60;, &#x60;site_admin&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60;, &#x60;last_login_at&#x60;, &#x60;authenticate_until&#x60; or &#x60;not_site_admin&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;username&#x60;, &#x60;email&#x60;, &#x60;company&#x60;, &#x60;site_admin&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60;, &#x60;last_login_at&#x60;, &#x60;authenticate_until&#x60; or &#x60;not_site_admin&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;username&#x60;, &#x60;email&#x60;, &#x60;company&#x60;, &#x60;site_admin&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60;, &#x60;last_login_at&#x60;, &#x60;authenticate_until&#x60; or &#x60;not_site_admin&#x60;. (optional)
   * @param ids comma-separated list of User IDs (optional)
   * @param qUsername List users matching username. (optional)
   * @param qEmail List users matching email. (optional)
   * @param qNotes List users matching notes field. (optional)
   * @param qAdmin If &#x60;true&#x60;, list only admin users. (optional)
   * @param qAllowedIps If set, list only users with overridden allowed IP setting. (optional)
   * @param qPasswordValidityDays If set, list only users with overridden password validity days setting. (optional)
   * @param qSslRequired If set, list only users with overridden SSL required setting. (optional)
   * @param search Searches for partial matches of name, username, or email. (optional)
   * @return List&lt;UserEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<UserEntity> getUsers(String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq, String ids, String qUsername, String qEmail, String qNotes, String qAdmin, String qAllowedIps, String qPasswordValidityDays, String qSslRequired, String search) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/users";

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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q[username]", qUsername));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q[email]", qEmail));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q[notes]", qNotes));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q[admin]", qAdmin));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q[allowed_ips]", qAllowedIps));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q[password_validity_days]", qPasswordValidityDays));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q[ssl_required]", qSslRequired));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "search", search));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<UserEntity>> localVarReturnType = new GenericType<List<UserEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Show User
   * Show User
   * @param id User ID. (required)
   * @return UserEntity
   * @throws ApiException if fails to make API call
   */
  public UserEntity getUsersId(Integer id) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getUsersId");
    }
    // create path and map variables
    String localVarPath = "/users/{id}"
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

    GenericType<UserEntity> localVarReturnType = new GenericType<UserEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List Api Keys
   * List Api Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @return List&lt;ApiKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ApiKeyEntity> getUsersUserIdApiKeys(Integer userId, String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersUserIdApiKeys");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/api_keys"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<ApiKeyEntity>> localVarReturnType = new GenericType<List<ApiKeyEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List As2 Keys
   * List As2 Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;As2KeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<As2KeyEntity> getUsersUserIdAs2Keys(Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersUserIdAs2Keys");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/as2_keys"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

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

    GenericType<List<As2KeyEntity>> localVarReturnType = new GenericType<List<As2KeyEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List User Cipher Uses
   * List User Cipher Uses
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;UserCipherUseEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<UserCipherUseEntity> getUsersUserIdCipherUses(Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersUserIdCipherUses");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/cipher_uses"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

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

    GenericType<List<UserCipherUseEntity>> localVarReturnType = new GenericType<List<UserCipherUseEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List Group Users
   * List Group Users
   * @param userId User ID.  If provided, will return group_users of this user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param groupId Group ID.  If provided, will return group_users of this group. (optional)
   * @return List&lt;GroupUserEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<GroupUserEntity> getUsersUserIdGroups(Integer userId, String cursor, Integer perPage, Integer groupId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersUserIdGroups");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/groups"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "group_id", groupId));


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
   * List Permissions
   * List Permissions
   * @param userId DEPRECATED: User ID.  If provided, will scope permissions to this user. Use &#x60;filter[user_id]&#x60; instead.&#x60; (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;group_id&#x60;, &#x60;path&#x60;, &#x60;user_id&#x60; or &#x60;permission&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param path DEPRECATED: Permission path.  If provided, will scope permissions to this path. Use &#x60;filter[path]&#x60; instead. (optional)
   * @param groupId DEPRECATED: Group ID.  If provided, will scope permissions to this group. Use &#x60;filter[group_id]&#x60; instead.&#x60; (optional)
   * @param includeGroups If searching by user or group, also include user&#x27;s permissions that are inherited from its groups? (optional)
   * @return List&lt;PermissionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PermissionEntity> getUsersUserIdPermissions(String userId, String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq, String path, String groupId, Boolean includeGroups) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersUserIdPermissions");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/permissions"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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
   * List Public Keys
   * List Public Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;PublicKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PublicKeyEntity> getUsersUserIdPublicKeys(Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersUserIdPublicKeys");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/public_keys"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

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

    GenericType<List<PublicKeyEntity>> localVarReturnType = new GenericType<List<PublicKeyEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update User
   * Update User
   * @param id User ID. (required)
   * @param body  (optional)
   * @return UserEntity
   * @throws ApiException if fails to make API call
   */
  public UserEntity patchUsersId(Integer id, UsersIdBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchUsersId");
    }
    // create path and map variables
    String localVarPath = "/users/{id}"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UserEntity> localVarReturnType = new GenericType<UserEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create User
   * Create User
   * @param body  (optional)
   * @return UserEntity
   * @throws ApiException if fails to make API call
   */
  public UserEntity postUsers(UsersBody body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/users";

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

    GenericType<UserEntity> localVarReturnType = new GenericType<UserEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Trigger 2FA Reset process for user who has lost access to their existing 2FA methods.
   * Trigger 2FA Reset process for user who has lost access to their existing 2FA methods.
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void postUsersId2faReset(Integer id) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling postUsersId2faReset");
    }
    // create path and map variables
    String localVarPath = "/users/{id}/2fa/reset"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Resend user welcome email
   * Resend user welcome email
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void postUsersIdResendWelcomeEmail(Integer id) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling postUsersIdResendWelcomeEmail");
    }
    // create path and map variables
    String localVarPath = "/users/{id}/resend_welcome_email"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Unlock user who has been locked out due to failed logins.
   * Unlock user who has been locked out due to failed logins.
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void postUsersIdUnlock(Integer id) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling postUsersIdUnlock");
    }
    // create path and map variables
    String localVarPath = "/users/{id}/unlock"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Create Api Key
   * Create Api Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (required)
   * @param body  (optional)
   * @return ApiKeyEntity
   * @throws ApiException if fails to make API call
   */
  public ApiKeyEntity postUsersUserIdApiKeys(Integer userId, UserIdApiKeysBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling postUsersUserIdApiKeys");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/api_keys"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ApiKeyEntity> localVarReturnType = new GenericType<ApiKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create As2 Key
   * Create As2 Key
   * @param body  (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (required)
   * @return As2KeyEntity
   * @throws ApiException if fails to make API call
   */
  public As2KeyEntity postUsersUserIdAs2Keys(UserIdAs2KeysBody body, Integer userId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postUsersUserIdAs2Keys");
    }
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling postUsersUserIdAs2Keys");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/as2_keys"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<As2KeyEntity> localVarReturnType = new GenericType<As2KeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create Public Key
   * Create Public Key
   * @param body  (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (required)
   * @return PublicKeyEntity
   * @throws ApiException if fails to make API call
   */
  public PublicKeyEntity postUsersUserIdPublicKeys(UserIdPublicKeysBody body, Integer userId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postUsersUserIdPublicKeys");
    }
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling postUsersUserIdPublicKeys");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/public_keys"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicKeyEntity> localVarReturnType = new GenericType<PublicKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
