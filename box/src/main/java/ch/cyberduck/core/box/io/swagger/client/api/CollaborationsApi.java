package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body32;
import ch.cyberduck.core.box.io.swagger.client.model.Body33;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.Collaboration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class CollaborationsApi {
  private ApiClient apiClient;

  public CollaborationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public CollaborationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove collaboration
   * Deletes a single collaboration.
   * @param collaborationId The ID of the collaboration (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCollaborationsId(String collaborationId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'collaborationId' is set
    if (collaborationId == null) {
      throw new ApiException(400, "Missing the required parameter 'collaborationId' when calling deleteCollaborationsId");
    }
    // create path and map variables
    String localVarPath = "/collaborations/{collaboration_id}"
      .replaceAll("\\{" + "collaboration_id" + "\\}", apiClient.escapeString(collaborationId.toString()));

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
   * Get collaboration
   * Retrieves a single collaboration.
   * @param collaborationId The ID of the collaboration (required)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return Collaboration
   * @throws ApiException if fails to make API call
   */
  public Collaboration getCollaborationsId(String collaborationId, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'collaborationId' is set
    if (collaborationId == null) {
      throw new ApiException(400, "Missing the required parameter 'collaborationId' when calling getCollaborationsId");
    }
    // create path and map variables
    String localVarPath = "/collaborations/{collaboration_id}"
      .replaceAll("\\{" + "collaboration_id" + "\\}", apiClient.escapeString(collaborationId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Collaboration> localVarReturnType = new GenericType<Collaboration>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create collaboration
   * Adds a collaboration for a single user or a single group to a file or folder.  Collaborations can be created using email address, user IDs, or a group IDs.  If a collaboration is being created with a group, access to this endpoint is dependent on the group&#x27;s ability to be invited.
   * @param body  (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @param notify Determines if users should receive email notification for the action performed. (optional)
   * @return Collaboration
   * @throws ApiException if fails to make API call
   */
  public Collaboration postCollaborations(Body33 body, List<String> fields, Boolean notify) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/collaborations";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "notify", notify));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Collaboration> localVarReturnType = new GenericType<Collaboration>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update collaboration
   * Updates a collaboration.  Can be used to change the owner of an item, or to accept collaboration invites.
   * @param collaborationId The ID of the collaboration (required)
   * @param body  (optional)
   * @return Collaboration
   * @throws ApiException if fails to make API call
   */
  public Collaboration putCollaborationsId(String collaborationId, Body32 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'collaborationId' is set
    if (collaborationId == null) {
      throw new ApiException(400, "Missing the required parameter 'collaborationId' when calling putCollaborationsId");
    }
    // create path and map variables
    String localVarPath = "/collaborations/{collaboration_id}"
      .replaceAll("\\{" + "collaboration_id" + "\\}", apiClient.escapeString(collaborationId.toString()));

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

    GenericType<Collaboration> localVarReturnType = new GenericType<Collaboration>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
