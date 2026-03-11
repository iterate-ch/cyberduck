package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
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


public class ResourcesApi {
  private ApiClient apiClient;
  private Map<String, String> headers;

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

  public void setHeadersOverrides(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * Request list of subscription scopes
   * ### Description: Retrieve a list of subscription scopes.  ### Precondition: Authenticated user.  ### Postcondition: List of scopes is returned.  ### Further Information: None.
   * @return NotificationScopeList
   * @throws ApiException if fails to make API call
   */
  public NotificationScopeList requestSubscriptionScopes() throws ApiException {
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

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request user avatar
   * ### Description: Get user avatar.  ### Precondition: Valid user ID and avatar UUID  ### Postcondition: Avatar is returned.  ### Further Information: None.
   * @param uuid UUID of the avatar (required)
   * @param userId User ID (required)
   * @return Avatar
   * @throws ApiException if fails to make API call
   */
  public Avatar requestUserAvatar(String uuid, Long userId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'uuid' is set
    if (uuid == null) {
      throw new ApiException(400, "Missing the required parameter 'uuid' when calling requestUserAvatar");
    }
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling requestUserAvatar");
    }
    // create path and map variables
    String localVarPath = "/v4/resources/users/{user_id}/avatar/{uuid}"
      .replaceAll("\\{" + "uuid" + "\\}", apiClient.escapeString(uuid.toString()))
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

    GenericType<Avatar> localVarReturnType = new GenericType<Avatar>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
