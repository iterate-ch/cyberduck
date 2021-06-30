package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ExternalEventEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class ExternalEventsApi {
  private ApiClient apiClient;

  public ExternalEventsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ExternalEventsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List External Events
   * List External Events
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;remote_server_type&#x60;, &#x60;event_type&#x60;, &#x60;created_at&#x60; or &#x60;status&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @return List&lt;ExternalEventEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ExternalEventEntity> getExternalEvents(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    return getExternalEventsWithHttpInfo(cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq).getData();
      }

  /**
   * List External Events
   * List External Events
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;remote_server_type&#x60;, &#x60;event_type&#x60;, &#x60;created_at&#x60; or &#x60;status&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;created_at&#x60;, &#x60;event_type&#x60;, &#x60;remote_server_type&#x60; or &#x60;status&#x60;. (optional)
   * @return ApiResponse&lt;List&lt;ExternalEventEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<ExternalEventEntity>> getExternalEventsWithHttpInfo(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/external_events";

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

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<ExternalEventEntity>> localVarReturnType = new GenericType<List<ExternalEventEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show External Event
   * Show External Event
   * @param id External Event ID. (required)
   * @return ExternalEventEntity
   * @throws ApiException if fails to make API call
   */
  public ExternalEventEntity getExternalEventsId(Integer id) throws ApiException {
    return getExternalEventsIdWithHttpInfo(id).getData();
      }

  /**
   * Show External Event
   * Show External Event
   * @param id External Event ID. (required)
   * @return ApiResponse&lt;ExternalEventEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExternalEventEntity> getExternalEventsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getExternalEventsId");
    }
    
    // create path and map variables
    String localVarPath = "/external_events/{id}"
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

    GenericType<ExternalEventEntity> localVarReturnType = new GenericType<ExternalEventEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create External Event
   * Create External Event
   * @param status Status of event. (required)
   * @param body Event body (required)
   * @return ExternalEventEntity
   * @throws ApiException if fails to make API call
   */
  public ExternalEventEntity postExternalEvents(String status, String body) throws ApiException {
    return postExternalEventsWithHttpInfo(status, body).getData();
      }

  /**
   * Create External Event
   * Create External Event
   * @param status Status of event. (required)
   * @param body Event body (required)
   * @return ApiResponse&lt;ExternalEventEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ExternalEventEntity> postExternalEventsWithHttpInfo(String status, String body) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'status' is set
    if (status == null) {
      throw new ApiException(400, "Missing the required parameter 'status' when calling postExternalEvents");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postExternalEvents");
    }
    
    // create path and map variables
    String localVarPath = "/external_events";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (status != null)
      localVarFormParams.put("status", status);
if (body != null)
      localVarFormParams.put("body", body);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ExternalEventEntity> localVarReturnType = new GenericType<ExternalEventEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
