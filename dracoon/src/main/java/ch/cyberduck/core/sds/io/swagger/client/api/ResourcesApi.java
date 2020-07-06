package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.Avatar;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.NotificationScopeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class ResourcesApi {
  private ApiClient apiClient;

  public ResourcesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ResourcesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get user avatar
   * ### Functional Description: Get user avatar.  ### Precondition: Valid: - user ID and - avatar UUID  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param userId User ID (required)
   * @param uuid UUID of the avatar (required)
   * @return Avatar
   * @throws ApiException if fails to make API call
   */
  public Avatar getUserAvatar(Long userId, String uuid) throws ApiException {
    return getUserAvatarWithHttpInfo(userId, uuid).getData();
      }

  /**
   * Get user avatar
   * ### Functional Description: Get user avatar.  ### Precondition: Valid: - user ID and - avatar UUID  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param userId User ID (required)
   * @param uuid UUID of the avatar (required)
   * @return ApiResponse&lt;Avatar&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Avatar> getUserAvatarWithHttpInfo(Long userId, String uuid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUserAvatar");
    }
    
    // verify the required parameter 'uuid' is set
    if (uuid == null) {
      throw new ApiException(400, "Missing the required parameter 'uuid' when calling getUserAvatar");
    }
    
    // create path and map variables
    String localVarPath = "/v4/resources/users/{user_id}/avatar/{uuid}"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()))
      .replaceAll("\\{" + "uuid" + "\\}", apiClient.escapeString(uuid.toString()));

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

    GenericType<Avatar> localVarReturnType = new GenericType<Avatar>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Request list of subscription scopes 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description: Retrieve a list of subscription scopes.  ### Precondition: Authenticated user.  ### Effects: List of scopes is returned.  ### &amp;#9432; Further Information: None.
   * @return NotificationScopeList
   * @throws ApiException if fails to make API call
   */
  public NotificationScopeList requestSubscriptionScopes() throws ApiException {
    return requestSubscriptionScopesWithHttpInfo().getData();
      }

  /**
   * Request list of subscription scopes 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description: Retrieve a list of subscription scopes.  ### Precondition: Authenticated user.  ### Effects: List of scopes is returned.  ### &amp;#9432; Further Information: None.
   * @return ApiResponse&lt;NotificationScopeList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NotificationScopeList> requestSubscriptionScopesWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/resources/user/notifications/scopes";

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

    GenericType<NotificationScopeList> localVarReturnType = new GenericType<NotificationScopeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
