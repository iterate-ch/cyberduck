package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.Group;
import ch.cyberduck.core.storegate.io.swagger.client.model.GroupRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
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
   * Add users to a group
   * 
   * @param id The id of the group (required)
   * @param userId The id to user you want to add (required)
   * @throws ApiException if fails to make API call
   */
  public void groupsAddUsersToGroup(String id, String userId) throws ApiException {

    groupsAddUsersToGroupWithHttpInfo(id, userId);
  }

  /**
   * Add users to a group
   * 
   * @param id The id of the group (required)
   * @param userId The id to user you want to add (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> groupsAddUsersToGroupWithHttpInfo(String id, String userId) throws ApiException {
    Object localVarPostBody = userId;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsAddUsersToGroup");
    }
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling groupsAddUsersToGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/groups/{id}/users"
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


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Create a new group
   * 
   * @param groupRequest Name and description (required)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group groupsCreateGroup(GroupRequest groupRequest) throws ApiException {
    return groupsCreateGroupWithHttpInfo(groupRequest).getData();
      }

  /**
   * Create a new group
   * 
   * @param groupRequest Name and description (required)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> groupsCreateGroupWithHttpInfo(GroupRequest groupRequest) throws ApiException {
    Object localVarPostBody = groupRequest;
    
    // verify the required parameter 'groupRequest' is set
    if (groupRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'groupRequest' when calling groupsCreateGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/groups";

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

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Deletes a group
   * 
   * @param id The id of the group to delete (required)
   * @throws ApiException if fails to make API call
   */
  public void groupsDeleteGroup(String id) throws ApiException {

    groupsDeleteGroupWithHttpInfo(id);
  }

  /**
   * Deletes a group
   * 
   * @param id The id of the group to delete (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> groupsDeleteGroupWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsDeleteGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/groups/{id}"
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
   * Get a group
   * 
   * @param id The group id (required)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group groupsGetGroup(String id) throws ApiException {
    return groupsGetGroupWithHttpInfo(id).getData();
      }

  /**
   * Get a group
   * 
   * @param id The group id (required)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> groupsGetGroupWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsGetGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/groups/{id}"
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

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get all users from a group
   * 
   * @param id The id of the group (required)
   * @return List&lt;User&gt;
   * @throws ApiException if fails to make API call
   */
  public List<User> groupsGetGroupUsers(String id) throws ApiException {
    return groupsGetGroupUsersWithHttpInfo(id).getData();
      }

  /**
   * Get all users from a group
   * 
   * @param id The id of the group (required)
   * @return ApiResponse&lt;List&lt;User&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<User>> groupsGetGroupUsersWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsGetGroupUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/groups/{id}/users"
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

    GenericType<List<User>> localVarReturnType = new GenericType<List<User>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get all groups
   * 
   * @return List&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Group> groupsGetGroups() throws ApiException {
    return groupsGetGroupsWithHttpInfo().getData();
      }

  /**
   * Get all groups
   * 
   * @return ApiResponse&lt;List&lt;Group&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<Group>> groupsGetGroupsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/groups";

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

    GenericType<List<Group>> localVarReturnType = new GenericType<List<Group>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Remove a user from a specific group
   * 
   * @param id The id to the group you want to delete the user (required)
   * @param userId The id to user you want to delete (required)
   * @throws ApiException if fails to make API call
   */
  public void groupsRemoveUserFromGroup(String id, String userId) throws ApiException {

    groupsRemoveUserFromGroupWithHttpInfo(id, userId);
  }

  /**
   * Remove a user from a specific group
   * 
   * @param id The id to the group you want to delete the user (required)
   * @param userId The id to user you want to delete (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> groupsRemoveUserFromGroupWithHttpInfo(String id, String userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsRemoveUserFromGroup");
    }
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling groupsRemoveUserFromGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/groups/{id}/users/{userId}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()))
      .replaceAll("\\{" + "userId" + "\\}", apiClient.escapeString(userId.toString()));

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
   * Updates an existing group
   * 
   * @param id The id of the group (required)
   * @param groupRequest Name and description (required)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group groupsUpdateGroup(String id, GroupRequest groupRequest) throws ApiException {
    return groupsUpdateGroupWithHttpInfo(id, groupRequest).getData();
      }

  /**
   * Updates an existing group
   * 
   * @param id The id of the group (required)
   * @param groupRequest Name and description (required)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> groupsUpdateGroupWithHttpInfo(String id, GroupRequest groupRequest) throws ApiException {
    Object localVarPostBody = groupRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsUpdateGroup");
    }
    
    // verify the required parameter 'groupRequest' is set
    if (groupRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'groupRequest' when calling groupsUpdateGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/groups/{id}"
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

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
