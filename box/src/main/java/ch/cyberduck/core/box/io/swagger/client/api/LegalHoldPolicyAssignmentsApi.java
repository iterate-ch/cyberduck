package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body64;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.FileVersionLegalHolds;
import ch.cyberduck.core.box.io.swagger.client.model.LegalHoldPolicyAssignment;
import ch.cyberduck.core.box.io.swagger.client.model.LegalHoldPolicyAssignments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class LegalHoldPolicyAssignmentsApi {
  private ApiClient apiClient;

  public LegalHoldPolicyAssignmentsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public LegalHoldPolicyAssignmentsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Unassign legal hold policy
   * Remove a legal hold from an item.  This is an asynchronous process. The policy will not be fully removed yet when the response returns.
   * @param legalHoldPolicyAssignmentId The ID of the legal hold policy assignment (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteLegalHoldPolicyAssignmentsId(String legalHoldPolicyAssignmentId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'legalHoldPolicyAssignmentId' is set
    if (legalHoldPolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'legalHoldPolicyAssignmentId' when calling deleteLegalHoldPolicyAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policy_assignments/{legal_hold_policy_assignment_id}"
      .replaceAll("\\{" + "legal_hold_policy_assignment_id" + "\\}", apiClient.escapeString(legalHoldPolicyAssignmentId.toString()));

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
   * List legal hold policy assignments
   * Retrieves a list of items a legal hold policy has been assigned to.
   * @param policyId The ID of the legal hold policy (required)
   * @param assignToType Filters the results by the type of item the policy was applied to. (optional)
   * @param assignToId Filters the results by the ID of item the policy was applied to. (optional)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return LegalHoldPolicyAssignments
   * @throws ApiException if fails to make API call
   */
  public LegalHoldPolicyAssignments getLegalHoldPolicyAssignments(String policyId, String assignToType, String assignToId, String marker, Long limit, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'policyId' is set
    if (policyId == null) {
      throw new ApiException(400, "Missing the required parameter 'policyId' when calling getLegalHoldPolicyAssignments");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policy_assignments";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "policy_id", policyId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "assign_to_type", assignToType));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "assign_to_id", assignToId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<LegalHoldPolicyAssignments> localVarReturnType = new GenericType<LegalHoldPolicyAssignments>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get legal hold policy assignment
   * Retrieve a legal hold policy assignment.
   * @param legalHoldPolicyAssignmentId The ID of the legal hold policy assignment (required)
   * @return LegalHoldPolicyAssignment
   * @throws ApiException if fails to make API call
   */
  public LegalHoldPolicyAssignment getLegalHoldPolicyAssignmentsId(String legalHoldPolicyAssignmentId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'legalHoldPolicyAssignmentId' is set
    if (legalHoldPolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'legalHoldPolicyAssignmentId' when calling getLegalHoldPolicyAssignmentsId");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policy_assignments/{legal_hold_policy_assignment_id}"
      .replaceAll("\\{" + "legal_hold_policy_assignment_id" + "\\}", apiClient.escapeString(legalHoldPolicyAssignmentId.toString()));

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

    GenericType<LegalHoldPolicyAssignment> localVarReturnType = new GenericType<LegalHoldPolicyAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List previous file versions for legal hold policy assignment
   * Get a list of previous file versions for a legal hold assignment.  In some cases you may only need the latest file versions instead. In these cases, use the &#x60;GET  /legal_hold_policy_assignments/:id/files_on_hold&#x60; API instead to return any current (latest) versions of a file for this legal hold policy assignment.  Due to ongoing re-architecture efforts this API might not return all files held for this policy ID. Instead, this API will only return past file versions held in the newly developed architecture. The &#x60;GET /file_version_legal_holds&#x60; API can be used to fetch current and past versions of files held within the legacy architecture.  The &#x60;GET /legal_hold_policy_assignments?policy_id&#x3D;{id}&#x60; API can be used to find a list of policy assignments for a given policy ID.
   * @param legalHoldPolicyAssignmentId The ID of the legal hold policy assignment (required)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return FileVersionLegalHolds
   * @throws ApiException if fails to make API call
   */
  public FileVersionLegalHolds getLegalHoldPolicyAssignmentsIdFileVersionsOnHold(String legalHoldPolicyAssignmentId, String marker, Long limit, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'legalHoldPolicyAssignmentId' is set
    if (legalHoldPolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'legalHoldPolicyAssignmentId' when calling getLegalHoldPolicyAssignmentsIdFileVersionsOnHold");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policy_assignments/{legal_hold_policy_assignment_id}/file_versions_on_hold"
      .replaceAll("\\{" + "legal_hold_policy_assignment_id" + "\\}", apiClient.escapeString(legalHoldPolicyAssignmentId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<FileVersionLegalHolds> localVarReturnType = new GenericType<FileVersionLegalHolds>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List current file versions for legal hold policy assignment
   * Get a list of current file versions for a legal hold assignment.  In some cases you may want to get previous file versions instead. In these cases, use the &#x60;GET  /legal_hold_policy_assignments/:id/file_versions_on_hold&#x60; API instead to return any previous versions of a file for this legal hold policy assignment.  Due to ongoing re-architecture efforts this API might not return all file versions held for this policy ID. Instead, this API will only return the latest file version held in the newly developed architecture. The &#x60;GET /file_version_legal_holds&#x60; API can be used to fetch current and past versions of files held within the legacy architecture.  The &#x60;GET /legal_hold_policy_assignments?policy_id&#x3D;{id}&#x60; API can be used to find a list of policy assignments for a given policy ID.
   * @param legalHoldPolicyAssignmentId The ID of the legal hold policy assignment (required)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return FileVersionLegalHolds
   * @throws ApiException if fails to make API call
   */
  public FileVersionLegalHolds getLegalHoldPolicyAssignmentsIdFilesOnHold(String legalHoldPolicyAssignmentId, String marker, Long limit, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'legalHoldPolicyAssignmentId' is set
    if (legalHoldPolicyAssignmentId == null) {
      throw new ApiException(400, "Missing the required parameter 'legalHoldPolicyAssignmentId' when calling getLegalHoldPolicyAssignmentsIdFilesOnHold");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policy_assignments/{legal_hold_policy_assignment_id}/files_on_hold"
      .replaceAll("\\{" + "legal_hold_policy_assignment_id" + "\\}", apiClient.escapeString(legalHoldPolicyAssignmentId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<FileVersionLegalHolds> localVarReturnType = new GenericType<FileVersionLegalHolds>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Assign legal hold policy
   * Assign a legal hold to a file, file version, folder, or user.
   * @param body  (optional)
   * @return LegalHoldPolicyAssignment
   * @throws ApiException if fails to make API call
   */
  public LegalHoldPolicyAssignment postLegalHoldPolicyAssignments(Body64 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/legal_hold_policy_assignments";

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

    GenericType<LegalHoldPolicyAssignment> localVarReturnType = new GenericType<LegalHoldPolicyAssignment>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
