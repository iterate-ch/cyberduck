package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.NotificationEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.NotificationsBody;
import ch.cyberduck.core.brick.io.swagger.client.model.NotificationsIdBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")public class NotificationsApi {
  private ApiClient apiClient;

  public NotificationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public NotificationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Notification
   * Delete Notification
   * @param id Notification ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteNotificationsId(Integer id) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteNotificationsId");
    }
    // create path and map variables
    String localVarPath = "/notifications/{id}"
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
   * List Notifications
   * List Notifications
   * @param userId DEPRECATED: Show notifications for this User ID. Use &#x60;filter[user_id]&#x60; instead. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;path&#x60;, &#x60;user_id&#x60; or &#x60;group_id&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;user_id&#x60;, &#x60;group_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;user_id&#x60;, &#x60;group_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;user_id&#x60;, &#x60;group_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;user_id&#x60;, &#x60;group_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;user_id&#x60;, &#x60;group_id&#x60; or &#x60;path&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;user_id&#x60;, &#x60;group_id&#x60; or &#x60;path&#x60;. (optional)
   * @param groupId DEPRECATED: Show notifications for this Group ID. Use &#x60;filter[group_id]&#x60; instead. (optional)
   * @param path Show notifications for this Path. (optional)
   * @param includeAncestors If &#x60;include_ancestors&#x60; is &#x60;true&#x60; and &#x60;path&#x60; is specified, include notifications for any parent paths. Ignored if &#x60;path&#x60; is not specified. (optional)
   * @return List&lt;NotificationEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<NotificationEntity> getNotifications(Integer userId, String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq, Integer groupId, String path, Boolean includeAncestors) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/notifications";

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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "group_id", groupId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "path", path));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_ancestors", includeAncestors));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<NotificationEntity>> localVarReturnType = new GenericType<List<NotificationEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Show Notification
   * Show Notification
   * @param id Notification ID. (required)
   * @return NotificationEntity
   * @throws ApiException if fails to make API call
   */
  public NotificationEntity getNotificationsId(Integer id) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getNotificationsId");
    }
    // create path and map variables
    String localVarPath = "/notifications/{id}"
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

    GenericType<NotificationEntity> localVarReturnType = new GenericType<NotificationEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update Notification
   * Update Notification
   * @param id Notification ID. (required)
   * @param body  (optional)
   * @return NotificationEntity
   * @throws ApiException if fails to make API call
   */
  public NotificationEntity patchNotificationsId(Integer id, NotificationsIdBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchNotificationsId");
    }
    // create path and map variables
    String localVarPath = "/notifications/{id}"
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

    GenericType<NotificationEntity> localVarReturnType = new GenericType<NotificationEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create Notification
   * Create Notification
   * @param body  (optional)
   * @return NotificationEntity
   * @throws ApiException if fails to make API call
   */
  public NotificationEntity postNotifications(NotificationsBody body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/notifications";

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

    GenericType<NotificationEntity> localVarReturnType = new GenericType<NotificationEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
