package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ClickwrapEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class ClickwrapsApi {
  private ApiClient apiClient;

  public ClickwrapsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ClickwrapsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Clickwrap
   * Delete Clickwrap
   * @param id Clickwrap ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteClickwrapsId(Integer id) throws ApiException {

    deleteClickwrapsIdWithHttpInfo(id);
  }

  /**
   * Delete Clickwrap
   * Delete Clickwrap
   * @param id Clickwrap ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteClickwrapsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteClickwrapsId");
    }
    
    // create path and map variables
    String localVarPath = "/clickwraps/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List Clickwraps
   * List Clickwraps
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;ClickwrapEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ClickwrapEntity> getClickwraps(String cursor, Integer perPage) throws ApiException {
    return getClickwrapsWithHttpInfo(cursor, perPage).getData();
      }

  /**
   * List Clickwraps
   * List Clickwraps
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;ClickwrapEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<ClickwrapEntity>> getClickwrapsWithHttpInfo(String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/clickwraps";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<ClickwrapEntity>> localVarReturnType = new GenericType<List<ClickwrapEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Clickwrap
   * Show Clickwrap
   * @param id Clickwrap ID. (required)
   * @return ClickwrapEntity
   * @throws ApiException if fails to make API call
   */
  public ClickwrapEntity getClickwrapsId(Integer id) throws ApiException {
    return getClickwrapsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Clickwrap
   * Show Clickwrap
   * @param id Clickwrap ID. (required)
   * @return ApiResponse&lt;ClickwrapEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ClickwrapEntity> getClickwrapsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getClickwrapsId");
    }
    
    // create path and map variables
    String localVarPath = "/clickwraps/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<ClickwrapEntity> localVarReturnType = new GenericType<ClickwrapEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Clickwrap
   * Update Clickwrap
   * @param id Clickwrap ID. (required)
   * @param name Name of the Clickwrap agreement (used when selecting from multiple Clickwrap agreements.) (optional)
   * @param body Body text of Clickwrap (supports Markdown formatting). (optional)
   * @param useWithBundles Use this Clickwrap for Bundles? (optional)
   * @param useWithInboxes Use this Clickwrap for Inboxes? (optional)
   * @param useWithUsers Use this Clickwrap for User Registrations?  Note: This only applies to User Registrations where the User is invited to your Files.com site using an E-Mail invitation process where they then set their own password. (optional)
   * @return ClickwrapEntity
   * @throws ApiException if fails to make API call
   */
  public ClickwrapEntity patchClickwrapsId(Integer id, String name, String body, String useWithBundles, String useWithInboxes, String useWithUsers) throws ApiException {
    return patchClickwrapsIdWithHttpInfo(id, name, body, useWithBundles, useWithInboxes, useWithUsers).getData();
      }

  /**
   * Update Clickwrap
   * Update Clickwrap
   * @param id Clickwrap ID. (required)
   * @param name Name of the Clickwrap agreement (used when selecting from multiple Clickwrap agreements.) (optional)
   * @param body Body text of Clickwrap (supports Markdown formatting). (optional)
   * @param useWithBundles Use this Clickwrap for Bundles? (optional)
   * @param useWithInboxes Use this Clickwrap for Inboxes? (optional)
   * @param useWithUsers Use this Clickwrap for User Registrations?  Note: This only applies to User Registrations where the User is invited to your Files.com site using an E-Mail invitation process where they then set their own password. (optional)
   * @return ApiResponse&lt;ClickwrapEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ClickwrapEntity> patchClickwrapsIdWithHttpInfo(Integer id, String name, String body, String useWithBundles, String useWithInboxes, String useWithUsers) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchClickwrapsId");
    }
    
    // create path and map variables
    String localVarPath = "/clickwraps/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (name != null)
      localVarFormParams.put("name", name);
if (body != null)
      localVarFormParams.put("body", body);
if (useWithBundles != null)
      localVarFormParams.put("use_with_bundles", useWithBundles);
if (useWithInboxes != null)
      localVarFormParams.put("use_with_inboxes", useWithInboxes);
if (useWithUsers != null)
      localVarFormParams.put("use_with_users", useWithUsers);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ClickwrapEntity> localVarReturnType = new GenericType<ClickwrapEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Clickwrap
   * Create Clickwrap
   * @param name Name of the Clickwrap agreement (used when selecting from multiple Clickwrap agreements.) (optional)
   * @param body Body text of Clickwrap (supports Markdown formatting). (optional)
   * @param useWithBundles Use this Clickwrap for Bundles? (optional)
   * @param useWithInboxes Use this Clickwrap for Inboxes? (optional)
   * @param useWithUsers Use this Clickwrap for User Registrations?  Note: This only applies to User Registrations where the User is invited to your Files.com site using an E-Mail invitation process where they then set their own password. (optional)
   * @return ClickwrapEntity
   * @throws ApiException if fails to make API call
   */
  public ClickwrapEntity postClickwraps(String name, String body, String useWithBundles, String useWithInboxes, String useWithUsers) throws ApiException {
    return postClickwrapsWithHttpInfo(name, body, useWithBundles, useWithInboxes, useWithUsers).getData();
      }

  /**
   * Create Clickwrap
   * Create Clickwrap
   * @param name Name of the Clickwrap agreement (used when selecting from multiple Clickwrap agreements.) (optional)
   * @param body Body text of Clickwrap (supports Markdown formatting). (optional)
   * @param useWithBundles Use this Clickwrap for Bundles? (optional)
   * @param useWithInboxes Use this Clickwrap for Inboxes? (optional)
   * @param useWithUsers Use this Clickwrap for User Registrations?  Note: This only applies to User Registrations where the User is invited to your Files.com site using an E-Mail invitation process where they then set their own password. (optional)
   * @return ApiResponse&lt;ClickwrapEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ClickwrapEntity> postClickwrapsWithHttpInfo(String name, String body, String useWithBundles, String useWithInboxes, String useWithUsers) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/clickwraps";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (name != null)
      localVarFormParams.put("name", name);
if (body != null)
      localVarFormParams.put("body", body);
if (useWithBundles != null)
      localVarFormParams.put("use_with_bundles", useWithBundles);
if (useWithInboxes != null)
      localVarFormParams.put("use_with_inboxes", useWithInboxes);
if (useWithUsers != null)
      localVarFormParams.put("use_with_users", useWithUsers);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ClickwrapEntity> localVarReturnType = new GenericType<ClickwrapEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
