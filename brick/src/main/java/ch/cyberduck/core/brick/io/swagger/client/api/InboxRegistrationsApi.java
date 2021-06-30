package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.InboxRegistrationEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class InboxRegistrationsApi {
  private ApiClient apiClient;

  public InboxRegistrationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public InboxRegistrationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List Inbox Registrations
   * List Inbox Registrations
   * @param folderBehaviorId ID of the associated Inbox. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;InboxRegistrationEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<InboxRegistrationEntity> getInboxRegistrations(Integer folderBehaviorId, String cursor, Integer perPage) throws ApiException {
    return getInboxRegistrationsWithHttpInfo(folderBehaviorId, cursor, perPage).getData();
      }

  /**
   * List Inbox Registrations
   * List Inbox Registrations
   * @param folderBehaviorId ID of the associated Inbox. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;InboxRegistrationEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<InboxRegistrationEntity>> getInboxRegistrationsWithHttpInfo(Integer folderBehaviorId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'folderBehaviorId' is set
    if (folderBehaviorId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderBehaviorId' when calling getInboxRegistrations");
    }
    
    // create path and map variables
    String localVarPath = "/inbox_registrations";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "folder_behavior_id", folderBehaviorId));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<InboxRegistrationEntity>> localVarReturnType = new GenericType<List<InboxRegistrationEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
