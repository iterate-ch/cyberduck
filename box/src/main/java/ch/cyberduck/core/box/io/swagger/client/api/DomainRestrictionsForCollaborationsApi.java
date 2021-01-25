package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body69;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.CollaborationAllowlistEntries;
import ch.cyberduck.core.box.io.swagger.client.model.CollaborationAllowlistEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class DomainRestrictionsForCollaborationsApi {
  private ApiClient apiClient;

  public DomainRestrictionsForCollaborationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DomainRestrictionsForCollaborationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove domain from list of allowed collaboration domains
   * Removes a domain from the list of domains that have been deemed safe to create collaborations for within the current enterprise.
   * @param collaborationWhitelistEntryId The ID of the entry in the list. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCollaborationWhitelistEntriesId(String collaborationWhitelistEntryId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'collaborationWhitelistEntryId' is set
    if (collaborationWhitelistEntryId == null) {
      throw new ApiException(400, "Missing the required parameter 'collaborationWhitelistEntryId' when calling deleteCollaborationWhitelistEntriesId");
    }
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_entries/{collaboration_whitelist_entry_id}"
      .replaceAll("\\{" + "collaboration_whitelist_entry_id" + "\\}", apiClient.escapeString(collaborationWhitelistEntryId.toString()));

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
   * List allowed collaboration domains
   * Returns the list domains that have been deemed safe to create collaborations for within the current enterprise.
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @return CollaborationAllowlistEntries
   * @throws ApiException if fails to make API call
   */
  public CollaborationAllowlistEntries getCollaborationWhitelistEntries(String marker, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_entries";

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

    GenericType<CollaborationAllowlistEntries> localVarReturnType = new GenericType<CollaborationAllowlistEntries>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get allowed collaboration domain
   * Returns a domain that has been deemed safe to create collaborations for within the current enterprise.
   * @param collaborationWhitelistEntryId The ID of the entry in the list. (required)
   * @return CollaborationAllowlistEntry
   * @throws ApiException if fails to make API call
   */
  public CollaborationAllowlistEntry getCollaborationWhitelistEntriesId(String collaborationWhitelistEntryId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'collaborationWhitelistEntryId' is set
    if (collaborationWhitelistEntryId == null) {
      throw new ApiException(400, "Missing the required parameter 'collaborationWhitelistEntryId' when calling getCollaborationWhitelistEntriesId");
    }
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_entries/{collaboration_whitelist_entry_id}"
      .replaceAll("\\{" + "collaboration_whitelist_entry_id" + "\\}", apiClient.escapeString(collaborationWhitelistEntryId.toString()));

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

    GenericType<CollaborationAllowlistEntry> localVarReturnType = new GenericType<CollaborationAllowlistEntry>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Add domain to list of allowed collaboration domains
   * Creates a new entry in the list of allowed domains to allow collaboration for.
   * @param body  (optional)
   * @return CollaborationAllowlistEntry
   * @throws ApiException if fails to make API call
   */
  public CollaborationAllowlistEntry postCollaborationWhitelistEntries(Body69 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/collaboration_whitelist_entries";

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

    GenericType<CollaborationAllowlistEntry> localVarReturnType = new GenericType<CollaborationAllowlistEntry>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
