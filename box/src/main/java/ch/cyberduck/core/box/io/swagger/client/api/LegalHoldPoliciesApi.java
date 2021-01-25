package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body62;
import ch.cyberduck.core.box.io.swagger.client.model.Body63;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.LegalHoldPolicies;
import ch.cyberduck.core.box.io.swagger.client.model.LegalHoldPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class LegalHoldPoliciesApi {
  private ApiClient apiClient;

  public LegalHoldPoliciesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public LegalHoldPoliciesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove legal hold policy
   * Delete an existing legal hold policy.  This is an asynchronous process. The policy will not be fully deleted yet when the response returns.
   * @param legalHoldPolicyId The ID of the legal hold policy (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteLegalHoldPoliciesId(String legalHoldPolicyId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'legalHoldPolicyId' is set
    if (legalHoldPolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'legalHoldPolicyId' when calling deleteLegalHoldPoliciesId");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policies/{legal_hold_policy_id}"
      .replaceAll("\\{" + "legal_hold_policy_id" + "\\}", apiClient.escapeString(legalHoldPolicyId.toString()));

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
   * List all legal hold policies
   * Retrieves a list of legal hold policies that belong to an enterprise.
   * @param policyName Limits results to policies for which the names start with this search term. This is a case-insensitive prefix. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @return LegalHoldPolicies
   * @throws ApiException if fails to make API call
   */
  public LegalHoldPolicies getLegalHoldPolicies(String policyName, List<String> fields, String marker, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/legal_hold_policies";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "policy_name", policyName));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));
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

    GenericType<LegalHoldPolicies> localVarReturnType = new GenericType<LegalHoldPolicies>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get legal hold policy
   * Retrieve a legal hold policy.
   * @param legalHoldPolicyId The ID of the legal hold policy (required)
   * @return LegalHoldPolicy
   * @throws ApiException if fails to make API call
   */
  public LegalHoldPolicy getLegalHoldPoliciesId(String legalHoldPolicyId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'legalHoldPolicyId' is set
    if (legalHoldPolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'legalHoldPolicyId' when calling getLegalHoldPoliciesId");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policies/{legal_hold_policy_id}"
      .replaceAll("\\{" + "legal_hold_policy_id" + "\\}", apiClient.escapeString(legalHoldPolicyId.toString()));

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

    GenericType<LegalHoldPolicy> localVarReturnType = new GenericType<LegalHoldPolicy>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create legal hold policy
   * Create a new legal hold policy.
   * @param body  (optional)
   * @return LegalHoldPolicy
   * @throws ApiException if fails to make API call
   */
  public LegalHoldPolicy postLegalHoldPolicies(Body62 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/legal_hold_policies";

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

    GenericType<LegalHoldPolicy> localVarReturnType = new GenericType<LegalHoldPolicy>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update legal hold policy
   * Update legal hold policy.
   * @param legalHoldPolicyId The ID of the legal hold policy (required)
   * @param body  (optional)
   * @return LegalHoldPolicy
   * @throws ApiException if fails to make API call
   */
  public LegalHoldPolicy putLegalHoldPoliciesId(String legalHoldPolicyId, Body63 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'legalHoldPolicyId' is set
    if (legalHoldPolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'legalHoldPolicyId' when calling putLegalHoldPoliciesId");
    }
    // create path and map variables
    String localVarPath = "/legal_hold_policies/{legal_hold_policy_id}"
      .replaceAll("\\{" + "legal_hold_policy_id" + "\\}", apiClient.escapeString(legalHoldPolicyId.toString()));

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

    GenericType<LegalHoldPolicy> localVarReturnType = new GenericType<LegalHoldPolicy>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
