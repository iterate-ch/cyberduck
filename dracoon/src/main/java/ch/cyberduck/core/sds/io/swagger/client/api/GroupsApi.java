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
import ch.cyberduck.core.sds.io.swagger.client.model.ChangeGroupMembersRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateGroupRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Group;
import ch.cyberduck.core.sds.io.swagger.client.model.GroupList;
import ch.cyberduck.core.sds.io.swagger.client.model.GroupUserList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoleList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomTreeDataList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateGroupRequest;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
public class GroupsApi {
  private ApiClient apiClient;

  public GroupsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public GroupsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Add group members
   * ### Functional Description: Add members to a group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: New members are added to the group.  ### &amp;#9432; Further Information: Batch function.   The newly provided members will be added to the existing ones.
   * @param groupId Group ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group addGroupMembers(Long groupId, ChangeGroupMembersRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling addGroupMembers");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling addGroupMembers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}/users"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new user group
   * ### Functional Description: Create a new user group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: A new group is created.  ### &amp;#9432; Further Information: * If a group should not expire, leave &#x60;expireAt&#x60; empty. * Group names are limited to **150** characters * **All** characters are allowed.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group createGroup(CreateGroupRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete user group
   * ### Functional Description: Delete a user group.  ### Precondition: Right _\&quot;delete groups\&quot;_ required.  ### Effects: User group is deleted.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteGroup(Long groupId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling deleteGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


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


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete group members
   * ### Functional Description:   Remove group members.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: Provided users are removed from the user group.  ### &amp;#9432; Further Information: Batch function.   The provided users are removed from the user group.
   * @param groupId Group ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group deleteGroupMembers(Long groupId, ChangeGroupMembersRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling deleteGroupMembers");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteGroupMembers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}/users"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user group
   * ### Functional Description:   Retrieve detailed information about one user group.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group getGroup(Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


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

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get group roles
   * ### Functional Description:   Retrieve a list of all roles and the role assignment rights of a group.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoleList
   * @throws ApiException if fails to make API call
   */
  public RoleList getGroupRoles(Long groupId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroupRoles");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}/roles"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


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

    GenericType<RoleList> localVarReturnType = new GenericType<RoleList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get rooms granted to the group or / and rooms that can be granted
   * ### Functional Description:   Retrieve a list of rooms, that are granted or may be granted to the group.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:false|name:cn:searchString&#x60;   Get all rooms where the group is **NOT** granted **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;groupId&#x60;** | Group ID filter | &#x60;eq&#x60; | Group ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;name&#x60;** | Room name filter | &#x60;cn&#x60; | Room name contains value. | &#x60;search String&#x60; | | **&#x60;isGranted&#x60;** | Filter rooms which the group is (not) granted | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;permissionsManage&#x60;** | Filter the groups that do (not) have &#x60;manage&#x60; permissions in this room. | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectivePerm&#x60;** | Filter rooms with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;:  DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;true&#x60; |
   * @param groupId Group ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return RoomTreeDataList
   * @throws ApiException if fails to make API call
   */
  public RoomTreeDataList getGroupRooms(Long groupId, Integer offset, Integer limit, String filter, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroupRooms");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}/rooms"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));

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

    GenericType<RoomTreeDataList> localVarReturnType = new GenericType<RoomTreeDataList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get group member users or / and users who can become a member
   * ### Functional Description:   Retrieve a list of group members.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isMember:eq:false|displayName:cn:searchString&#x60;   Get all users that are **NOT** in this group **AND** whose (firstname **OR** lastname **OR** login) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;displayName&#x60;** | User display name filter | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;login&#x60;). | &#x60;search String&#x60; | | **&#x60;isMember&#x60;** | Filter group members | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; |
   * @param groupId Group ID (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return GroupUserList
   * @throws ApiException if fails to make API call
   */
  public GroupUserList getGroupUsers(Long groupId, Integer offset, Integer limit, String filter, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroupUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}/users"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));

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

    GenericType<GroupUserList> localVarReturnType = new GenericType<GroupUserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user groups
   * ### Functional Description:   Returns a list of user groups.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;name:cn:searchString&#x60;   Filter by group name containing &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;name:asc|expireAt:desc&#x60;   Sort by &#x60;name&#x60; ascending **AND** by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Group name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;cntUsers&#x60;** | Amount of users |
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return GroupList
   * @throws ApiException if fails to make API call
   */
  public GroupList getGroups(Integer offset, Integer limit, String filter, String sort, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/groups";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
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

    GenericType<GroupList> localVarReturnType = new GenericType<GroupList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update user group
   * ### Functional Description:   Update the meta data of a user group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: Meta data of a user group is updated.  ### &amp;#9432; Further Information: * If a group should not expire, leave &#x60;expireAt&#x60; empty. * Group names are limited to **150** characters * **All** characters are allowed.
   * @param groupId Group ID (required)
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group updateGroup(Long groupId, UpdateGroupRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling updateGroup");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateGroup");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
