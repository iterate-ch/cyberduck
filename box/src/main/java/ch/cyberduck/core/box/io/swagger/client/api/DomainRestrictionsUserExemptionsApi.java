package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body70;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.CollaborationAllowlistExemptTarget;
import ch.cyberduck.core.box.io.swagger.client.model.CollaborationAllowlistExemptTargets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class DomainRestrictionsUserExemptionsApi {
  private ApiClient apiClient;

  public DomainRestrictionsUserExemptionsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DomainRestrictionsUserExemptionsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove user from list of users exempt from domain restrictions
   * Removes a user&#x27;s exemption from the restrictions set out by the allowed list of domains for collaborations.
   * @param collaborationWhitelistExemptTargetId The ID of the exemption to the list. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCollaborationWhitelistExemptTargetsId(String collaborationWhitelistExemptTargetId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'collaborationWhitelistExemptTargetId' is set
    if (collaborationWhitelistExemptTargetId == null) {
      throw new ApiException(400, "Missing the required parameter 'collaborationWhitelistExemptTargetId' when calling deleteCollaborationWhitelistExemptTargetsId");
    }
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_exempt_targets/{collaboration_whitelist_exempt_target_id}"
      .replaceAll("\\{" + "collaboration_whitelist_exempt_target_id" + "\\}", apiClient.escapeString(collaborationWhitelistExemptTargetId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List users exempt from collaboration domain restrictions
   * Returns a list of users who have been exempt from the collaboration domain restrictions.
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @return CollaborationAllowlistExemptTargets
   * @throws ApiException if fails to make API call
   */
  public CollaborationAllowlistExemptTargets getCollaborationWhitelistExemptTargets(String marker, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_exempt_targets";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<CollaborationAllowlistExemptTargets> localVarReturnType = new GenericType<CollaborationAllowlistExemptTargets>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get user exempt from collaboration domain restrictions
   * Returns a users who has been exempt from the collaboration domain restrictions.
   * @param collaborationWhitelistExemptTargetId The ID of the exemption to the list. (required)
   * @return CollaborationAllowlistExemptTarget
   * @throws ApiException if fails to make API call
   */
  public CollaborationAllowlistExemptTarget getCollaborationWhitelistExemptTargetsId(String collaborationWhitelistExemptTargetId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'collaborationWhitelistExemptTargetId' is set
    if (collaborationWhitelistExemptTargetId == null) {
      throw new ApiException(400, "Missing the required parameter 'collaborationWhitelistExemptTargetId' when calling getCollaborationWhitelistExemptTargetsId");
    }
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_exempt_targets/{collaboration_whitelist_exempt_target_id}"
      .replaceAll("\\{" + "collaboration_whitelist_exempt_target_id" + "\\}", apiClient.escapeString(collaborationWhitelistExemptTargetId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<CollaborationAllowlistExemptTarget> localVarReturnType = new GenericType<CollaborationAllowlistExemptTarget>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create user exemption from collaboration domain restrictions
   * Exempts a user from the restrictions set out by the allowed list of domains for collaborations.
   * @param body  (optional)
   * @return CollaborationAllowlistExemptTarget
   * @throws ApiException if fails to make API call
   */
  public CollaborationAllowlistExemptTarget postCollaborationWhitelistExemptTargets(Body70 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_exempt_targets";

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

    GenericType<CollaborationAllowlistExemptTarget> localVarReturnType = new GenericType<CollaborationAllowlistExemptTarget>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
