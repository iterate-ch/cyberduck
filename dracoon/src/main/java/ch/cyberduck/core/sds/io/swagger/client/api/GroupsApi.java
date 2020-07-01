package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ChangeGroupMembersRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateGroupRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.Group;
import ch.cyberduck.core.sds.io.swagger.client.model.GroupList;
import ch.cyberduck.core.sds.io.swagger.client.model.GroupUserList;
import ch.cyberduck.core.sds.io.swagger.client.model.LastAdminGroupRoomList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoleList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomTreeDataList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateGroupRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
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
   * @param body body (required)
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group addGroupMembers(ChangeGroupMembersRequest body, Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return addGroupMembersWithHttpInfo(body, groupId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Add group members
   * ### Functional Description: Add members to a group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: New members are added to the group.  ### &amp;#9432; Further Information: Batch function.   The newly provided members will be added to the existing ones.
   * @param body body (required)
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> addGroupMembersWithHttpInfo(ChangeGroupMembersRequest body, Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling addGroupMembers");
    }
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling addGroupMembers");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new user group
   * ### Functional Description: Create a new user group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: A new group is created.  ### &amp;#9432; Further Information: * If a group should **NOT** expire, leave &#x60;expireAt&#x60; empty. * Group names are limited to **150** characters * **All** characters are allowed.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group createGroup(CreateGroupRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return createGroupWithHttpInfo(body, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Create new user group
   * ### Functional Description: Create a new user group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: A new group is created.  ### &amp;#9432; Further Information: * If a group should **NOT** expire, leave &#x60;expireAt&#x60; empty. * Group names are limited to **150** characters * **All** characters are allowed.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> createGroupWithHttpInfo(CreateGroupRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
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

    deleteGroupWithHttpInfo(groupId, xSdsAuthToken);
  }

  /**
   * Delete user group
   * ### Functional Description: Delete a user group.  ### Precondition: Right _\&quot;delete groups\&quot;_ required.  ### Effects: User group is deleted.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteGroupWithHttpInfo(Long groupId, String xSdsAuthToken) throws ApiException {
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
   * Remove group members
   * ### Functional Description:   Remove group members.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: Provided users are removed from the user group.  ### &amp;#9432; Further Information: Batch function.   The provided users are removed from the user group.
   * @param body body (required)
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group deleteGroupMembers(ChangeGroupMembersRequest body, Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return deleteGroupMembersWithHttpInfo(body, groupId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Remove group members
   * ### Functional Description:   Remove group members.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: Provided users are removed from the user group.  ### &amp;#9432; Further Information: Batch function.   The provided users are removed from the user group.
   * @param body body (required)
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> deleteGroupMembersWithHttpInfo(ChangeGroupMembersRequest body, Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteGroupMembers");
    }
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling deleteGroupMembers");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get user group
   * ### Functional Description:   Retrieve detailed information about a user group.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group getGroup(Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return getGroupWithHttpInfo(groupId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Get user group
   * ### Functional Description:   Retrieve detailed information about a user group.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> getGroupWithHttpInfo(Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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
      "application/json"
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
   * Get list of roles assigned to the group
   * ### Functional Description:   Retrieve a list of all roles granted to a group.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoleList
   * @throws ApiException if fails to make API call
   */
  public RoleList getGroupRoles(Long groupId, String xSdsAuthToken) throws ApiException {
    return getGroupRolesWithHttpInfo(groupId, xSdsAuthToken).getData();
      }

  /**
   * Get list of roles assigned to the group
   * ### Functional Description:   Retrieve a list of all roles granted to a group.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RoleList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleList> getGroupRolesWithHttpInfo(Long groupId, String xSdsAuthToken) throws ApiException {
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
      "application/json"
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
   * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   Retrieves a list of rooms granted to the group and / or that can be granted.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:false|name:cn:searchString&#x60;   Get all rooms where the group is **NOT** granted **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Room name filter | &#x60;cn&#x60; | Room name contains value. | &#x60;search String&#x60; | | **&#x60;isGranted&#x60;** | Filter rooms which the group is (not) granted | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;effectivePerm&#x60;** | Filter rooms with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;:  DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;true&#x60; |
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return RoomTreeDataList
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public RoomTreeDataList getGroupRooms(Long groupId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset) throws ApiException {
    return getGroupRoomsWithHttpInfo(groupId, xSdsAuthToken, xSdsDateFormat, filter, limit, offset).getData();
      }

  /**
   * Get rooms granted to the group or / and rooms that can be granted
   * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   Retrieves a list of rooms granted to the group and / or that can be granted.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:false|name:cn:searchString&#x60;   Get all rooms where the group is **NOT** granted **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Room name filter | &#x60;cn&#x60; | Room name contains value. | &#x60;search String&#x60; | | **&#x60;isGranted&#x60;** | Filter rooms which the group is (not) granted | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;effectivePerm&#x60;** | Filter rooms with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;:  DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;true&#x60; |
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;RoomTreeDataList&gt;
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public ApiResponse<RoomTreeDataList> getGroupRoomsWithHttpInfo(Long groupId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));

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

    GenericType<RoomTreeDataList> localVarReturnType = new GenericType<RoomTreeDataList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get group member users or / and users who can become a member
   * ### Functional Description:   Retrieve a list of group member users or / and users who can become a member.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isMember:eq:false|user:cn:searchString&#x60;   Get all users that are **NOT** in this group **AND** whose (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;user&#x60;** | User filter | &#x60;cn&#x60; | User contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;isMember&#x60;** | Filter group members | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;displayName&#x60;** | (**&#x60;DEPRECATED&#x60;**) User display name filter (use **&#x60;user&#x60;** filter) | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60;). | &#x60;search String&#x60; | 
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return GroupUserList
   * @throws ApiException if fails to make API call
   */
  public GroupUserList getGroupUsers(Long groupId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    return getGroupUsersWithHttpInfo(groupId, xSdsAuthToken, filter, limit, offset).getData();
      }

  /**
   * Get group member users or / and users who can become a member
   * ### Functional Description:   Retrieve a list of group member users or / and users who can become a member.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isMember:eq:false|user:cn:searchString&#x60;   Get all users that are **NOT** in this group **AND** whose (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;user&#x60;** | User filter | &#x60;cn&#x60; | User contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;isMember&#x60;** | Filter group members | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;displayName&#x60;** | (**&#x60;DEPRECATED&#x60;**) User display name filter (use **&#x60;user&#x60;** filter) | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60;). | &#x60;search String&#x60; | 
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;GroupUserList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GroupUserList> getGroupUsersWithHttpInfo(Long groupId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));

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

    GenericType<GroupUserList> localVarReturnType = new GenericType<GroupUserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of user groups
   * ### Functional Description:   Returns a list of user groups.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;name:cn:searchString&#x60;   Filter by group name containing &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;name:asc|expireAt:desc&#x60;   Sort by &#x60;name&#x60; ascending **AND** by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Group name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;cntUsers&#x60;** | Amount of users |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return GroupList
   * @throws ApiException if fails to make API call
   */
  public GroupList getGroups(String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getGroupsWithHttpInfo(xSdsAuthToken, xSdsDateFormat, filter, limit, offset, sort).getData();
      }

  /**
   * Get list of user groups
   * ### Functional Description:   Returns a list of user groups.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;name:cn:searchString&#x60;   Filter by group name containing &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;name:asc|expireAt:desc&#x60;   Sort by &#x60;name&#x60; ascending **AND** by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Group name | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;cntUsers&#x60;** | Amount of users |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;GroupList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<GroupList> getGroupsWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/groups";

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

    GenericType<GroupList> localVarReturnType = new GenericType<GroupList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get rooms where the group is defined as last admin group
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description:   Retrieve a list of all rooms where the group is defined as last admin group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: List of rooms is returned.   ### &amp;#9432; Further Information: An empty list is returned if no rooms were found where the group is defined as last admin group.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return LastAdminGroupRoomList
   * @throws ApiException if fails to make API call
   */
  public LastAdminGroupRoomList getLastAdminRoomsGroups(Long groupId, String xSdsAuthToken) throws ApiException {
    return getLastAdminRoomsGroupsWithHttpInfo(groupId, xSdsAuthToken).getData();
      }

  /**
   * Get rooms where the group is defined as last admin group
   * ### &amp;#128640; Since version 4.10.0  ### Functional Description:   Retrieve a list of all rooms where the group is defined as last admin group.  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: List of rooms is returned.   ### &amp;#9432; Further Information: An empty list is returned if no rooms were found where the group is defined as last admin group.
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;LastAdminGroupRoomList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<LastAdminGroupRoomList> getLastAdminRoomsGroupsWithHttpInfo(Long groupId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getLastAdminRoomsGroups");
    }
    
    // create path and map variables
    String localVarPath = "/v4/groups/{group_id}/last_admin_rooms"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

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

    GenericType<LastAdminGroupRoomList> localVarReturnType = new GenericType<LastAdminGroupRoomList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update user group&#39;s metadata
   * ### Functional Description:   Update user group&#39;s metadata .  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: User group&#39;s metadata is changed.  ### &amp;#9432; Further Information: * If a group should **NOT** expire, leave &#x60;expireAt&#x60; empty. * Group names are limited to **150** characters * **All** characters are allowed.
   * @param body body (required)
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group updateGroup(UpdateGroupRequest body, Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    return updateGroupWithHttpInfo(body, groupId, xSdsAuthToken, xSdsDateFormat).getData();
      }

  /**
   * Update user group&#39;s metadata
   * ### Functional Description:   Update user group&#39;s metadata .  ### Precondition: Right _\&quot;change groups\&quot;_ required.  ### Effects: User group&#39;s metadata is changed.  ### &amp;#9432; Further Information: * If a group should **NOT** expire, leave &#x60;expireAt&#x60; empty. * Group names are limited to **150** characters * **All** characters are allowed.
   * @param body body (required)
   * @param groupId Group ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Group&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Group> updateGroupWithHttpInfo(UpdateGroupRequest body, Long groupId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateGroup");
    }
    
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling updateGroup");
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
