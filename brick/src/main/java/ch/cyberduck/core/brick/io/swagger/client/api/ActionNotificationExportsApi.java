package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ActionNotificationExportEntity;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class ActionNotificationExportsApi {
  private ApiClient apiClient;

  public ActionNotificationExportsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ActionNotificationExportsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Show Action Notification Export
   * Show Action Notification Export
   * @param id Action Notification Export ID. (required)
   * @return ActionNotificationExportEntity
   * @throws ApiException if fails to make API call
   */
  public ActionNotificationExportEntity getActionNotificationExportsId(Integer id) throws ApiException {
    return getActionNotificationExportsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Action Notification Export
   * Show Action Notification Export
   * @param id Action Notification Export ID. (required)
   * @return ApiResponse&lt;ActionNotificationExportEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ActionNotificationExportEntity> getActionNotificationExportsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getActionNotificationExportsId");
    }
    
    // create path and map variables
    String localVarPath = "/action_notification_exports/{id}"
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

    GenericType<ActionNotificationExportEntity> localVarReturnType = new GenericType<ActionNotificationExportEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Action Notification Export
   * Create Action Notification Export
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param startAt Start date/time of export range. (optional)
   * @param endAt End date/time of export range. (optional)
   * @param queryMessage Error message associated with the request, if any. (optional)
   * @param queryRequestMethod The HTTP request method used by the webhook. (optional)
   * @param queryRequestUrl The target webhook URL. (optional)
   * @param queryStatus The HTTP status returned from the server in response to the webhook request. (optional)
   * @param querySuccess true if the webhook request succeeded (i.e. returned a 200 or 204 response status). false otherwise. (optional)
   * @param queryPath Return notifications that were triggered by actions on this specific path. (optional)
   * @param queryFolder Return notifications that were triggered by actions in this folder. (optional)
   * @return ActionNotificationExportEntity
   * @throws ApiException if fails to make API call
   */
  public ActionNotificationExportEntity postActionNotificationExports(Integer userId, DateTime startAt, DateTime endAt, String queryMessage, String queryRequestMethod, String queryRequestUrl, String queryStatus, Boolean querySuccess, String queryPath, String queryFolder) throws ApiException {
    return postActionNotificationExportsWithHttpInfo(userId, startAt, endAt, queryMessage, queryRequestMethod, queryRequestUrl, queryStatus, querySuccess, queryPath, queryFolder).getData();
      }

  /**
   * Create Action Notification Export
   * Create Action Notification Export
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param startAt Start date/time of export range. (optional)
   * @param endAt End date/time of export range. (optional)
   * @param queryMessage Error message associated with the request, if any. (optional)
   * @param queryRequestMethod The HTTP request method used by the webhook. (optional)
   * @param queryRequestUrl The target webhook URL. (optional)
   * @param queryStatus The HTTP status returned from the server in response to the webhook request. (optional)
   * @param querySuccess true if the webhook request succeeded (i.e. returned a 200 or 204 response status). false otherwise. (optional)
   * @param queryPath Return notifications that were triggered by actions on this specific path. (optional)
   * @param queryFolder Return notifications that were triggered by actions in this folder. (optional)
   * @return ApiResponse&lt;ActionNotificationExportEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ActionNotificationExportEntity> postActionNotificationExportsWithHttpInfo(Integer userId, DateTime startAt, DateTime endAt, String queryMessage, String queryRequestMethod, String queryRequestUrl, String queryStatus, Boolean querySuccess, String queryPath, String queryFolder) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/action_notification_exports";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (startAt != null)
      localVarFormParams.put("start_at", startAt);
if (endAt != null)
      localVarFormParams.put("end_at", endAt);
if (queryMessage != null)
      localVarFormParams.put("query_message", queryMessage);
if (queryRequestMethod != null)
      localVarFormParams.put("query_request_method", queryRequestMethod);
if (queryRequestUrl != null)
      localVarFormParams.put("query_request_url", queryRequestUrl);
if (queryStatus != null)
      localVarFormParams.put("query_status", queryStatus);
if (querySuccess != null)
      localVarFormParams.put("query_success", querySuccess);
if (queryPath != null)
      localVarFormParams.put("query_path", queryPath);
if (queryFolder != null)
      localVarFormParams.put("query_folder", queryFolder);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ActionNotificationExportEntity> localVarReturnType = new GenericType<ActionNotificationExportEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
