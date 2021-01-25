package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body71;
import ch.cyberduck.core.box.io.swagger.client.model.Body72;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.StoragePolicyAssignment;
import ch.cyberduck.core.box.io.swagger.client.model.StoragePolicyAssignments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class StoragePolicyAssignmentsApi {
  private ApiClient apiClient;

  public StoragePolicyAssignmentsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public StoragePolicyAssignmentsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Unassign storage policy
   * Delete a storage policy assignment.  Deleting a storage policy assignment on a user will have the user inherit the enterprise&#x27;s default storage policy.  There is a rate limit for calling this endpoint of only twice per user in a 24 hour time frame.
   * @param storagePolicyAssignmentId The ID of the storage policy assignment. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteStoragePolicyAssignmentsId(String storagePolicyAssignmentId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'storagePolicyAssignmentId' is set
    if (storagePolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'storagePolicyAssignmentId' when calling deleteStoragePolicyAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/storage_policy_assignments/{storage_policy_assignment_id}"
      .replaceAll("\\{" + "storage_policy_assignment_id" + "\\}", apiClient.escapeString(storagePolicyAssignmentId.toString()));

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
   * List storage policy assignments
   * Fetches all the storage policy assignment for an enterprise or user.
   * @param resolvedForType The target type to return assignments for (required)
   * @param resolvedForId The ID of the user or enterprise to return assignments for (required)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @return StoragePolicyAssignments
   * @throws ApiException if fails to make API call
   */
  public StoragePolicyAssignments getStoragePolicyAssignments(String resolvedForType, String resolvedForId, String marker) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'resolvedForType' is set
    if (resolvedForType == null) {
      throw new ApiException(400, "Missing the required parameter 'resolvedForType' when calling getStoragePolicyAssignments");
    }
    // verify the required parameter 'resolvedForId' is set
    if (resolvedForId == null) {
      throw new ApiException(400, "Missing the required parameter 'resolvedForId' when calling getStoragePolicyAssignments");
    }
    // create path and map variables
    String localVarPath = "/storage_policy_assignments";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "resolved_for_type", resolvedForType));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "resolved_for_id", resolvedForId));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<StoragePolicyAssignments> localVarReturnType = new GenericType<StoragePolicyAssignments>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get storage policy assignment
   * Fetches a specific storage policy assignment.
   * @param storagePolicyAssignmentId The ID of the storage policy assignment. (required)
   * @return StoragePolicyAssignment
   * @throws ApiException if fails to make API call
   */
  public StoragePolicyAssignment getStoragePolicyAssignmentsId(String storagePolicyAssignmentId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'storagePolicyAssignmentId' is set
    if (storagePolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'storagePolicyAssignmentId' when calling getStoragePolicyAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/storage_policy_assignments/{storage_policy_assignment_id}"
      .replaceAll("\\{" + "storage_policy_assignment_id" + "\\}", apiClient.escapeString(storagePolicyAssignmentId.toString()));

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

    GenericType<StoragePolicyAssignment> localVarReturnType = new GenericType<StoragePolicyAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Assign storage policy
   * Creates a storage policy assignment for an enterprise or user.
   * @param body  (optional)
   * @return StoragePolicyAssignment
   * @throws ApiException if fails to make API call
   */
  public StoragePolicyAssignment postStoragePolicyAssignments(Body71 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/storage_policy_assignments";

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

    GenericType<StoragePolicyAssignment> localVarReturnType = new GenericType<StoragePolicyAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update storage policy assignment
   * Updates a specific storage policy assignment.
   * @param storagePolicyAssignmentId The ID of the storage policy assignment. (required)
   * @param body  (optional)
   * @return StoragePolicyAssignment
   * @throws ApiException if fails to make API call
   */
  public StoragePolicyAssignment putStoragePolicyAssignmentsId(String storagePolicyAssignmentId, Body72 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'storagePolicyAssignmentId' is set
    if (storagePolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'storagePolicyAssignmentId' when calling putStoragePolicyAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/storage_policy_assignments/{storage_policy_assignment_id}"
      .replaceAll("\\{" + "storage_policy_assignment_id" + "\\}", apiClient.escapeString(storagePolicyAssignmentId.toString()));

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

    GenericType<StoragePolicyAssignment> localVarReturnType = new GenericType<StoragePolicyAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
