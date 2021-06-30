package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.BundleEntity;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class BundlesApi {
  private ApiClient apiClient;

  public BundlesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BundlesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Bundle
   * Delete Bundle
   * @param id Bundle ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteBundlesId(Integer id) throws ApiException {

    deleteBundlesIdWithHttpInfo(id);
  }

  /**
   * Delete Bundle
   * Delete Bundle
   * @param id Bundle ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteBundlesIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteBundlesId");
    }
    
    // create path and map variables
    String localVarPath = "/bundles/{id}"
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
   * List Bundles
   * List Bundles
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;created_at&#x60; and &#x60;code&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @return List&lt;BundleEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<BundleEntity> getBundles(Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    return getBundlesWithHttpInfo(userId, cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq).getData();
      }

  /**
   * List Bundles
   * List Bundles
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;created_at&#x60; and &#x60;code&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;. (optional)
   * @return ApiResponse&lt;List&lt;BundleEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<BundleEntity>> getBundlesWithHttpInfo(Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/bundles";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<BundleEntity>> localVarReturnType = new GenericType<List<BundleEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Bundle
   * Show Bundle
   * @param id Bundle ID. (required)
   * @return BundleEntity
   * @throws ApiException if fails to make API call
   */
  public BundleEntity getBundlesId(Integer id) throws ApiException {
    return getBundlesIdWithHttpInfo(id).getData();
      }

  /**
   * Show Bundle
   * Show Bundle
   * @param id Bundle ID. (required)
   * @return ApiResponse&lt;BundleEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BundleEntity> getBundlesIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getBundlesId");
    }
    
    // create path and map variables
    String localVarPath = "/bundles/{id}"
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

    GenericType<BundleEntity> localVarReturnType = new GenericType<BundleEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Bundle
   * Update Bundle
   * @param id Bundle ID. (required)
   * @param paths A list of paths to include in this bundle. (optional)
   * @param password Password for this bundle. (optional)
   * @param formFieldSetId Id of Form Field Set to use with this bundle (optional)
   * @param clickwrapId ID of the clickwrap to use with this bundle. (optional)
   * @param code Bundle code.  This code forms the end part of the Public URL. (optional)
   * @param description Public description (optional)
   * @param expiresAt Bundle expiration date/time (optional)
   * @param inboxId ID of the associated inbox, if available. (optional)
   * @param maxUses Maximum number of times bundle can be accessed (optional)
   * @param note Bundle internal note (optional)
   * @param previewOnly Restrict users to previewing files only? (optional)
   * @param requireRegistration Show a registration page that captures the downloader&#39;s name and email address? (optional)
   * @param requireShareRecipient Only allow access to recipients who have explicitly received the share via an email sent through the Files.com UI? (optional)
   * @return BundleEntity
   * @throws ApiException if fails to make API call
   */
  public BundleEntity patchBundlesId(Integer id, List<String> paths, String password, Integer formFieldSetId, Integer clickwrapId, String code, String description, DateTime expiresAt, Integer inboxId, Integer maxUses, String note, Boolean previewOnly, Boolean requireRegistration, Boolean requireShareRecipient) throws ApiException {
    return patchBundlesIdWithHttpInfo(id, paths, password, formFieldSetId, clickwrapId, code, description, expiresAt, inboxId, maxUses, note, previewOnly, requireRegistration, requireShareRecipient).getData();
      }

  /**
   * Update Bundle
   * Update Bundle
   * @param id Bundle ID. (required)
   * @param paths A list of paths to include in this bundle. (optional)
   * @param password Password for this bundle. (optional)
   * @param formFieldSetId Id of Form Field Set to use with this bundle (optional)
   * @param clickwrapId ID of the clickwrap to use with this bundle. (optional)
   * @param code Bundle code.  This code forms the end part of the Public URL. (optional)
   * @param description Public description (optional)
   * @param expiresAt Bundle expiration date/time (optional)
   * @param inboxId ID of the associated inbox, if available. (optional)
   * @param maxUses Maximum number of times bundle can be accessed (optional)
   * @param note Bundle internal note (optional)
   * @param previewOnly Restrict users to previewing files only? (optional)
   * @param requireRegistration Show a registration page that captures the downloader&#39;s name and email address? (optional)
   * @param requireShareRecipient Only allow access to recipients who have explicitly received the share via an email sent through the Files.com UI? (optional)
   * @return ApiResponse&lt;BundleEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BundleEntity> patchBundlesIdWithHttpInfo(Integer id, List<String> paths, String password, Integer formFieldSetId, Integer clickwrapId, String code, String description, DateTime expiresAt, Integer inboxId, Integer maxUses, String note, Boolean previewOnly, Boolean requireRegistration, Boolean requireShareRecipient) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchBundlesId");
    }
    
    // create path and map variables
    String localVarPath = "/bundles/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (paths != null)
      localVarFormParams.put("paths", paths);
if (password != null)
      localVarFormParams.put("password", password);
if (formFieldSetId != null)
      localVarFormParams.put("form_field_set_id", formFieldSetId);
if (clickwrapId != null)
      localVarFormParams.put("clickwrap_id", clickwrapId);
if (code != null)
      localVarFormParams.put("code", code);
if (description != null)
      localVarFormParams.put("description", description);
if (expiresAt != null)
      localVarFormParams.put("expires_at", expiresAt);
if (inboxId != null)
      localVarFormParams.put("inbox_id", inboxId);
if (maxUses != null)
      localVarFormParams.put("max_uses", maxUses);
if (note != null)
      localVarFormParams.put("note", note);
if (previewOnly != null)
      localVarFormParams.put("preview_only", previewOnly);
if (requireRegistration != null)
      localVarFormParams.put("require_registration", requireRegistration);
if (requireShareRecipient != null)
      localVarFormParams.put("require_share_recipient", requireShareRecipient);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<BundleEntity> localVarReturnType = new GenericType<BundleEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Bundle
   * Create Bundle
   * @param paths A list of paths to include in this bundle. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param password Password for this bundle. (optional)
   * @param formFieldSetId Id of Form Field Set to use with this bundle (optional)
   * @param expiresAt Bundle expiration date/time (optional)
   * @param maxUses Maximum number of times bundle can be accessed (optional)
   * @param description Public description (optional)
   * @param note Bundle internal note (optional)
   * @param code Bundle code.  This code forms the end part of the Public URL. (optional)
   * @param previewOnly Restrict users to previewing files only? (optional)
   * @param requireRegistration Show a registration page that captures the downloader&#39;s name and email address? (optional)
   * @param clickwrapId ID of the clickwrap to use with this bundle. (optional)
   * @param inboxId ID of the associated inbox, if available. (optional)
   * @param requireShareRecipient Only allow access to recipients who have explicitly received the share via an email sent through the Files.com UI? (optional)
   * @return BundleEntity
   * @throws ApiException if fails to make API call
   */
  public BundleEntity postBundles(List<String> paths, Integer userId, String password, Integer formFieldSetId, DateTime expiresAt, Integer maxUses, String description, String note, String code, Boolean previewOnly, Boolean requireRegistration, Integer clickwrapId, Integer inboxId, Boolean requireShareRecipient) throws ApiException {
    return postBundlesWithHttpInfo(paths, userId, password, formFieldSetId, expiresAt, maxUses, description, note, code, previewOnly, requireRegistration, clickwrapId, inboxId, requireShareRecipient).getData();
      }

  /**
   * Create Bundle
   * Create Bundle
   * @param paths A list of paths to include in this bundle. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param password Password for this bundle. (optional)
   * @param formFieldSetId Id of Form Field Set to use with this bundle (optional)
   * @param expiresAt Bundle expiration date/time (optional)
   * @param maxUses Maximum number of times bundle can be accessed (optional)
   * @param description Public description (optional)
   * @param note Bundle internal note (optional)
   * @param code Bundle code.  This code forms the end part of the Public URL. (optional)
   * @param previewOnly Restrict users to previewing files only? (optional)
   * @param requireRegistration Show a registration page that captures the downloader&#39;s name and email address? (optional)
   * @param clickwrapId ID of the clickwrap to use with this bundle. (optional)
   * @param inboxId ID of the associated inbox, if available. (optional)
   * @param requireShareRecipient Only allow access to recipients who have explicitly received the share via an email sent through the Files.com UI? (optional)
   * @return ApiResponse&lt;BundleEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BundleEntity> postBundlesWithHttpInfo(List<String> paths, Integer userId, String password, Integer formFieldSetId, DateTime expiresAt, Integer maxUses, String description, String note, String code, Boolean previewOnly, Boolean requireRegistration, Integer clickwrapId, Integer inboxId, Boolean requireShareRecipient) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'paths' is set
    if (paths == null) {
      throw new ApiException(400, "Missing the required parameter 'paths' when calling postBundles");
    }
    
    // create path and map variables
    String localVarPath = "/bundles";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (paths != null)
      localVarFormParams.put("paths", paths);
if (password != null)
      localVarFormParams.put("password", password);
if (formFieldSetId != null)
      localVarFormParams.put("form_field_set_id", formFieldSetId);
if (expiresAt != null)
      localVarFormParams.put("expires_at", expiresAt);
if (maxUses != null)
      localVarFormParams.put("max_uses", maxUses);
if (description != null)
      localVarFormParams.put("description", description);
if (note != null)
      localVarFormParams.put("note", note);
if (code != null)
      localVarFormParams.put("code", code);
if (previewOnly != null)
      localVarFormParams.put("preview_only", previewOnly);
if (requireRegistration != null)
      localVarFormParams.put("require_registration", requireRegistration);
if (clickwrapId != null)
      localVarFormParams.put("clickwrap_id", clickwrapId);
if (inboxId != null)
      localVarFormParams.put("inbox_id", inboxId);
if (requireShareRecipient != null)
      localVarFormParams.put("require_share_recipient", requireShareRecipient);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<BundleEntity> localVarReturnType = new GenericType<BundleEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Send email(s) with a link to bundle
   * Send email(s) with a link to bundle
   * @param id Bundle ID. (required)
   * @param to A list of email addresses to share this bundle with. Required unless &#x60;recipients&#x60; is used. (optional)
   * @param note Note to include in email. (optional)
   * @param recipients A list of recipients to share this bundle with. Required unless &#x60;to&#x60; is used. (optional)
   * @throws ApiException if fails to make API call
   */
  public void postBundlesIdShare(Integer id, List<String> to, String note, List<Object> recipients) throws ApiException {

    postBundlesIdShareWithHttpInfo(id, to, note, recipients);
  }

  /**
   * Send email(s) with a link to bundle
   * Send email(s) with a link to bundle
   * @param id Bundle ID. (required)
   * @param to A list of email addresses to share this bundle with. Required unless &#x60;recipients&#x60; is used. (optional)
   * @param note Note to include in email. (optional)
   * @param recipients A list of recipients to share this bundle with. Required unless &#x60;to&#x60; is used. (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> postBundlesIdShareWithHttpInfo(Integer id, List<String> to, String note, List<Object> recipients) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling postBundlesIdShare");
    }
    
    // create path and map variables
    String localVarPath = "/bundles/{id}/share"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (to != null)
      localVarFormParams.put("to", to);
if (note != null)
      localVarFormParams.put("note", note);
if (recipients != null)
      localVarFormParams.put("recipients", recipients);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
