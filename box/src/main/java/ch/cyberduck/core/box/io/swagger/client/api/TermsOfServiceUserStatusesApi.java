package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body67;
import ch.cyberduck.core.box.io.swagger.client.model.Body68;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.TermsOfServiceUserStatus;
import ch.cyberduck.core.box.io.swagger.client.model.TermsOfServiceUserStatuses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class TermsOfServiceUserStatusesApi {
  private ApiClient apiClient;

  public TermsOfServiceUserStatusesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TermsOfServiceUserStatusesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List terms of service user statuses
   * Retrieves an overview of users and their status for a terms of service, including Whether they have accepted the terms and when.
   * @param tosId The ID of the terms of service. (required)
   * @param userId Limits results to the given user ID. (optional)
   * @return TermsOfServiceUserStatuses
   * @throws ApiException if fails to make API call
   */
  public TermsOfServiceUserStatuses getTermsOfServiceUserStatuses(String tosId, String userId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'tosId' is set
    if (tosId == null) {
      throw new ApiException(400, "Missing the required parameter 'tosId' when calling getTermsOfServiceUserStatuses");
    }
    // create path and map variables
    String localVarPath = "/terms_of_service_user_statuses";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "tos_id", tosId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<TermsOfServiceUserStatuses> localVarReturnType = new GenericType<TermsOfServiceUserStatuses>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create terms of service status for new user
   * Sets the status for a terms of service for a user.
   * @param body  (optional)
   * @return TermsOfServiceUserStatus
   * @throws ApiException if fails to make API call
   */
  public TermsOfServiceUserStatus postTermsOfServiceUserStatuses(Body67 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/terms_of_service_user_statuses";

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<TermsOfServiceUserStatus> localVarReturnType = new GenericType<TermsOfServiceUserStatus>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update terms of service status for existing user
   * Updates the status for a terms of service for a user.
   * @param termsOfServiceUserStatusId The ID of the terms of service status. (required)
   * @param body  (optional)
   * @return TermsOfServiceUserStatus
   * @throws ApiException if fails to make API call
   */
  public TermsOfServiceUserStatus putTermsOfServiceUserStatusesId(String termsOfServiceUserStatusId, Body68 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'termsOfServiceUserStatusId' is set
    if (termsOfServiceUserStatusId == null) {
      throw new ApiException(400, "Missing the required parameter 'termsOfServiceUserStatusId' when calling putTermsOfServiceUserStatusesId");
    }
    // create path and map variables
    String localVarPath = "/terms_of_service_user_statuses/{terms_of_service_user_status_id}"
      .replaceAll("\\{" + "terms_of_service_user_status_id" + "\\}", apiClient.escapeString(termsOfServiceUserStatusId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<TermsOfServiceUserStatus> localVarReturnType = new GenericType<TermsOfServiceUserStatus>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
