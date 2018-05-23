package ch.cyberduck.core.sds.io.swagger.client.api;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.AuditNodeResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.LogEventList;
import ch.cyberduck.core.sds.io.swagger.client.model.LogOperationList;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
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
   * ### Functional Description:   Retrieve a list of all nodes of type &#x60;room&#x60; and the room assignment users with permissions.  ### Precondition: Right _\&quot;read audit log\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;userName&#x60;**, **&#x60;userFirstName&#x60;** and  **&#x60;userLastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    Example: &gt; &#x60;userName:cn:searchString_1|userFirstName:cn:searchString_2|nodeId:eq:2&#x60;   Filter by user login containing &#x60;searchString_1&#x60; **OR** first name containing &#x60;searchString_2&#x60; **AND** node ID equals &#x60;2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID filter | &#x60;eq&#x60; | Node ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;nodeName&#x60;** | Node name filter | &#x60;cn, eq&#x60; | Node name contains / equals value. | &#x60;search String&#x60; | | **&#x60;nodeParentId&#x60;** | Node parent ID filter | &#x60;eq&#x60; | Parent ID equals value. | &#x60;positive Integer&#x60;&lt;br&gt;Parent ID &#x60;0&#x60; is the root node. | | **&#x60;userId&#x60;** | User ID filter | &#x60;eq&#x60; | User ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;userName&#x60;** | User name (login) filter | &#x60;cn, eq&#x60; | User name contains / equals value. | &#x60;search String&#x60; | | **&#x60;userFirstName&#x60;** | User first name filter | &#x60;cn, eq&#x60; | User first name contains / equals value. | &#x60;search String&#x60; | | **&#x60;userLastName&#x60;** | User last name filter | &#x60;cn, eq&#x60; | User last name contains / equals value. | &#x60;search String&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the users that do (not) have &#x60;manage&#x60; permissions in this room | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeIsEncrypted&#x60;** | Encrypted node filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeHasRecycleBin&#x60;** | Recycle bin filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;nodeHasActivitiesLog&#x60;** | Activities log filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;nodeName:asc&#x60;   Sort by &#x60;nodeName&#x60; ascending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;nodeId&#x60;** | Node ID | | **&#x60;nodeName&#x60;** | Node name | | **&#x60;nodeParentId&#x60;** | Node parent ID | | **&#x60;nodeSize&#x60;** | Node size | | **&#x60;nodeQuota&#x60;** | Node quota |
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return List&lt;AuditNodeResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public List<AuditNodeResponse> getAuditNodeUserData(String filter, String sort, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/eventlog/audits/nodes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
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
   * ### Functional Description:   Retrieve eventlog (audit log) events.  ### Precondition: Role _\&quot;Log Auditor\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: Output may be limited to a certain number of entries.   Please use filter criteria and paging.
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param dateStart Start date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param dateEnd End date e.g. &#x60;2015-12-31T23:59:00&#x60; (optional)
   * @param type Operation ID cf. &#x60;GET /eventlog/operations&#x60; (optional)
   * @param userId User ID (optional)
   * @param status Operation status: * &#x60;0&#x60; - Success * &#x60;2&#x60; - Error (optional)
   * @param userClient User client (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return LogEventList
   * @throws ApiException if fails to make API call
   */
  public LogEventList getLogEvents(Integer offset, Integer limit, String dateStart, String dateEnd, Integer type, Long userId, Integer status, String userClient, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/eventlog/events";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "status", status));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_client", userClient));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8", "text/csv"
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
   * ### Functional Description:   Retrieve eventlog (audit log) operation IDs and the associated log operation description.  ### Precondition: Role _\&quot;Log Auditor\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param isDeprecated Show only deprecated operations (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return LogOperationList
   * @throws ApiException if fails to make API call
   */
  public LogOperationList getLogOperations(Boolean isDeprecated, String xSdsAuthToken) throws ApiException {
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
      "application/json;charset=UTF-8"
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
