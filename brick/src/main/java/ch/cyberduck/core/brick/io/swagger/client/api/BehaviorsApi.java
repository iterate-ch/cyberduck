package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.BehaviorEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.BehaviorsBody;
import ch.cyberduck.core.brick.io.swagger.client.model.BehaviorsIdBody;
import ch.cyberduck.core.brick.io.swagger.client.model.StatusEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.WebhookTestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")public class BehaviorsApi {
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
   * List Behaviors by path
   * List Behaviors by path
   * @param path Path to operate on. (required)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;behavior&#x60;. (optional)
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
  public List<BehaviorEntity> behaviorsListForPath(String path, String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq, String recursive, String behavior) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling behaviorsListForPath");
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
   * Delete Behavior
   * Delete Behavior
   * @param id Behavior ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteBehaviorsId(Integer id) throws ApiException {
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
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List Behaviors
   * List Behaviors
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;behavior&#x60;. (optional)
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
  public List<BehaviorEntity> getBehaviors(String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq, String behavior) throws ApiException {
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
   * Update Behavior
   * Update Behavior
   * @param id Behavior ID. (required)
   * @param body  (optional)
   * @return BehaviorEntity
   * @throws ApiException if fails to make API call
   */
  public BehaviorEntity patchBehaviorsId(Integer id, BehaviorsIdBody body) throws ApiException {
    Object localVarPostBody = body;
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



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<BehaviorEntity> localVarReturnType = new GenericType<BehaviorEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create Behavior
   * Create Behavior
   * @param body  (required)
   * @return BehaviorEntity
   * @throws ApiException if fails to make API call
   */
  public BehaviorEntity postBehaviors(BehaviorsBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postBehaviors");
    }
    // create path and map variables
    String localVarPath = "/behaviors";

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<BehaviorEntity> localVarReturnType = new GenericType<BehaviorEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Test webhook.
   * Test webhook.
   * @param body  (required)
   * @return StatusEntity
   * @throws ApiException if fails to make API call
   */
  public StatusEntity postBehaviorsWebhookTest(WebhookTestBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postBehaviorsWebhookTest");
    }
    // create path and map variables
    String localVarPath = "/behaviors/webhook/test";

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<StatusEntity> localVarReturnType = new GenericType<StatusEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
