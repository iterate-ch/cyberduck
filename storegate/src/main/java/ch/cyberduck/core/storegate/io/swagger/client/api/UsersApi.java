package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateUserRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.ExtendedUser;
import ch.cyberduck.core.storegate.io.swagger.client.model.ExtendedUserContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.InviteUserRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateUserRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.User;
import ch.cyberduck.core.storegate.io.swagger.client.model.UserExportRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
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
   * Activate inactive users              &lt;param name&#x3D;\&quot;id\&quot;&gt;The id of the user to activate&lt;/param&gt;
   * 
   * @param id  (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersActivateDormantUser(String id) throws ApiException {
    return usersActivateDormantUserWithHttpInfo(id).getData();
      }

  /**
   * Activate inactive users              &lt;param name&#x3D;\&quot;id\&quot;&gt;The id of the user to activate&lt;/param&gt;
   * 
   * @param id  (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersActivateDormantUserWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersActivateDormantUser");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/activateDormantUser"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<ExtendedUser> localVarReturnType = new GenericType<ExtendedUser>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Activate all inactive users
   * 
   * @return List&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ExtendedUser> usersActivateDormantUsers() throws ApiException {
    return usersActivateDormantUsersWithHttpInfo().getData();
      }

  /**
   * Activate all inactive users
   * 
   * @return ApiResponse&lt;List&lt;ExtendedUser&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<ExtendedUser>> usersActivateDormantUsersWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/users/activateDormantUsers";

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

    GenericType<List<ExtendedUser>> localVarReturnType = new GenericType<List<ExtendedUser>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Set a new password on sub user.
   * 
   * @param id The id to the specific user (required)
   * @param password The new password (required)
   * @throws ApiException if fails to make API call
   */
  public void usersChangeSubUserPassword(String id, String password) throws ApiException {

    usersChangeSubUserPasswordWithHttpInfo(id, password);
  }

  /**
   * Set a new password on sub user.
   * 
   * @param id The id to the specific user (required)
   * @param password The new password (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersChangeSubUserPasswordWithHttpInfo(String id, String password) throws ApiException {
    Object localVarPostBody = password;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersChangeSubUserPassword");
    }
    
    // verify the required parameter 'password' is set
    if (password == null) {
      throw new ApiException(400, "Missing the required parameter 'password' when calling usersChangeSubUserPassword");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/password"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Enable/disable a user
   * 
   * @param id The id to the specific user (required)
   * @param enabled True or false (required)
   * @throws ApiException if fails to make API call
   */
  public void usersChangeSubUserStatus(String id, Boolean enabled) throws ApiException {

    usersChangeSubUserStatusWithHttpInfo(id, enabled);
  }

  /**
   * Enable/disable a user
   * 
   * @param id The id to the specific user (required)
   * @param enabled True or false (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersChangeSubUserStatusWithHttpInfo(String id, Boolean enabled) throws ApiException {
    Object localVarPostBody = enabled;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersChangeSubUserStatus");
    }
    
    // verify the required parameter 'enabled' is set
    if (enabled == null) {
      throw new ApiException(400, "Missing the required parameter 'enabled' when calling usersChangeSubUserStatus");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/status"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Deletes a user, this can&#39;t be undone!
   * 
   * @param id The id to the specific user to delete (required)
   * @throws ApiException if fails to make API call
   */
  public void usersDelete(String id) throws ApiException {

    usersDeleteWithHttpInfo(id);
  }

  /**
   * Deletes a user, this can&#39;t be undone!
   * 
   * @param id The id to the specific user to delete (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}"
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

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete all the users tokens
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public void usersDeleteUserAllTokens(String id) throws ApiException {

    usersDeleteUserAllTokensWithHttpInfo(id);
  }

  /**
   * Delete all the users tokens
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersDeleteUserAllTokensWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersDeleteUserAllTokens");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/tokens"
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

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Resets TwoFactor login for specified userId
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public void usersDisableTwoFactorAuthentication(String id) throws ApiException {

    usersDisableTwoFactorAuthenticationWithHttpInfo(id);
  }

  /**
   * Resets TwoFactor login for specified userId
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersDisableTwoFactorAuthenticationWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersDisableTwoFactorAuthentication");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/disableTwoFactorAuthentication"
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

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Download userlist via token
   * 
   * @param downloadid  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String usersGet(String downloadid) throws ApiException {
    return usersGetWithHttpInfo(downloadid).getData();
      }

  /**
   * Download userlist via token
   * 
   * @param downloadid  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> usersGetWithHttpInfo(String downloadid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'downloadid' is set
    if (downloadid == null) {
      throw new ApiException(400, "Missing the required parameter 'downloadid' when calling usersGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/export/{downloadid}"
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
   * Get all users
   * 
   * @return List&lt;User&gt;
   * @throws ApiException if fails to make API call
   */
  public List<User> usersGetAll() throws ApiException {
    return usersGetAllWithHttpInfo().getData();
      }

  /**
   * Get all users
   * 
   * @return ApiResponse&lt;List&lt;User&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<User>> usersGetAllWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/users/all";

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

    GenericType<List<User>> localVarReturnType = new GenericType<List<User>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download userlist
   * 
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String usersGetExportPermissions() throws ApiException {
    return usersGetExportPermissionsWithHttpInfo().getData();
      }

  /**
   * Download userlist
   * 
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> usersGetExportPermissionsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/users/exportPermissions";

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
   * Download userlist via token
   * 
   * @param downloadid  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String usersGetExportPermissions_0(String downloadid) throws ApiException {
    return usersGetExportPermissions_0WithHttpInfo(downloadid).getData();
      }

  /**
   * Download userlist via token
   * 
   * @param downloadid  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> usersGetExportPermissions_0WithHttpInfo(String downloadid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'downloadid' is set
    if (downloadid == null) {
      throw new ApiException(400, "Missing the required parameter 'downloadid' when calling usersGetExportPermissions_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/exportPermissions/{downloadid}"
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
   * Get the current user
   * 
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersGetMe() throws ApiException {
    return usersGetMeWithHttpInfo().getData();
      }

  /**
   * Get the current user
   * 
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersGetMeWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/users/me";

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

    GenericType<ExtendedUser> localVarReturnType = new GenericType<ExtendedUser>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get a user
   * 
   * @param id The id for the user (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersGetUser(String id) throws ApiException {
    return usersGetUserWithHttpInfo(id).getData();
      }

  /**
   * Get a user
   * 
   * @param id The id for the user (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersGetUserWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersGetUser");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<ExtendedUser> localVarReturnType = new GenericType<ExtendedUser>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get all groups the user is a member in
   * 
   * @param id The id to the user (required)
   * @return List&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public List<String> usersGetUserGroups(String id) throws ApiException {
    return usersGetUserGroupsWithHttpInfo(id).getData();
      }

  /**
   * Get all groups the user is a member in
   * 
   * @param id The id to the user (required)
   * @return ApiResponse&lt;List&lt;String&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<String>> usersGetUserGroupsWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersGetUserGroups");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/groups"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<List<String>> localVarReturnType = new GenericType<List<String>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download userlist
   * 
   * @param searchCriteria  (required)
   * @param sortExpression  (required)
   * @param timeZone  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String usersGet_0(String searchCriteria, String sortExpression, String timeZone) throws ApiException {
    return usersGet_0WithHttpInfo(searchCriteria, sortExpression, timeZone).getData();
      }

  /**
   * Download userlist
   * 
   * @param searchCriteria  (required)
   * @param sortExpression  (required)
   * @param timeZone  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> usersGet_0WithHttpInfo(String searchCriteria, String sortExpression, String timeZone) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling usersGet_0");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling usersGet_0");
    }
    
    // verify the required parameter 'timeZone' is set
    if (timeZone == null) {
      throw new ApiException(400, "Missing the required parameter 'timeZone' when calling usersGet_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/export";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "timeZone", timeZone));

    
    
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
   * Get user list
   * 
   * @param pageIndex  (required)
   * @param pageSize  (required)
   * @param searchCriteria  (required)
   * @param sortExpression  (required)
   * @return ExtendedUserContents
   * @throws ApiException if fails to make API call
   */
  public ExtendedUserContents usersGet_1(Integer pageIndex, Integer pageSize, String searchCriteria, String sortExpression) throws ApiException {
    return usersGet_1WithHttpInfo(pageIndex, pageSize, searchCriteria, sortExpression).getData();
      }

  /**
   * Get user list
   * 
   * @param pageIndex  (required)
   * @param pageSize  (required)
   * @param searchCriteria  (required)
   * @param sortExpression  (required)
   * @return ApiResponse&lt;ExtendedUserContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUserContents> usersGet_1WithHttpInfo(Integer pageIndex, Integer pageSize, String searchCriteria, String sortExpression) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling usersGet_1");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling usersGet_1");
    }
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling usersGet_1");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling usersGet_1");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ExtendedUserContents> localVarReturnType = new GenericType<ExtendedUserContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get download user list token
   * 
   * @param request  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String usersPost(UserExportRequest request) throws ApiException {
    return usersPostWithHttpInfo(request).getData();
      }

  /**
   * Get download user list token
   * 
   * @param request  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> usersPostWithHttpInfo(UserExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling usersPost");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/export";

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
   * Get download user list token
   * 
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String usersPostExportPermissions() throws ApiException {
    return usersPostExportPermissionsWithHttpInfo().getData();
      }

  /**
   * Get download user list token
   * 
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> usersPostExportPermissionsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/users/exportPermissions";

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
  /**
   * Create a new user via invite
   * 
   * @param inviteUserRequest The CreateUserRequest (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersPostInvite(InviteUserRequest inviteUserRequest) throws ApiException {
    return usersPostInviteWithHttpInfo(inviteUserRequest).getData();
      }

  /**
   * Create a new user via invite
   * 
   * @param inviteUserRequest The CreateUserRequest (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersPostInviteWithHttpInfo(InviteUserRequest inviteUserRequest) throws ApiException {
    Object localVarPostBody = inviteUserRequest;
    
    // verify the required parameter 'inviteUserRequest' is set
    if (inviteUserRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'inviteUserRequest' when calling usersPostInvite");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/invite";

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

    GenericType<ExtendedUser> localVarReturnType = new GenericType<ExtendedUser>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create a new user with username and password
   * 
   * @param createUserRequest The CreateUserRequest (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersPost_0(CreateUserRequest createUserRequest) throws ApiException {
    return usersPost_0WithHttpInfo(createUserRequest).getData();
      }

  /**
   * Create a new user with username and password
   * 
   * @param createUserRequest The CreateUserRequest (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersPost_0WithHttpInfo(CreateUserRequest createUserRequest) throws ApiException {
    Object localVarPostBody = createUserRequest;
    
    // verify the required parameter 'createUserRequest' is set
    if (createUserRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createUserRequest' when calling usersPost_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users";

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

    GenericType<ExtendedUser> localVarReturnType = new GenericType<ExtendedUser>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Updates an existing user
   * 
   * @param id The id of the user (required)
   * @param updateUserRequest The updateUserRequest (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersPut(String id, UpdateUserRequest updateUserRequest) throws ApiException {
    return usersPutWithHttpInfo(id, updateUserRequest).getData();
      }

  /**
   * Updates an existing user
   * 
   * @param id The id of the user (required)
   * @param updateUserRequest The updateUserRequest (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersPutWithHttpInfo(String id, UpdateUserRequest updateUserRequest) throws ApiException {
    Object localVarPostBody = updateUserRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersPut");
    }
    
    // verify the required parameter 'updateUserRequest' is set
    if (updateUserRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'updateUserRequest' when calling usersPut");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<ExtendedUser> localVarReturnType = new GenericType<ExtendedUser>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Send a password mail to a sub user.
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public void usersSendPasswordRequest(String id) throws ApiException {

    usersSendPasswordRequestWithHttpInfo(id);
  }

  /**
   * Send a password mail to a sub user.
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersSendPasswordRequestWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersSendPasswordRequest");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/passwordrequest"
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

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Sends a copy of the invite mail
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public void usersSendReminder(String id) throws ApiException {

    usersSendReminderWithHttpInfo(id);
  }

  /**
   * Sends a copy of the invite mail
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersSendReminderWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersSendReminder");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/reminder"
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

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Enable/disable sub admin permissions
   * 
   * @param id The id to the specific user (required)
   * @param subadmin True or false (required)
   * @throws ApiException if fails to make API call
   */
  public void usersSubUserSubAdmin(String id, Boolean subadmin) throws ApiException {

    usersSubUserSubAdminWithHttpInfo(id, subadmin);
  }

  /**
   * Enable/disable sub admin permissions
   * 
   * @param id The id to the specific user (required)
   * @param subadmin True or false (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersSubUserSubAdminWithHttpInfo(String id, Boolean subadmin) throws ApiException {
    Object localVarPostBody = subadmin;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersSubUserSubAdmin");
    }
    
    // verify the required parameter 'subadmin' is set
    if (subadmin == null) {
      throw new ApiException(400, "Missing the required parameter 'subadmin' when calling usersSubUserSubAdmin");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/subadmin"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param id  (required)
   * @return Long
   * @throws ApiException if fails to make API call
   */
  public Long usersTakeOverPublicLinks(String id) throws ApiException {
    return usersTakeOverPublicLinksWithHttpInfo(id).getData();
      }

  /**
   * 
   * 
   * @param id  (required)
   * @return ApiResponse&lt;Long&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Long> usersTakeOverPublicLinksWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersTakeOverPublicLinks");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/users/{id}/takeOverPublicLinks"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<Long> localVarReturnType = new GenericType<Long>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
