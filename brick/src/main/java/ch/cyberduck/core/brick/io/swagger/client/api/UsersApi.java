package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ApiKeyEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.As2KeyEntity;
import org.joda.time.DateTime;
import java.io.File;
import ch.cyberduck.core.brick.io.swagger.client.model.GroupUserEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.PermissionEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.PublicKeyEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.UserCipherUseEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.UserEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class UsersApi {
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

    deleteUsersIdWithHttpInfo(id);
  }

  /**
   * Delete User
   * Delete User
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteUsersIdWithHttpInfo(Integer id) throws ApiException {
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
   * List Users
   * List Users
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;authenticate_until&#x60;, &#x60;email&#x60;, &#x60;last_desktop_login_at&#x60;, &#x60;last_login_at&#x60;, &#x60;username&#x60;, &#x60;company&#x60;, &#x60;name&#x60;, &#x60;site_admin&#x60;, &#x60;receive_admin_alerts&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60; or &#x60;not_site_admin&#x60;. (optional)
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
  public List<UserEntity> getUsers(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String ids, String qUsername, String qEmail, String qNotes, String qAdmin, String qAllowedIps, String qPasswordValidityDays, String qSslRequired, String search) throws ApiException {
    return getUsersWithHttpInfo(cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, ids, qUsername, qEmail, qNotes, qAdmin, qAllowedIps, qPasswordValidityDays, qSslRequired, search).getData();
      }

  /**
   * List Users
   * List Users
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;authenticate_until&#x60;, &#x60;email&#x60;, &#x60;last_desktop_login_at&#x60;, &#x60;last_login_at&#x60;, &#x60;username&#x60;, &#x60;company&#x60;, &#x60;name&#x60;, &#x60;site_admin&#x60;, &#x60;receive_admin_alerts&#x60;, &#x60;password_validity_days&#x60;, &#x60;ssl_required&#x60; or &#x60;not_site_admin&#x60;. (optional)
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
   * @return ApiResponse&lt;List&lt;UserEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<UserEntity>> getUsersWithHttpInfo(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String ids, String qUsername, String qEmail, String qNotes, String qAdmin, String qAllowedIps, String qPasswordValidityDays, String qSslRequired, String search) throws ApiException {
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
    return getUsersIdWithHttpInfo(id).getData();
      }

  /**
   * Show User
   * Show User
   * @param id User ID. (required)
   * @return ApiResponse&lt;UserEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserEntity> getUsersIdWithHttpInfo(Integer id) throws ApiException {
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
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @return List&lt;ApiKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ApiKeyEntity> getUsersUserIdApiKeys(Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    return getUsersUserIdApiKeysWithHttpInfo(userId, cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq).getData();
      }

  /**
   * List Api Keys
   * List Api Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @return ApiResponse&lt;List&lt;ApiKeyEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<ApiKeyEntity>> getUsersUserIdApiKeysWithHttpInfo(Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
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
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;As2KeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<As2KeyEntity> getUsersUserIdAs2Keys(Integer userId, String cursor, Integer perPage) throws ApiException {
    return getUsersUserIdAs2KeysWithHttpInfo(userId, cursor, perPage).getData();
      }

  /**
   * List As2 Keys
   * List As2 Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;As2KeyEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<As2KeyEntity>> getUsersUserIdAs2KeysWithHttpInfo(Integer userId, String cursor, Integer perPage) throws ApiException {
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
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;UserCipherUseEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<UserCipherUseEntity> getUsersUserIdCipherUses(Integer userId, String cursor, Integer perPage) throws ApiException {
    return getUsersUserIdCipherUsesWithHttpInfo(userId, cursor, perPage).getData();
      }

  /**
   * List User Cipher Uses
   * List User Cipher Uses
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;UserCipherUseEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<UserCipherUseEntity>> getUsersUserIdCipherUsesWithHttpInfo(Integer userId, String cursor, Integer perPage) throws ApiException {
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
    return getUsersUserIdGroupsWithHttpInfo(userId, cursor, perPage, groupId).getData();
      }

  /**
   * List Group Users
   * List Group Users
   * @param userId User ID.  If provided, will return group_users of this user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param groupId Group ID.  If provided, will return group_users of this group. (optional)
   * @return ApiResponse&lt;List&lt;GroupUserEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<GroupUserEntity>> getUsersUserIdGroupsWithHttpInfo(Integer userId, String cursor, Integer perPage, Integer groupId) throws ApiException {
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
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;group_id&#x60;, &#x60;path&#x60;, &#x60;user_id&#x60; or &#x60;permission&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;group_id&#x60;, &#x60;user_id&#x60; or &#x60;path&#x60;. (optional)
   * @param path DEPRECATED: Permission path.  If provided, will scope permissions to this path. Use &#x60;filter[path]&#x60; instead. (optional)
   * @param groupId DEPRECATED: Group ID.  If provided, will scope permissions to this group. Use &#x60;filter[group_id]&#x60; instead.&#x60; (optional)
   * @param includeGroups If searching by user or group, also include user&#39;s permissions that are inherited from its groups? (optional)
   * @return List&lt;PermissionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PermissionEntity> getUsersUserIdPermissions(String userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String path, String groupId, Boolean includeGroups) throws ApiException {
    return getUsersUserIdPermissionsWithHttpInfo(userId, cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, path, groupId, includeGroups).getData();
      }

  /**
   * List Permissions
   * List Permissions
   * @param userId DEPRECATED: User ID.  If provided, will scope permissions to this user. Use &#x60;filter[user_id]&#x60; instead.&#x60; (required)
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
   * @param includeGroups If searching by user or group, also include user&#39;s permissions that are inherited from its groups? (optional)
   * @return ApiResponse&lt;List&lt;PermissionEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<PermissionEntity>> getUsersUserIdPermissionsWithHttpInfo(String userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String path, String groupId, Boolean includeGroups) throws ApiException {
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
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;PublicKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PublicKeyEntity> getUsersUserIdPublicKeys(Integer userId, String cursor, Integer perPage) throws ApiException {
    return getUsersUserIdPublicKeysWithHttpInfo(userId, cursor, perPage).getData();
      }

  /**
   * List Public Keys
   * List Public Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;PublicKeyEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<PublicKeyEntity>> getUsersUserIdPublicKeysWithHttpInfo(Integer userId, String cursor, Integer perPage) throws ApiException {
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
   * @param avatarFile An image file for your user avatar. (optional)
   * @param avatarDelete If true, the avatar will be deleted. (optional)
   * @param changePassword Used for changing a password on an existing user. (optional)
   * @param changePasswordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;change_password&#x60;. (optional)
   * @param email User&#39;s email. (optional)
   * @param grantPermission Permission to grant on the user root.  Can be blank or &#x60;full&#x60;, &#x60;read&#x60;, &#x60;write&#x60;, &#x60;list&#x60;, or &#x60;history&#x60;. (optional)
   * @param groupId Group ID to associate this user with. (optional)
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
  public UserEntity patchUsersId(Integer id, File avatarFile, Boolean avatarDelete, String changePassword, String changePasswordConfirmation, String email, String grantPermission, Integer groupId, String groupIds, String password, String passwordConfirmation, Boolean announcementsRead, String allowedIps, Boolean attachmentsPermission, DateTime authenticateUntil, String authenticationMethod, Boolean billingPermission, Boolean bypassInactiveDisable, Boolean bypassSiteAllowedIps, Boolean davPermission, Boolean disabled, Boolean ftpPermission, String headerText, String language, Integer notificationDailySendTime, String name, String company, String notes, Boolean officeIntegrationEnabled, Integer passwordValidityDays, Boolean receiveAdminAlerts, Boolean requirePasswordChange, Boolean restapiPermission, Boolean selfManaged, Boolean sftpPermission, Boolean siteAdmin, Boolean skipWelcomeScreen, String sslRequired, Integer ssoStrategyId, Boolean subscribeToNewsletter, String require2fa, String timeZone, String userRoot, String username) throws ApiException {
    return patchUsersIdWithHttpInfo(id, avatarFile, avatarDelete, changePassword, changePasswordConfirmation, email, grantPermission, groupId, groupIds, password, passwordConfirmation, announcementsRead, allowedIps, attachmentsPermission, authenticateUntil, authenticationMethod, billingPermission, bypassInactiveDisable, bypassSiteAllowedIps, davPermission, disabled, ftpPermission, headerText, language, notificationDailySendTime, name, company, notes, officeIntegrationEnabled, passwordValidityDays, receiveAdminAlerts, requirePasswordChange, restapiPermission, selfManaged, sftpPermission, siteAdmin, skipWelcomeScreen, sslRequired, ssoStrategyId, subscribeToNewsletter, require2fa, timeZone, userRoot, username).getData();
      }

  /**
   * Update User
   * Update User
   * @param id User ID. (required)
   * @param avatarFile An image file for your user avatar. (optional)
   * @param avatarDelete If true, the avatar will be deleted. (optional)
   * @param changePassword Used for changing a password on an existing user. (optional)
   * @param changePasswordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;change_password&#x60;. (optional)
   * @param email User&#39;s email. (optional)
   * @param grantPermission Permission to grant on the user root.  Can be blank or &#x60;full&#x60;, &#x60;read&#x60;, &#x60;write&#x60;, &#x60;list&#x60;, or &#x60;history&#x60;. (optional)
   * @param groupId Group ID to associate this user with. (optional)
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
  public ApiResponse<UserEntity> patchUsersIdWithHttpInfo(Integer id, File avatarFile, Boolean avatarDelete, String changePassword, String changePasswordConfirmation, String email, String grantPermission, Integer groupId, String groupIds, String password, String passwordConfirmation, Boolean announcementsRead, String allowedIps, Boolean attachmentsPermission, DateTime authenticateUntil, String authenticationMethod, Boolean billingPermission, Boolean bypassInactiveDisable, Boolean bypassSiteAllowedIps, Boolean davPermission, Boolean disabled, Boolean ftpPermission, String headerText, String language, Integer notificationDailySendTime, String name, String company, String notes, Boolean officeIntegrationEnabled, Integer passwordValidityDays, Boolean receiveAdminAlerts, Boolean requirePasswordChange, Boolean restapiPermission, Boolean selfManaged, Boolean sftpPermission, Boolean siteAdmin, Boolean skipWelcomeScreen, String sslRequired, Integer ssoStrategyId, Boolean subscribeToNewsletter, String require2fa, String timeZone, String userRoot, String username) throws ApiException {
    Object localVarPostBody = null;
    
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
if (groupId != null)
      localVarFormParams.put("group_id", groupId);
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
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create User
   * Create User
   * @param avatarFile An image file for your user avatar. (optional)
   * @param avatarDelete If true, the avatar will be deleted. (optional)
   * @param changePassword Used for changing a password on an existing user. (optional)
   * @param changePasswordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;change_password&#x60;. (optional)
   * @param email User&#39;s email. (optional)
   * @param grantPermission Permission to grant on the user root.  Can be blank or &#x60;full&#x60;, &#x60;read&#x60;, &#x60;write&#x60;, &#x60;list&#x60;, or &#x60;history&#x60;. (optional)
   * @param groupId Group ID to associate this user with. (optional)
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
  public UserEntity postUsers(File avatarFile, Boolean avatarDelete, String changePassword, String changePasswordConfirmation, String email, String grantPermission, Integer groupId, String groupIds, String password, String passwordConfirmation, Boolean announcementsRead, String allowedIps, Boolean attachmentsPermission, DateTime authenticateUntil, String authenticationMethod, Boolean billingPermission, Boolean bypassInactiveDisable, Boolean bypassSiteAllowedIps, Boolean davPermission, Boolean disabled, Boolean ftpPermission, String headerText, String language, Integer notificationDailySendTime, String name, String company, String notes, Boolean officeIntegrationEnabled, Integer passwordValidityDays, Boolean receiveAdminAlerts, Boolean requirePasswordChange, Boolean restapiPermission, Boolean selfManaged, Boolean sftpPermission, Boolean siteAdmin, Boolean skipWelcomeScreen, String sslRequired, Integer ssoStrategyId, Boolean subscribeToNewsletter, String require2fa, String timeZone, String userRoot, String username) throws ApiException {
    return postUsersWithHttpInfo(avatarFile, avatarDelete, changePassword, changePasswordConfirmation, email, grantPermission, groupId, groupIds, password, passwordConfirmation, announcementsRead, allowedIps, attachmentsPermission, authenticateUntil, authenticationMethod, billingPermission, bypassInactiveDisable, bypassSiteAllowedIps, davPermission, disabled, ftpPermission, headerText, language, notificationDailySendTime, name, company, notes, officeIntegrationEnabled, passwordValidityDays, receiveAdminAlerts, requirePasswordChange, restapiPermission, selfManaged, sftpPermission, siteAdmin, skipWelcomeScreen, sslRequired, ssoStrategyId, subscribeToNewsletter, require2fa, timeZone, userRoot, username).getData();
      }

  /**
   * Create User
   * Create User
   * @param avatarFile An image file for your user avatar. (optional)
   * @param avatarDelete If true, the avatar will be deleted. (optional)
   * @param changePassword Used for changing a password on an existing user. (optional)
   * @param changePasswordConfirmation Optional, but if provided, we will ensure that it matches the value sent in &#x60;change_password&#x60;. (optional)
   * @param email User&#39;s email. (optional)
   * @param grantPermission Permission to grant on the user root.  Can be blank or &#x60;full&#x60;, &#x60;read&#x60;, &#x60;write&#x60;, &#x60;list&#x60;, or &#x60;history&#x60;. (optional)
   * @param groupId Group ID to associate this user with. (optional)
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
  public ApiResponse<UserEntity> postUsersWithHttpInfo(File avatarFile, Boolean avatarDelete, String changePassword, String changePasswordConfirmation, String email, String grantPermission, Integer groupId, String groupIds, String password, String passwordConfirmation, Boolean announcementsRead, String allowedIps, Boolean attachmentsPermission, DateTime authenticateUntil, String authenticationMethod, Boolean billingPermission, Boolean bypassInactiveDisable, Boolean bypassSiteAllowedIps, Boolean davPermission, Boolean disabled, Boolean ftpPermission, String headerText, String language, Integer notificationDailySendTime, String name, String company, String notes, Boolean officeIntegrationEnabled, Integer passwordValidityDays, Boolean receiveAdminAlerts, Boolean requirePasswordChange, Boolean restapiPermission, Boolean selfManaged, Boolean sftpPermission, Boolean siteAdmin, Boolean skipWelcomeScreen, String sslRequired, Integer ssoStrategyId, Boolean subscribeToNewsletter, String require2fa, String timeZone, String userRoot, String username) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/users";

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
if (groupId != null)
      localVarFormParams.put("group_id", groupId);
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
  /**
   * Trigger 2FA Reset process for user who has lost access to their existing 2FA methods.
   * Trigger 2FA Reset process for user who has lost access to their existing 2FA methods.
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void postUsersId2faReset(Integer id) throws ApiException {

    postUsersId2faResetWithHttpInfo(id);
  }

  /**
   * Trigger 2FA Reset process for user who has lost access to their existing 2FA methods.
   * Trigger 2FA Reset process for user who has lost access to their existing 2FA methods.
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> postUsersId2faResetWithHttpInfo(Integer id) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Resend user welcome email
   * Resend user welcome email
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void postUsersIdResendWelcomeEmail(Integer id) throws ApiException {

    postUsersIdResendWelcomeEmailWithHttpInfo(id);
  }

  /**
   * Resend user welcome email
   * Resend user welcome email
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> postUsersIdResendWelcomeEmailWithHttpInfo(Integer id) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Unlock user who has been locked out due to failed logins.
   * Unlock user who has been locked out due to failed logins.
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void postUsersIdUnlock(Integer id) throws ApiException {

    postUsersIdUnlockWithHttpInfo(id);
  }

  /**
   * Unlock user who has been locked out due to failed logins.
   * Unlock user who has been locked out due to failed logins.
   * @param id User ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> postUsersIdUnlockWithHttpInfo(Integer id) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Create Api Key
   * Create Api Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param name Internal name for the API Key.  For your use. (optional)
   * @param expiresAt API Key expiration date (optional)
   * @param permissionSet Permissions for this API Key.  Keys with the &#x60;desktop_app&#x60; permission set only have the ability to do the functions provided in our Desktop App (File and Share Link operations).  Additional permission sets may become available in the future, such as for a Site Admin to give a key with no administrator privileges.  If you have ideas for permission sets, please let us know. (optional, default to full)
   * @param path Folder path restriction for this api key. (optional)
   * @return ApiKeyEntity
   * @throws ApiException if fails to make API call
   */
  public ApiKeyEntity postUsersUserIdApiKeys(Integer userId, String name, DateTime expiresAt, String permissionSet, String path) throws ApiException {
    return postUsersUserIdApiKeysWithHttpInfo(userId, name, expiresAt, permissionSet, path).getData();
      }

  /**
   * Create Api Key
   * Create Api Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param name Internal name for the API Key.  For your use. (optional)
   * @param expiresAt API Key expiration date (optional)
   * @param permissionSet Permissions for this API Key.  Keys with the &#x60;desktop_app&#x60; permission set only have the ability to do the functions provided in our Desktop App (File and Share Link operations).  Additional permission sets may become available in the future, such as for a Site Admin to give a key with no administrator privileges.  If you have ideas for permission sets, please let us know. (optional, default to full)
   * @param path Folder path restriction for this api key. (optional)
   * @return ApiResponse&lt;ApiKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiKeyEntity> postUsersUserIdApiKeysWithHttpInfo(Integer userId, String name, DateTime expiresAt, String permissionSet, String path) throws ApiException {
    Object localVarPostBody = null;
    
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


    
    if (name != null)
      localVarFormParams.put("name", name);
if (expiresAt != null)
      localVarFormParams.put("expires_at", expiresAt);
if (permissionSet != null)
      localVarFormParams.put("permission_set", permissionSet);
if (path != null)
      localVarFormParams.put("path", path);

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
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create As2 Key
   * Create As2 Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param as2PartnershipName AS2 Partnership Name (required)
   * @param publicKey Actual contents of Public key. (required)
   * @return As2KeyEntity
   * @throws ApiException if fails to make API call
   */
  public As2KeyEntity postUsersUserIdAs2Keys(Integer userId, String as2PartnershipName, String publicKey) throws ApiException {
    return postUsersUserIdAs2KeysWithHttpInfo(userId, as2PartnershipName, publicKey).getData();
      }

  /**
   * Create As2 Key
   * Create As2 Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param as2PartnershipName AS2 Partnership Name (required)
   * @param publicKey Actual contents of Public key. (required)
   * @return ApiResponse&lt;As2KeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<As2KeyEntity> postUsersUserIdAs2KeysWithHttpInfo(Integer userId, String as2PartnershipName, String publicKey) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling postUsersUserIdAs2Keys");
    }
    
    // verify the required parameter 'as2PartnershipName' is set
    if (as2PartnershipName == null) {
      throw new ApiException(400, "Missing the required parameter 'as2PartnershipName' when calling postUsersUserIdAs2Keys");
    }
    
    // verify the required parameter 'publicKey' is set
    if (publicKey == null) {
      throw new ApiException(400, "Missing the required parameter 'publicKey' when calling postUsersUserIdAs2Keys");
    }
    
    // create path and map variables
    String localVarPath = "/users/{user_id}/as2_keys"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (as2PartnershipName != null)
      localVarFormParams.put("as2_partnership_name", as2PartnershipName);
if (publicKey != null)
      localVarFormParams.put("public_key", publicKey);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<As2KeyEntity> localVarReturnType = new GenericType<As2KeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Public Key
   * Create Public Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param title Internal reference for key. (required)
   * @param publicKey Actual contents of SSH key. (required)
   * @return PublicKeyEntity
   * @throws ApiException if fails to make API call
   */
  public PublicKeyEntity postUsersUserIdPublicKeys(Integer userId, String title, String publicKey) throws ApiException {
    return postUsersUserIdPublicKeysWithHttpInfo(userId, title, publicKey).getData();
      }

  /**
   * Create Public Key
   * Create Public Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (required)
   * @param title Internal reference for key. (required)
   * @param publicKey Actual contents of SSH key. (required)
   * @return ApiResponse&lt;PublicKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicKeyEntity> postUsersUserIdPublicKeysWithHttpInfo(Integer userId, String title, String publicKey) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling postUsersUserIdPublicKeys");
    }
    
    // verify the required parameter 'title' is set
    if (title == null) {
      throw new ApiException(400, "Missing the required parameter 'title' when calling postUsersUserIdPublicKeys");
    }
    
    // verify the required parameter 'publicKey' is set
    if (publicKey == null) {
      throw new ApiException(400, "Missing the required parameter 'publicKey' when calling postUsersUserIdPublicKeys");
    }
    
    // create path and map variables
    String localVarPath = "/users/{user_id}/public_keys"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (title != null)
      localVarFormParams.put("title", title);
if (publicKey != null)
      localVarFormParams.put("public_key", publicKey);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicKeyEntity> localVarReturnType = new GenericType<PublicKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
