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
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateUserRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-12-02T20:20:31.369+01:00")
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
    String localVarPath = "/v4/users/{id}/password"
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
   * Send a password mail to a sub user.
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public void usersChangeSubUserPassword_0(String id) throws ApiException {

    usersChangeSubUserPassword_0WithHttpInfo(id);
  }

  /**
   * Send a password mail to a sub user.
   * 
   * @param id The id to the specific user (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> usersChangeSubUserPassword_0WithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersChangeSubUserPassword_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users/{id}/passwordrequest"
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
    String localVarPath = "/v4/users/{id}/status"
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
    String localVarPath = "/v4/users/{id}"
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
   * Get users
   * 
   * @param pageIndex  (required)
   * @param pageSize  (required)
   * @param sortExpression  (required)
   * @param searchCriteria  (required)
   * @return ExtendedUserContents
   * @throws ApiException if fails to make API call
   */
  public ExtendedUserContents usersGet(Integer pageIndex, Integer pageSize, String sortExpression, String searchCriteria) throws ApiException {
    return usersGetWithHttpInfo(pageIndex, pageSize, sortExpression, searchCriteria).getData();
      }

  /**
   * Get users
   * 
   * @param pageIndex  (required)
   * @param pageSize  (required)
   * @param sortExpression  (required)
   * @param searchCriteria  (required)
   * @return ApiResponse&lt;ExtendedUserContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUserContents> usersGetWithHttpInfo(Integer pageIndex, Integer pageSize, String sortExpression, String searchCriteria) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling usersGet");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling usersGet");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling usersGet");
    }
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling usersGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));

    
    
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
    String localVarPath = "/v4/users/all";

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
    String localVarPath = "/v4/users/me";

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
    String localVarPath = "/v4/users/{id}/groups"
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
   * Get a user
   * 
   * @param id The id for the user (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersGet_0(String id) throws ApiException {
    return usersGet_0WithHttpInfo(id).getData();
      }

  /**
   * Get a user
   * 
   * @param id The id for the user (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersGet_0WithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersGet_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users/{id}"
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
   * Create a new user with username and password
   * 
   * @param createUserRequest The CreateUserRequest (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersPost(CreateUserRequest createUserRequest) throws ApiException {
    return usersPostWithHttpInfo(createUserRequest).getData();
      }

  /**
   * Create a new user with username and password
   * 
   * @param createUserRequest The CreateUserRequest (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersPostWithHttpInfo(CreateUserRequest createUserRequest) throws ApiException {
    Object localVarPostBody = createUserRequest;
    
    // verify the required parameter 'createUserRequest' is set
    if (createUserRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createUserRequest' when calling usersPost");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users";

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
   * Create a new user via invite
   * 
   * @param createUserRequest The CreateUserRequest (required)
   * @return ExtendedUser
   * @throws ApiException if fails to make API call
   */
  public ExtendedUser usersPostInvite(UpdateUserRequest createUserRequest) throws ApiException {
    return usersPostInviteWithHttpInfo(createUserRequest).getData();
      }

  /**
   * Create a new user via invite
   * 
   * @param createUserRequest The CreateUserRequest (required)
   * @return ApiResponse&lt;ExtendedUser&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExtendedUser> usersPostInviteWithHttpInfo(UpdateUserRequest createUserRequest) throws ApiException {
    Object localVarPostBody = createUserRequest;
    
    // verify the required parameter 'createUserRequest' is set
    if (createUserRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createUserRequest' when calling usersPostInvite");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users/invite";

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
    String localVarPath = "/v4/users/{id}"
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
    String localVarPath = "/v4/users/{id}/reminder"
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
    String localVarPath = "/v4/users/{id}/subadmin"
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
}
