package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body61;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.RetentionPolicyAssignment;
import ch.cyberduck.core.box.io.swagger.client.model.RetentionPolicyAssignments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class RetentionPolicyAssignmentsApi {
  private ApiClient apiClient;

  public RetentionPolicyAssignmentsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RetentionPolicyAssignmentsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List retention policy assignments
   * Returns a list of all retention policy assignments associated with a specified retention policy.
   * @param retentionPolicyId The ID of the retention policy. (required)
   * @param type The type of the retention policy assignment to retrieve. (optional)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @return RetentionPolicyAssignments
   * @throws ApiException if fails to make API call
   */
  public RetentionPolicyAssignments getRetentionPoliciesIdAssignments(String retentionPolicyId, String type, String marker, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'retentionPolicyId' is set
    if (retentionPolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'retentionPolicyId' when calling getRetentionPoliciesIdAssignments");
    }
    // create path and map variables
    String localVarPath = "/retention_policies/{retention_policy_id}/assignments"
      .replaceAll("\\{" + "retention_policy_id" + "\\}", apiClient.escapeString(retentionPolicyId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
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

    GenericType<RetentionPolicyAssignments> localVarReturnType = new GenericType<RetentionPolicyAssignments>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get retention policy assignment
   * Retrieves a retention policy assignment
   * @param retentionPolicyAssignmentId The ID of the retention policy assignment. (required)
   * @return RetentionPolicyAssignment
   * @throws ApiException if fails to make API call
   */
  public RetentionPolicyAssignment getRetentionPolicyAssignmentsId(String retentionPolicyAssignmentId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'retentionPolicyAssignmentId' is set
    if (retentionPolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'retentionPolicyAssignmentId' when calling getRetentionPolicyAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/retention_policy_assignments/{retention_policy_assignment_id}"
      .replaceAll("\\{" + "retention_policy_assignment_id" + "\\}", apiClient.escapeString(retentionPolicyAssignmentId.toString()));

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

    GenericType<RetentionPolicyAssignment> localVarReturnType = new GenericType<RetentionPolicyAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Assign retention policy
   * Assigns a retention policy to an item.
   * @param body  (optional)
   * @return RetentionPolicyAssignment
   * @throws ApiException if fails to make API call
   */
  public RetentionPolicyAssignment postRetentionPolicyAssignments(Body61 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/retention_policy_assignments";

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

    GenericType<RetentionPolicyAssignment> localVarReturnType = new GenericType<RetentionPolicyAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
