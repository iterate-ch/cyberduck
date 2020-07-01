package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.GroupIds;
import ch.cyberduck.core.sds.io.swagger.client.model.RoleGroupList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoleList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoleUserList;
import ch.cyberduck.core.sds.io.swagger.client.model.UserIds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class RolesApi {
  private ApiClient apiClient;

  public RolesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RolesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Assign group(s) to the role
   * ### Functional Description: Assign group(s) to a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.  ### Effects: One or more groups will be added to a role.  ### &amp;#9432; Further Information: None.
   * @param body Group IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoleGroupList
   * @throws ApiException if fails to make API call
   */
  public RoleGroupList addRoleGroups(GroupIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    return addRoleGroupsWithHttpInfo(body, roleId, xSdsAuthToken).getData();
      }

  /**
   * Assign group(s) to the role
   * ### Functional Description: Assign group(s) to a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.  ### Effects: One or more groups will be added to a role.  ### &amp;#9432; Further Information: None.
   * @param body Group IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RoleGroupList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleGroupList> addRoleGroupsWithHttpInfo(GroupIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling addRoleGroups");
    }
    
    // verify the required parameter 'roleId' is set
    if (roleId == null) {
      throw new ApiException(400, "Missing the required parameter 'roleId' when calling addRoleGroups");
    }
    
    // create path and map variables
    String localVarPath = "/v4/roles/{role_id}/groups"
      .replaceAll("\\{" + "role_id" + "\\}", apiClient.escapeString(roleId.toString()));

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

    GenericType<RoleGroupList> localVarReturnType = new GenericType<RoleGroupList>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Assign user(s) to the role
   * ### Functional Description: Assign user(s) to a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.  ### Effects: One or more users will be added to a role.  ### &amp;#9432; Further Information: None.
   * @param body User IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoleUserList
   * @throws ApiException if fails to make API call
   */
  public RoleUserList addRoleUsers(UserIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    return addRoleUsersWithHttpInfo(body, roleId, xSdsAuthToken).getData();
      }

  /**
   * Assign user(s) to the role
   * ### Functional Description: Assign user(s) to a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.  ### Effects: One or more users will be added to a role.  ### &amp;#9432; Further Information: None.
   * @param body User IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RoleUserList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleUserList> addRoleUsersWithHttpInfo(UserIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling addRoleUsers");
    }
    
    // verify the required parameter 'roleId' is set
    if (roleId == null) {
      throw new ApiException(400, "Missing the required parameter 'roleId' when calling addRoleUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/roles/{role_id}/users"
      .replaceAll("\\{" + "role_id" + "\\}", apiClient.escapeString(roleId.toString()));

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

    GenericType<RoleUserList> localVarReturnType = new GenericType<RoleUserList>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Revoke granted role from group(s)
   * ### Functional Description:   Revoke granted group(s) from a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.   For each role, at least one non-expiring user **MUST** remain who may grant the role.  ### Effects: One or more groups will be removed from a role.  ### &amp;#9432; Further Information: None.
   * @param body Group IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoleGroupList
   * @throws ApiException if fails to make API call
   */
  public RoleGroupList deleteRoleGroups(GroupIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    return deleteRoleGroupsWithHttpInfo(body, roleId, xSdsAuthToken).getData();
      }

  /**
   * Revoke granted role from group(s)
   * ### Functional Description:   Revoke granted group(s) from a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.   For each role, at least one non-expiring user **MUST** remain who may grant the role.  ### Effects: One or more groups will be removed from a role.  ### &amp;#9432; Further Information: None.
   * @param body Group IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RoleGroupList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleGroupList> deleteRoleGroupsWithHttpInfo(GroupIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoleGroups");
    }
    
    // verify the required parameter 'roleId' is set
    if (roleId == null) {
      throw new ApiException(400, "Missing the required parameter 'roleId' when calling deleteRoleGroups");
    }
    
    // create path and map variables
    String localVarPath = "/v4/roles/{role_id}/groups"
      .replaceAll("\\{" + "role_id" + "\\}", apiClient.escapeString(roleId.toString()));

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

    GenericType<RoleGroupList> localVarReturnType = new GenericType<RoleGroupList>() {};
    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Revoke granted role from user(s)
   * ### Functional Description:   Revoke granted user(s) from a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.   For each role, at least one non-expiring user **MUST** remain who may grant the role.  ### Effects: One or more users will be removed from a role.  ### &amp;#9432; Further Information: None.
   * @param body User IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoleUserList
   * @throws ApiException if fails to make API call
   */
  public RoleUserList deleteRoleUsers(UserIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    return deleteRoleUsersWithHttpInfo(body, roleId, xSdsAuthToken).getData();
      }

  /**
   * Revoke granted role from user(s)
   * ### Functional Description:   Revoke granted user(s) from a role.  ### Precondition: Right _\&quot;grant permission on desired role\&quot;_ required.   For each role, at least one non-expiring user **MUST** remain who may grant the role.  ### Effects: One or more users will be removed from a role.  ### &amp;#9432; Further Information: None.
   * @param body User IDs (required)
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RoleUserList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleUserList> deleteRoleUsersWithHttpInfo(UserIds body, Integer roleId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteRoleUsers");
    }
    
    // verify the required parameter 'roleId' is set
    if (roleId == null) {
      throw new ApiException(400, "Missing the required parameter 'roleId' when calling deleteRoleUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/roles/{role_id}/users"
      .replaceAll("\\{" + "role_id" + "\\}", apiClient.escapeString(roleId.toString()));

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

    GenericType<RoleUserList> localVarReturnType = new GenericType<RoleUserList>() {};
    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get groups with specific role
   * ### Functional Description:   Get all groups with a specific role.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isMember:eq:false|name:cn:searchString&#x60;   Get all groups that are **NOT** a member of that role **AND** whose name contains &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isMember&#x60;** | Filter the groups which are (not) member of that role | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; |
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return RoleGroupList
   * @throws ApiException if fails to make API call
   */
  public RoleGroupList getRoleGroups(Integer roleId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    return getRoleGroupsWithHttpInfo(roleId, xSdsAuthToken, filter, limit, offset).getData();
      }

  /**
   * Get groups with specific role
   * ### Functional Description:   Get all groups with a specific role.  ### Precondition: Right _\&quot;read groups\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isMember:eq:false|name:cn:searchString&#x60;   Get all groups that are **NOT** a member of that role **AND** whose name contains &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;isMember&#x60;** | Filter the groups which are (not) member of that role | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; |
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;RoleGroupList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleGroupList> getRoleGroupsWithHttpInfo(Integer roleId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roleId' is set
    if (roleId == null) {
      throw new ApiException(400, "Missing the required parameter 'roleId' when calling getRoleGroups");
    }
    
    // create path and map variables
    String localVarPath = "/v4/roles/{role_id}/groups"
      .replaceAll("\\{" + "role_id" + "\\}", apiClient.escapeString(roleId.toString()));

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

    GenericType<RoleGroupList> localVarReturnType = new GenericType<RoleGroupList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get users with specific role
   * ### Functional Description:   Get all users with a specific role.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isMember:eq:false|user:cn:searchString&#x60;   Get all users that are **NOT** member of that role **AND** whose (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;user&#x60;** | User filter | &#x60;cn&#x60; | User contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;isMember&#x60;** | Filter the users which are (not) member of that role | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;displayName&#x60;** | (**&#x60;DEPRECATED&#x60;**) User display name filter (use **&#x60;user&#x60;** filter) | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60;). | &#x60;search String&#x60; | 
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return RoleUserList
   * @throws ApiException if fails to make API call
   */
  public RoleUserList getRoleUsers(Integer roleId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    return getRoleUsersWithHttpInfo(roleId, xSdsAuthToken, filter, limit, offset).getData();
      }

  /**
   * Get users with specific role
   * ### Functional Description:   Get all users with a specific role.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isMember:eq:false|user:cn:searchString&#x60;   Get all users that are **NOT** member of that role **AND** whose (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;user&#x60;** | User filter | &#x60;cn&#x60; | User contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;). | &#x60;search String&#x60; | | **&#x60;isMember&#x60;** | Filter the users which are (not) member of that role | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;displayName&#x60;** | (**&#x60;DEPRECATED&#x60;**) User display name filter (use **&#x60;user&#x60;** filter) | &#x60;cn&#x60; | User display name contains value (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60;). | &#x60;search String&#x60; | 
   * @param roleId Role ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @return ApiResponse&lt;RoleUserList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleUserList> getRoleUsersWithHttpInfo(Integer roleId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'roleId' is set
    if (roleId == null) {
      throw new ApiException(400, "Missing the required parameter 'roleId' when calling getRoleUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/roles/{role_id}/users"
      .replaceAll("\\{" + "role_id" + "\\}", apiClient.escapeString(roleId.toString()));

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

    GenericType<RoleUserList> localVarReturnType = new GenericType<RoleUserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get all roles with assigned rights
   * ### Functional Description:   Retrieve a list of all roles with assigned rights.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return RoleList
   * @throws ApiException if fails to make API call
   */
  public RoleList getRoles(String xSdsAuthToken) throws ApiException {
    return getRolesWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get all roles with assigned rights
   * ### Functional Description:   Retrieve a list of all roles with assigned rights.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;RoleList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RoleList> getRolesWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/roles";

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
}
