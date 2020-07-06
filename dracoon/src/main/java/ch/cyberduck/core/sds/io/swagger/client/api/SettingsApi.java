package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.CreateWebhookRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerSettingsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerSettingsResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.EventTypeList;
import ch.cyberduck.core.sds.io.swagger.client.model.NotificationChannelActivationRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.NotificationChannelList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateWebhookRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Webhook;
import ch.cyberduck.core.sds.io.swagger.client.model.WebhookList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class SettingsApi {
  private ApiClient apiClient;

  public SettingsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SettingsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Create a new webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is created for given event types.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;user.created&#x60;** | Triggered when a new user is created | Customer Admin Webhook | | **&#x60;user.deleted&#x60;** | Triggered when a user is deleted | Customer Admin Webhook | | **&#x60;user.locked&#x60;** | Triggered when a user gets locked | Customer Admin Webhook | |  |  |  | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Customer Admin Webhook | |  |  |  | | **&#x60;downloadshare.created&#x60;** | Triggered when a new download share is created in affected room | Node Webhook | | **&#x60;downloadshare.deleted&#x60;** | Triggered when a download share is deleted in affected room | Node Webhook | | **&#x60;downloadshare.used&#x60;** | Triggered when a download share is utilized in affected room | Node Webhook | | **&#x60;uploadshare.created&#x60;** | Triggered when a new upload share is created in affected room | Node Webhook | | **&#x60;uploadshare.deleted&#x60;** | Triggered when a upload share is deleted in affected room | Node Webhook | | **&#x60;uploadshare.used&#x60;** | Triggered when a new file is uploaded via the upload share in affected room | Node Webhook | | **&#x60;file.created&#x60;** | Triggered when a new file is uploaded in affected room | Node Webhook | | **&#x60;folder.created&#x60;** | Triggered when a new folder is created in affected room | Node Webhook | | **&#x60;room.created&#x60;** | Triggered when a new room is created (in affected room) | Node Webhook | | **&#x60;file.deleted&#x60;** | Triggered when a file is deleted in affected room | Node Webhook | | **&#x60;folder.deleted&#x60;** | Triggered when a folder is deleted in affected room | Node Webhook | | **&#x60;room.deleted&#x60;** | Triggered when a room is deleted in affected room | Node Webhook |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook createWebhook(CreateWebhookRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return createWebhookWithHttpInfo(body, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Create webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Create a new webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is created for given event types.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;user.created&#x60;** | Triggered when a new user is created | Customer Admin Webhook | | **&#x60;user.deleted&#x60;** | Triggered when a user is deleted | Customer Admin Webhook | | **&#x60;user.locked&#x60;** | Triggered when a user gets locked | Customer Admin Webhook | |  |  |  | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Customer Admin Webhook | |  |  |  | | **&#x60;downloadshare.created&#x60;** | Triggered when a new download share is created in affected room | Node Webhook | | **&#x60;downloadshare.deleted&#x60;** | Triggered when a download share is deleted in affected room | Node Webhook | | **&#x60;downloadshare.used&#x60;** | Triggered when a download share is utilized in affected room | Node Webhook | | **&#x60;uploadshare.created&#x60;** | Triggered when a new upload share is created in affected room | Node Webhook | | **&#x60;uploadshare.deleted&#x60;** | Triggered when a upload share is deleted in affected room | Node Webhook | | **&#x60;uploadshare.used&#x60;** | Triggered when a new file is uploaded via the upload share in affected room | Node Webhook | | **&#x60;file.created&#x60;** | Triggered when a new file is uploaded in affected room | Node Webhook | | **&#x60;folder.created&#x60;** | Triggered when a new folder is created in affected room | Node Webhook | | **&#x60;room.created&#x60;** | Triggered when a new room is created (in affected room) | Node Webhook | | **&#x60;file.deleted&#x60;** | Triggered when a file is deleted in affected room | Node Webhook | | **&#x60;folder.deleted&#x60;** | Triggered when a folder is deleted in affected room | Node Webhook | | **&#x60;room.deleted&#x60;** | Triggered when a room is deleted in affected room | Node Webhook |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> createWebhookWithHttpInfo(CreateWebhookRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/settings/webhooks";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Delete a webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is deleted.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteWebhook(Long webhookId, String xSdsAuthToken) throws ApiException {

    deleteWebhookWithHttpInfo(webhookId, xSdsAuthToken);
  }

  /**
   * Delete webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Delete a webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is deleted.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteWebhookWithHttpInfo(Long webhookId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling deleteWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/settings/webhooks/{webhook_id}"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get list of event types 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of available (for _Config Manager_) event types.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: List of available event types is returned.  ### &amp;#9432; Further Information: None. 
   * @param xSdsAuthToken Authentication token (optional)
   * @return EventTypeList
   * @throws ApiException if fails to make API call
   */
  public EventTypeList getListOfEventTypesForConfigManager(String xSdsAuthToken) throws ApiException {
    return getListOfEventTypesForConfigManagerWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get list of event types 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of available (for _Config Manager_) event types.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: List of available event types is returned.  ### &amp;#9432; Further Information: None. 
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;EventTypeList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<EventTypeList> getListOfEventTypesForConfigManagerWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/settings/webhooks/event_types";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<EventTypeList> localVarReturnType = new GenericType<EventTypeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of webhooks 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of webhooks for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: List of webhooks is returned.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;name:cn:goo|createdAt:ge:2015-01-01&#x60;   Get webhooks where name contains &#x60;goo&#x60; **AND** webhook creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;id&#x60;** | Webhook id filter | &#x60;eq&#x60; | Webhook id equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**). |&#x60;positive number&#x60;| | **&#x60;name&#x60;** | Webhook type name| &#x60;cn, eq&#x60; | Webhook name contains / equals value. | &#x60;search String&#x60; | | **&#x60;isEnabled&#x60;** | Webhook isEnabled filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;expiration&#x60;** | Expiration date filter | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expiration:ge:2016-12-31&#x60;&amp;#124;&#x60;expiration:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;lastFailStatus&#x60;** | Failure status filter | &#x60;eq&#x60; | Last HTTP status code. Set when a webhook is auto-disabled due to repeated delivery failures |&#x60;positive number&#x60;|   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;id&#x60;** | Webhook id | | **&#x60;name&#x60;** | Webhook name | | **&#x60;isEnabled&#x60;** | Webhook isEnabled | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;expiration&#x60;** | Expiration date | 
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return WebhookList
   * @throws ApiException if fails to make API call
   */
  public WebhookList getListOfWebhooks(String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getListOfWebhooksWithHttpInfo(xSdsAuthToken, xSdsDateFormat, filter, limit, offset, sort).getData();
      }

  /**
   * Get list of webhooks 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of webhooks for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: List of webhooks is returned.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;name:cn:goo|createdAt:ge:2015-01-01&#x60;   Get webhooks where name contains &#x60;goo&#x60; **AND** webhook creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;id&#x60;** | Webhook id filter | &#x60;eq&#x60; | Webhook id equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**). |&#x60;positive number&#x60;| | **&#x60;name&#x60;** | Webhook type name| &#x60;cn, eq&#x60; | Webhook name contains / equals value. | &#x60;search String&#x60; | | **&#x60;isEnabled&#x60;** | Webhook isEnabled filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;expiration&#x60;** | Expiration date filter | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expiration:ge:2016-12-31&#x60;&amp;#124;&#x60;expiration:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;lastFailStatus&#x60;** | Failure status filter | &#x60;eq&#x60; | Last HTTP status code. Set when a webhook is auto-disabled due to repeated delivery failures |&#x60;positive number&#x60;|   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;id&#x60;** | Webhook id | | **&#x60;name&#x60;** | Webhook name | | **&#x60;isEnabled&#x60;** | Webhook isEnabled | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;expiration&#x60;** | Expiration date | 
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;WebhookList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookList> getListOfWebhooksWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/settings/webhooks";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<WebhookList> localVarReturnType = new GenericType<WebhookList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get customer settings
   * ### Functional Description:   Retrieve customer related settings.   ### Precondition: Right _\&quot;read config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return CustomerSettingsResponse
   * @throws ApiException if fails to make API call
   */
  public CustomerSettingsResponse getSettings(String xSdsAuthToken) throws ApiException {
    return getSettingsWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get customer settings
   * ### Functional Description:   Retrieve customer related settings.   ### Precondition: Right _\&quot;read config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;CustomerSettingsResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CustomerSettingsResponse> getSettingsWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/settings";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<CustomerSettingsResponse> localVarReturnType = new GenericType<CustomerSettingsResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a specific webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is returned.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook getWebhook(Long webhookId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return getWebhookWithHttpInfo(webhookId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Get webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a specific webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is returned.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> getWebhookWithHttpInfo(Long webhookId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling getWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/settings/webhooks/{webhook_id}"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Request list of notification channels 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of configured notification channels.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: List of notification channels is returned.  ### &amp;#9432; Further Information: None. 
   * @param xSdsAuthToken Authentication token (optional)
   * @return NotificationChannelList
   * @throws ApiException if fails to make API call
   */
  public NotificationChannelList requestNotificationChannels(String xSdsAuthToken) throws ApiException {
    return requestNotificationChannelsWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Request list of notification channels 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Retrieve a list of configured notification channels.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: List of notification channels is returned.  ### &amp;#9432; Further Information: None. 
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;NotificationChannelList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NotificationChannelList> requestNotificationChannelsWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/settings/notifications/channels";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<NotificationChannelList> localVarReturnType = new GenericType<NotificationChannelList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Reset webhook lifetime 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Reset the lifetime of a webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Lifetime of the webhook is reset.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook resetWebhookLifetime(Long webhookId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return resetWebhookLifetimeWithHttpInfo(webhookId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Reset webhook lifetime 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Reset the lifetime of a webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Lifetime of the webhook is reset.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> resetWebhookLifetimeWithHttpInfo(Long webhookId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling resetWebhookLifetime");
    }
    
    // create path and map variables
    String localVarPath = "/v4/settings/webhooks/{webhook_id}/reset_lifetime"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Set customer settings
   * ### Functional Description:   Set customer related settings.  ### Precondition: Right _\&quot;change config\&quot;_ required.   Role _\&quot;Config Manager\&quot;_.  ### Effects: Home Room configuration is updated.   ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return CustomerSettingsResponse
   * @throws ApiException if fails to make API call
   */
  public CustomerSettingsResponse setSettings(CustomerSettingsRequest body, String xSdsAuthToken) throws ApiException {
    return setSettingsWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Set customer settings
   * ### Functional Description:   Set customer related settings.  ### Precondition: Right _\&quot;change config\&quot;_ required.   Role _\&quot;Config Manager\&quot;_.  ### Effects: Home Room configuration is updated.   ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60; 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;CustomerSettingsResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CustomerSettingsResponse> setSettingsWithHttpInfo(CustomerSettingsRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setSettings");
    }
    
    // create path and map variables
    String localVarPath = "/v4/settings";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<CustomerSettingsResponse> localVarReturnType = new GenericType<CustomerSettingsResponse>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Toggle notification channels 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Toggle configured notification channels.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Channel status is switched.  ### &amp;#9432; Further Information: None. 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return NotificationChannelList
   * @throws ApiException if fails to make API call
   */
  public NotificationChannelList toggleNotificationChannels(NotificationChannelActivationRequest body, String xSdsAuthToken) throws ApiException {
    return toggleNotificationChannelsWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Toggle notification channels 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.20.0  ### Functional Description:   Toggle configured notification channels.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Channel status is switched.  ### &amp;#9432; Further Information: None. 
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;NotificationChannelList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NotificationChannelList> toggleNotificationChannelsWithHttpInfo(NotificationChannelActivationRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling toggleNotificationChannels");
    }
    
    // create path and map variables
    String localVarPath = "/v4/settings/notifications/channels";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<NotificationChannelList> localVarReturnType = new GenericType<NotificationChannelList>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Update an existing webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is updated.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters. Webhook event types can not be changed from Customer Admin Webhook types to Node Webhook types and vice versa    ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;user.created&#x60;** | Triggered when a new user is created | Customer Admin Webhook | | **&#x60;user.deleted&#x60;** | Triggered when a user is deleted | Customer Admin Webhook | | **&#x60;user.locked&#x60;** | Triggered when a user gets locked | Customer Admin Webhook | |  |  |  | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Customer Admin Webhook | |  |  |  | | **&#x60;downloadshare.created&#x60;** | Triggered when a new download share is created in affected room | Node Webhook | | **&#x60;downloadshare.deleted&#x60;** | Triggered when a download share is deleted in affected room | Node Webhook | | **&#x60;downloadshare.used&#x60;** | Triggered when a download share is utilized in affected room | Node Webhook | | **&#x60;uploadshare.created&#x60;** | Triggered when a new upload share is created in affected room | Node Webhook | | **&#x60;uploadshare.deleted&#x60;** | Triggered when a upload share is deleted in affected room | Node Webhook | | **&#x60;uploadshare.used&#x60;** | Triggered when a new file is uploaded via the upload share in affected room | Node Webhook | | **&#x60;file.created&#x60;** | Triggered when a new file is uploaded in affected room | Node Webhook | | **&#x60;folder.created&#x60;** | Triggered when a new folder is created in affected room | Node Webhook | | **&#x60;room.created&#x60;** | Triggered when a new room is created (in affected room) | Node Webhook | | **&#x60;file.deleted&#x60;** | Triggered when a file is deleted in affected room | Node Webhook | | **&#x60;folder.deleted&#x60;** | Triggered when a folder is deleted in affected room | Node Webhook | | **&#x60;room.deleted&#x60;** | Triggered when a room is deleted in affected room | Node Webhook |
   * @param body body (required)
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook updateWebhook(UpdateWebhookRequest body, Long webhookId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return updateWebhookWithHttpInfo(body, webhookId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Update webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Update an existing webhook for the customer scope.  ### Precondition: Right _\&quot;change config\&quot;_ required.  ### Effects: Webhook is updated.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters. Webhook event types can not be changed from Customer Admin Webhook types to Node Webhook types and vice versa    ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;user.created&#x60;** | Triggered when a new user is created | Customer Admin Webhook | | **&#x60;user.deleted&#x60;** | Triggered when a user is deleted | Customer Admin Webhook | | **&#x60;user.locked&#x60;** | Triggered when a user gets locked | Customer Admin Webhook | |  |  |  | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Customer Admin Webhook | |  |  |  | | **&#x60;downloadshare.created&#x60;** | Triggered when a new download share is created in affected room | Node Webhook | | **&#x60;downloadshare.deleted&#x60;** | Triggered when a download share is deleted in affected room | Node Webhook | | **&#x60;downloadshare.used&#x60;** | Triggered when a download share is utilized in affected room | Node Webhook | | **&#x60;uploadshare.created&#x60;** | Triggered when a new upload share is created in affected room | Node Webhook | | **&#x60;uploadshare.deleted&#x60;** | Triggered when a upload share is deleted in affected room | Node Webhook | | **&#x60;uploadshare.used&#x60;** | Triggered when a new file is uploaded via the upload share in affected room | Node Webhook | | **&#x60;file.created&#x60;** | Triggered when a new file is uploaded in affected room | Node Webhook | | **&#x60;folder.created&#x60;** | Triggered when a new folder is created in affected room | Node Webhook | | **&#x60;room.created&#x60;** | Triggered when a new room is created (in affected room) | Node Webhook | | **&#x60;file.deleted&#x60;** | Triggered when a file is deleted in affected room | Node Webhook | | **&#x60;folder.deleted&#x60;** | Triggered when a folder is deleted in affected room | Node Webhook | | **&#x60;room.deleted&#x60;** | Triggered when a room is deleted in affected room | Node Webhook |
   * @param body body (required)
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> updateWebhookWithHttpInfo(UpdateWebhookRequest body, Long webhookId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateWebhook");
    }
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling updateWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/settings/webhooks/{webhook_id}"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
