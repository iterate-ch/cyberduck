package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.AuditNodeResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.LogEventList;
import ch.cyberduck.core.sds.io.swagger.client.model.LogOperationList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class EventlogApi {
  private ApiClient apiClient;

  public EventlogApi() {
    this(Configuration.getDefaultApiClient());
  }

  public EventlogApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get node assigned users with permissions
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve a list of all nodes of type &#x60;room&#x60;, and the room assignment users with permissions.  ### Precondition: Right _\&quot;read audit log\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;userName&#x60;**, **&#x60;userFirstName&#x60;** and  **&#x60;userLastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    Example: &gt; &#x60;userName:cn:searchString_1|userFirstName:cn:searchString_2|nodeId:eq:2&#x60;   Filter by user login containing &#x60;searchString_1&#x60; **OR** first name containing &#x60;searchString_2&#x60; **AND** node ID equals &#x60;2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID filter | &#x60;eq&#x60; | Node ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;nodeName&#x60;** | Node name filter | &#x60;cn, eq&#x60; | Node name contains / equals value. | &#x60;search String&#x60; | | **&#x60;nodeParentId&#x60;** | Node parent ID filter | &#x60;eq&#x60; | Parent ID equals value. | &#x60;positive Integer&#x60;&lt;br&gt;Parent ID &#x60;0&#x60; is the root node. | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;userName&#x60;** | Username (login) filter | &#x60;cn, eq&#x60; | Username contains / equals value. | &#x60;search String&#x60; | | **&#x60;userFirstName&#x60;** | User first name filter | &#x60;cn, eq&#x60; | User first name contains / equals value. | &#x60;search String&#x60; | | **&#x60;userLastName&#x60;** | User last name filter | &#x60;cn, eq&#x60; | User last name contains / equals value. | &#x60;search String&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the users that do (not) have &#x60;manage&#x60; permissions in this room | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeIsEncrypted&#x60;** | Encrypted node filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeHasActivitiesLog&#x60;** | Activities log filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeHasRecycleBin&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Recycle bin filter&lt;br&gt;**Filter has no effect!** | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;nodeName:asc&#x60;   Sort by &#x60;nodeName&#x60; ascending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID | | **&#x60;nodeName&#x60;** | Node name | | **&#x60;nodeParentId&#x60;** | Node parent ID | | **&#x60;nodeSize&#x60;** | Node size | | **&#x60;nodeQuota&#x60;** | Node quota |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return List&lt;AuditNodeResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public List<AuditNodeResponse> getAuditNodeUserData(String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getAuditNodeUserDataWithHttpInfo(xSdsAuthToken, xSdsDateFormat, filter, limit, offset, sort).getData();
      }

  /**
   * Get node assigned users with permissions
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve a list of all nodes of type &#x60;room&#x60;, and the room assignment users with permissions.  ### Precondition: Right _\&quot;read audit log\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;userName&#x60;**, **&#x60;userFirstName&#x60;** and  **&#x60;userLastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    Example: &gt; &#x60;userName:cn:searchString_1|userFirstName:cn:searchString_2|nodeId:eq:2&#x60;   Filter by user login containing &#x60;searchString_1&#x60; **OR** first name containing &#x60;searchString_2&#x60; **AND** node ID equals &#x60;2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID filter | &#x60;eq&#x60; | Node ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;nodeName&#x60;** | Node name filter | &#x60;cn, eq&#x60; | Node name contains / equals value. | &#x60;search String&#x60; | | **&#x60;nodeParentId&#x60;** | Node parent ID filter | &#x60;eq&#x60; | Parent ID equals value. | &#x60;positive Integer&#x60;&lt;br&gt;Parent ID &#x60;0&#x60; is the root node. | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;userName&#x60;** | Username (login) filter | &#x60;cn, eq&#x60; | Username contains / equals value. | &#x60;search String&#x60; | | **&#x60;userFirstName&#x60;** | User first name filter | &#x60;cn, eq&#x60; | User first name contains / equals value. | &#x60;search String&#x60; | | **&#x60;userLastName&#x60;** | User last name filter | &#x60;cn, eq&#x60; | User last name contains / equals value. | &#x60;search String&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the users that do (not) have &#x60;manage&#x60; permissions in this room | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeIsEncrypted&#x60;** | Encrypted node filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeHasActivitiesLog&#x60;** | Activities log filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeHasRecycleBin&#x60;** | (**&#x60;DEPRECATED&#x60;**)&lt;br&gt;Recycle bin filter&lt;br&gt;**Filter has no effect!** | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;nodeName:asc&#x60;   Sort by &#x60;nodeName&#x60; ascending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID | | **&#x60;nodeName&#x60;** | Node name | | **&#x60;nodeParentId&#x60;** | Node parent ID | | **&#x60;nodeSize&#x60;** | Node size | | **&#x60;nodeQuota&#x60;** | Node quota |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;List&lt;AuditNodeResponse&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<AuditNodeResponse>> getAuditNodeUserDataWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/eventlog/audits/nodes";

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

    GenericType<List<AuditNodeResponse>> localVarReturnType = new GenericType<List<AuditNodeResponse>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get system events
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve eventlog (audit log) events.  ### Precondition: Role _\&quot;Log Auditor\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;time:desc&#x60;   Sort by &#x60;time&#x60; descending (default sort option).  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;time&#x60;** | Event timestamp |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param dateEnd Filter events until given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateStart Filter events from given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userClient User client (optional)
   * @param userId User ID (optional)
   * @return LogEventList
   * @throws ApiException if fails to make API call
   */
  public LogEventList getLogEvents(String xSdsAuthToken, String xSdsDateFormat, String dateEnd, String dateStart, Integer limit, Integer offset, String sort, Integer status, Integer type, String userClient, Long userId) throws ApiException {
    return getLogEventsWithHttpInfo(xSdsAuthToken, xSdsDateFormat, dateEnd, dateStart, limit, offset, sort, status, type, userClient, userId).getData();
      }

  /**
   * Get system events
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve eventlog (audit log) events.  ### Precondition: Role _\&quot;Log Auditor\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;time:desc&#x60;   Sort by &#x60;time&#x60; descending (default sort option).  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;time&#x60;** | Event timestamp |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param dateEnd Filter events until given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateStart Filter events from given date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userClient User client (optional)
   * @param userId User ID (optional)
   * @return ApiResponse&lt;LogEventList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<LogEventList> getLogEventsWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, String dateEnd, String dateStart, Integer limit, Integer offset, String sort, Integer status, Integer type, String userClient, Long userId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/eventlog/events";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_client", userClient));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json", "text/csv"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<LogEventList> localVarReturnType = new GenericType<LogEventList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get allowed Log Operations
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve eventlog (audit log) operation IDs and the associated log operation description.  ### Precondition: Role _\&quot;Log Auditor\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @param isDeprecated Show only deprecated operations (optional)
   * @return LogOperationList
   * @throws ApiException if fails to make API call
   */
  public LogOperationList getLogOperations(String xSdsAuthToken, Boolean isDeprecated) throws ApiException {
    return getLogOperationsWithHttpInfo(xSdsAuthToken, isDeprecated).getData();
      }

  /**
   * Get allowed Log Operations
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve eventlog (audit log) operation IDs and the associated log operation description.  ### Precondition: Role _\&quot;Log Auditor\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @param isDeprecated Show only deprecated operations (optional)
   * @return ApiResponse&lt;LogOperationList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<LogOperationList> getLogOperationsWithHttpInfo(String xSdsAuthToken, Boolean isDeprecated) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/eventlog/operations";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "is_deprecated", isDeprecated));

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

    GenericType<LogOperationList> localVarReturnType = new GenericType<LogOperationList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
