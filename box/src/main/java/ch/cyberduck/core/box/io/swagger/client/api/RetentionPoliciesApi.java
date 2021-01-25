package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body59;
import ch.cyberduck.core.box.io.swagger.client.model.Body60;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.RetentionPolicies;
import ch.cyberduck.core.box.io.swagger.client.model.RetentionPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class RetentionPoliciesApi {
  private ApiClient apiClient;

  public RetentionPoliciesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RetentionPoliciesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List retention policies
   * Retrieves all of the retention policies for an enterprise.
   * @param policyName Filters results by a case sensitive prefix of the name of retention policies. (optional)
   * @param policyType Filters results by the type of retention policy. (optional)
   * @param createdByUserId Filters results by the ID of the user who created policy. (optional)
   * @return RetentionPolicies
   * @throws ApiException if fails to make API call
   */
  public RetentionPolicies getRetentionPolicies(String policyName, String policyType, String createdByUserId) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/retention_policies";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "policy_name", policyName));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "policy_type", policyType));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "created_by_user_id", createdByUserId));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<RetentionPolicies> localVarReturnType = new GenericType<RetentionPolicies>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get retention policy
   * Retrieves a retention policy.
   * @param retentionPolicyId The ID of the retention policy. (required)
   * @return RetentionPolicy
   * @throws ApiException if fails to make API call
   */
  public RetentionPolicy getRetentionPoliciesId(String retentionPolicyId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'retentionPolicyId' is set
    if (retentionPolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'retentionPolicyId' when calling getRetentionPoliciesId");
    }
    // create path and map variables
    String localVarPath = "/retention_policies/{retention_policy_id}"
      .replaceAll("\\{" + "retention_policy_id" + "\\}", apiClient.escapeString(retentionPolicyId.toString()));

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

    GenericType<RetentionPolicy> localVarReturnType = new GenericType<RetentionPolicy>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create retention policy
   * Creates a retention policy.
   * @param body  (optional)
   * @return RetentionPolicy
   * @throws ApiException if fails to make API call
   */
  public RetentionPolicy postRetentionPolicies(Body59 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/retention_policies";

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

    GenericType<RetentionPolicy> localVarReturnType = new GenericType<RetentionPolicy>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update retention policy
   * Updates a retention policy.
   * @param retentionPolicyId The ID of the retention policy. (required)
   * @param body  (optional)
   * @return RetentionPolicy
   * @throws ApiException if fails to make API call
   */
  public RetentionPolicy putRetentionPoliciesId(String retentionPolicyId, Body60 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'retentionPolicyId' is set
    if (retentionPolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'retentionPolicyId' when calling putRetentionPoliciesId");
    }
    // create path and map variables
    String localVarPath = "/retention_policies/{retention_policy_id}"
      .replaceAll("\\{" + "retention_policy_id" + "\\}", apiClient.escapeString(retentionPolicyId.toString()));

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

    GenericType<RetentionPolicy> localVarReturnType = new GenericType<RetentionPolicy>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
