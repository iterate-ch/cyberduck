package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.BehaviorEntity;
import java.io.File;
import ch.cyberduck.core.brick.io.swagger.client.model.StatusEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class BehaviorsApi {
  private ApiClient apiClient;

  public BehaviorsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BehaviorsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Behavior
   * Delete Behavior
   * @param id Behavior ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteBehaviorsId(Integer id) throws ApiException {

    deleteBehaviorsIdWithHttpInfo(id);
  }

  /**
   * Delete Behavior
   * Delete Behavior
   * @param id Behavior ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteBehaviorsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteBehaviorsId");
    }
    
    // create path and map variables
    String localVarPath = "/behaviors/{id}"
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
   * List Behaviors
   * List Behaviors
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param behavior If set, only shows folder behaviors matching this behavior type. (optional)
   * @return List&lt;BehaviorEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<BehaviorEntity> getBehaviors(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String behavior) throws ApiException {
    return getBehaviorsWithHttpInfo(cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, behavior).getData();
      }

  /**
   * List Behaviors
   * List Behaviors
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param behavior If set, only shows folder behaviors matching this behavior type. (optional)
   * @return ApiResponse&lt;List&lt;BehaviorEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<BehaviorEntity>> getBehaviorsWithHttpInfo(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String behavior) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/behaviors";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "behavior", behavior));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<BehaviorEntity>> localVarReturnType = new GenericType<List<BehaviorEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Behavior
   * Show Behavior
   * @param id Behavior ID. (required)
   * @return BehaviorEntity
   * @throws ApiException if fails to make API call
   */
  public BehaviorEntity getBehaviorsId(Integer id) throws ApiException {
    return getBehaviorsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Behavior
   * Show Behavior
   * @param id Behavior ID. (required)
   * @return ApiResponse&lt;BehaviorEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BehaviorEntity> getBehaviorsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getBehaviorsId");
    }
    
    // create path and map variables
    String localVarPath = "/behaviors/{id}"
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

    GenericType<BehaviorEntity> localVarReturnType = new GenericType<BehaviorEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List Behaviors by path
   * List Behaviors by path
   * @param path Path to operate on. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param recursive Show behaviors above this path? (optional)
   * @param behavior DEPRECATED: If set only shows folder behaviors matching this behavior type. Use &#x60;filter[behavior]&#x60; instead. (optional)
   * @return List&lt;BehaviorEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<BehaviorEntity> listForPath(String path, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String recursive, String behavior) throws ApiException {
    return listForPathWithHttpInfo(path, cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, recursive, behavior).getData();
      }

  /**
   * List Behaviors by path
   * List Behaviors by path
   * @param path Path to operate on. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;behavior&#x60;. (optional)
   * @param recursive Show behaviors above this path? (optional)
   * @param behavior DEPRECATED: If set only shows folder behaviors matching this behavior type. Use &#x60;filter[behavior]&#x60; instead. (optional)
   * @return ApiResponse&lt;List&lt;BehaviorEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<BehaviorEntity>> listForPathWithHttpInfo(String path, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String recursive, String behavior) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling listForPath");
    }
    
    // create path and map variables
    String localVarPath = "/behaviors/folders/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "recursive", recursive));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "behavior", behavior));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<BehaviorEntity>> localVarReturnType = new GenericType<List<BehaviorEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Behavior
   * Update Behavior
   * @param id Behavior ID. (required)
   * @param value The value of the folder behavior.  Can be a integer, array, or hash depending on the type of folder behavior. (optional)
   * @param attachmentFile Certain behaviors may require a file, for instance, the \&quot;watermark\&quot; behavior requires a watermark image (optional)
   * @param behavior Behavior type. (optional)
   * @param path Folder behaviors path. (optional)
   * @return BehaviorEntity
   * @throws ApiException if fails to make API call
   */
  public BehaviorEntity patchBehaviorsId(Integer id, String value, File attachmentFile, String behavior, String path) throws ApiException {
    return patchBehaviorsIdWithHttpInfo(id, value, attachmentFile, behavior, path).getData();
      }

  /**
   * Update Behavior
   * Update Behavior
   * @param id Behavior ID. (required)
   * @param value The value of the folder behavior.  Can be a integer, array, or hash depending on the type of folder behavior. (optional)
   * @param attachmentFile Certain behaviors may require a file, for instance, the \&quot;watermark\&quot; behavior requires a watermark image (optional)
   * @param behavior Behavior type. (optional)
   * @param path Folder behaviors path. (optional)
   * @return ApiResponse&lt;BehaviorEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BehaviorEntity> patchBehaviorsIdWithHttpInfo(Integer id, String value, File attachmentFile, String behavior, String path) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchBehaviorsId");
    }
    
    // create path and map variables
    String localVarPath = "/behaviors/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (value != null)
      localVarFormParams.put("value", value);
if (attachmentFile != null)
      localVarFormParams.put("attachment_file", attachmentFile);
if (behavior != null)
      localVarFormParams.put("behavior", behavior);
if (path != null)
      localVarFormParams.put("path", path);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<BehaviorEntity> localVarReturnType = new GenericType<BehaviorEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Behavior
   * Create Behavior
   * @param path Folder behaviors path. (required)
   * @param behavior Behavior type. (required)
   * @param value The value of the folder behavior.  Can be a integer, array, or hash depending on the type of folder behavior. (optional)
   * @param attachmentFile Certain behaviors may require a file, for instance, the \&quot;watermark\&quot; behavior requires a watermark image (optional)
   * @return BehaviorEntity
   * @throws ApiException if fails to make API call
   */
  public BehaviorEntity postBehaviors(String path, String behavior, String value, File attachmentFile) throws ApiException {
    return postBehaviorsWithHttpInfo(path, behavior, value, attachmentFile).getData();
      }

  /**
   * Create Behavior
   * Create Behavior
   * @param path Folder behaviors path. (required)
   * @param behavior Behavior type. (required)
   * @param value The value of the folder behavior.  Can be a integer, array, or hash depending on the type of folder behavior. (optional)
   * @param attachmentFile Certain behaviors may require a file, for instance, the \&quot;watermark\&quot; behavior requires a watermark image (optional)
   * @return ApiResponse&lt;BehaviorEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BehaviorEntity> postBehaviorsWithHttpInfo(String path, String behavior, String value, File attachmentFile) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling postBehaviors");
    }
    
    // verify the required parameter 'behavior' is set
    if (behavior == null) {
      throw new ApiException(400, "Missing the required parameter 'behavior' when calling postBehaviors");
    }
    
    // create path and map variables
    String localVarPath = "/behaviors";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (value != null)
      localVarFormParams.put("value", value);
if (attachmentFile != null)
      localVarFormParams.put("attachment_file", attachmentFile);
if (path != null)
      localVarFormParams.put("path", path);
if (behavior != null)
      localVarFormParams.put("behavior", behavior);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<BehaviorEntity> localVarReturnType = new GenericType<BehaviorEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Test webhook.
   * Test webhook.
   * @param url URL for testing the webhook. (required)
   * @param method HTTP method(GET or POST). (optional)
   * @param encoding HTTP encoding method.  Can be JSON, XML, or RAW (form data). (optional)
   * @param headers Additional request headers. (optional)
   * @param body Additional body parameters. (optional)
   * @param action action for test body (optional)
   * @return StatusEntity
   * @throws ApiException if fails to make API call
   */
  public StatusEntity postBehaviorsWebhookTest(String url, String method, String encoding, Map<String, String> headers, Map<String, String> body, String action) throws ApiException {
    return postBehaviorsWebhookTestWithHttpInfo(url, method, encoding, headers, body, action).getData();
      }

  /**
   * Test webhook.
   * Test webhook.
   * @param url URL for testing the webhook. (required)
   * @param method HTTP method(GET or POST). (optional)
   * @param encoding HTTP encoding method.  Can be JSON, XML, or RAW (form data). (optional)
   * @param headers Additional request headers. (optional)
   * @param body Additional body parameters. (optional)
   * @param action action for test body (optional)
   * @return ApiResponse&lt;StatusEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<StatusEntity> postBehaviorsWebhookTestWithHttpInfo(String url, String method, String encoding, Map<String, String> headers, Map<String, String> body, String action) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'url' is set
    if (url == null) {
      throw new ApiException(400, "Missing the required parameter 'url' when calling postBehaviorsWebhookTest");
    }
    
    // create path and map variables
    String localVarPath = "/behaviors/webhook/test";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (url != null)
      localVarFormParams.put("url", url);
if (method != null)
      localVarFormParams.put("method", method);
if (encoding != null)
      localVarFormParams.put("encoding", encoding);
if (headers != null)
      localVarFormParams.put("headers", headers);
if (body != null)
      localVarFormParams.put("body", body);
if (action != null)
      localVarFormParams.put("action", action);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<StatusEntity> localVarReturnType = new GenericType<StatusEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
