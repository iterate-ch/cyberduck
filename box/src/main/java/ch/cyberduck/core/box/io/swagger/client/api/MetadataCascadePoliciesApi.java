package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body28;
import ch.cyberduck.core.box.io.swagger.client.model.Body29;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.ConflictError;
import ch.cyberduck.core.box.io.swagger.client.model.MetadataCascadePolicies;
import ch.cyberduck.core.box.io.swagger.client.model.MetadataCascadePolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class MetadataCascadePoliciesApi {
  private ApiClient apiClient;

  public MetadataCascadePoliciesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MetadataCascadePoliciesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove metadata cascade policy
   * Deletes a metadata cascade policy.
   * @param metadataCascadePolicyId The ID of the metadata cascade policy. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteMetadataCascadePoliciesId(String metadataCascadePolicyId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'metadataCascadePolicyId' is set
    if (metadataCascadePolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'metadataCascadePolicyId' when calling deleteMetadataCascadePoliciesId");
    }
    // create path and map variables
    String localVarPath = "/metadata_cascade_policies/{metadata_cascade_policy_id}"
      .replaceAll("\\{" + "metadata_cascade_policy_id" + "\\}", apiClient.escapeString(metadataCascadePolicyId.toString()));

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
   * List metadata cascade policies
   * Retrieves a list of all the metadata cascade policies that are applied to a given folder. This can not be used on the root folder with ID &#x60;0&#x60;.
   * @param folderId Specifies which folder to return policies for. This can not be used on the root folder with ID &#x60;0&#x60;. (required)
   * @param ownerEnterpriseId The ID of the enterprise ID for which to find metadata cascade policies. If not specified, it defaults to the current enterprise. (optional)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param offset The offset of the item at which to begin the response. (optional, default to 0)
   * @return MetadataCascadePolicies
   * @throws ApiException if fails to make API call
   */
  public MetadataCascadePolicies getMetadataCascadePolicies(String folderId, String ownerEnterpriseId, String marker, Long offset) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling getMetadataCascadePolicies");
    }
    // create path and map variables
    String localVarPath = "/metadata_cascade_policies";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "folder_id", folderId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "owner_enterprise_id", ownerEnterpriseId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<MetadataCascadePolicies> localVarReturnType = new GenericType<MetadataCascadePolicies>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get metadata cascade policy
   * Retrieve a specific metadata cascade policy assigned to a folder.
   * @param metadataCascadePolicyId The ID of the metadata cascade policy. (required)
   * @return MetadataCascadePolicy
   * @throws ApiException if fails to make API call
   */
  public MetadataCascadePolicy getMetadataCascadePoliciesId(String metadataCascadePolicyId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'metadataCascadePolicyId' is set
    if (metadataCascadePolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'metadataCascadePolicyId' when calling getMetadataCascadePoliciesId");
    }
    // create path and map variables
    String localVarPath = "/metadata_cascade_policies/{metadata_cascade_policy_id}"
      .replaceAll("\\{" + "metadata_cascade_policy_id" + "\\}", apiClient.escapeString(metadataCascadePolicyId.toString()));

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

    GenericType<MetadataCascadePolicy> localVarReturnType = new GenericType<MetadataCascadePolicy>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create metadata cascade policy
   * Creates a new metadata cascade policy that applies a given metadata template to a given folder and automatically cascades it down to any files within that folder.  In order for the policy to be applied a metadata instance must first be applied to the folder the policy is to be applied to.
   * @param body  (optional)
   * @return MetadataCascadePolicy
   * @throws ApiException if fails to make API call
   */
  public MetadataCascadePolicy postMetadataCascadePolicies(Body28 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/metadata_cascade_policies";

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

    GenericType<MetadataCascadePolicy> localVarReturnType = new GenericType<MetadataCascadePolicy>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Force-apply metadata cascade policy to folder
   * Force the metadata on a folder with a metadata cascade policy to be applied to all of its children. This can be used after creating a new cascade policy to enforce the metadata to be cascaded down to all existing files within that folder.
   * @param metadataCascadePolicyId The ID of the cascade policy to force-apply. (required)
   * @param body  (optional)
   * @throws ApiException if fails to make API call
   */
  public void postMetadataCascadePoliciesIdApply(String metadataCascadePolicyId, Body29 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'metadataCascadePolicyId' is set
    if (metadataCascadePolicyId == null) {
      throw new ApiException(400, "Missing the required parameter 'metadataCascadePolicyId' when calling postMetadataCascadePoliciesIdApply");
    }
    // create path and map variables
    String localVarPath = "/metadata_cascade_policies/{metadata_cascade_policy_id}/apply"
      .replaceAll("\\{" + "metadata_cascade_policy_id" + "\\}", apiClient.escapeString(metadataCascadePolicyId.toString()));

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

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
