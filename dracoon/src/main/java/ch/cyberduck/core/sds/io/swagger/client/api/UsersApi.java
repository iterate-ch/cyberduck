package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateUserRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LastAdminUserRoomList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoleList;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomTreeDataList;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateUserRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserAttributes;
import ch.cyberduck.core.sds.io.swagger.client.model.UserData;
import ch.cyberduck.core.sds.io.swagger.client.model.UserGroupList;
import ch.cyberduck.core.sds.io.swagger.client.model.UserList;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-05-17T14:22:07.810+02:00")
public class UsersApi {
  private ApiClient apiClient;

  public UsersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UsersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create new user
   * ### Functional Description: Create a new user.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: A new user is created.  ### &amp;#9432; Further Information: * If a user should **NOT** expire, leave &#x60;expireAt&#x60; empty. * All input fields are limited to **150** characters * **All** characters are allowed.  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | none | none | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
   * @return UserData
   * @throws ApiException if fails to make API call
   */
  public UserData createUser(CreateUserRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
      return createUserWithHttpInfo(body, xSdsAuthToken, xSdsDateFormat).getData();
  }

    /**
     * Create new user
     * ### Functional Description: Create a new user.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: A new user is created.  ### &amp;#9432; Further Information: * If a user should **NOT** expire, leave &#x60;expireAt&#x60; empty. * All input fields are limited to **150** characters * **All** characters are allowed.  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | none | none | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |
     *
     * @param body           body (required)
     * @param xSdsAuthToken  Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @return ApiResponse&lt;UserData&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserData> createUserWithHttpInfo(CreateUserRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling createUser");
        }

        // create path and map variables
        String localVarPath = "/v4/users";

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

        GenericType<UserData> localVarReturnType = new GenericType<UserData>() {};
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete user
     * ### Functional Description: Delete a user.  ### Precondition: Right _\&quot;delete users\&quot;_ required.  ### Effects: User is deleted.  ### &amp;#9432; Further Information: User **CANNOT** be deleted if he is a last room administrator of any room.
   * @param userId User ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
    public void deleteUser(Long userId, String xSdsAuthToken) throws ApiException {

        deleteUserWithHttpInfo(userId, xSdsAuthToken);
    }

    /**
     * Delete user
     * ### Functional Description: Delete a user.  ### Precondition: Right _\&quot;delete users\&quot;_ required.  ### Effects: User is deleted.  ### &amp;#9432; Further Information: User **CANNOT** be deleted if he is a last room administrator of any room.
     *
     * @param userId        User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteUserWithHttpInfo(Long userId, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling deleteUser");
        }

        // create path and map variables
        String localVarPath = "/v4/users/{user_id}"
            .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};


        return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    }

    /**
     * Delete custom user attribute
     * ### Functional Description: Delete custom user attribute.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: Custom user attribute gets deleted.  ### &amp;#9432; Further Information: * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
   * @param key Key (required)
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @throws ApiException if fails to make API call
     */
    public void deleteUserAttributes(String key, Long userId, String xSdsAuthToken) throws ApiException {

        deleteUserAttributesWithHttpInfo(key, userId, xSdsAuthToken);
    }

    /**
     * Delete custom user attribute
     * ### Functional Description: Delete custom user attribute.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: Custom user attribute gets deleted.  ### &amp;#9432; Further Information: * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
     * @param key Key (required)
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteUserAttributesWithHttpInfo(String key, Long userId, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'key' is set
        if(key == null) {
            throw new ApiException(400, "Missing the required parameter 'key' when calling deleteUserAttributes");
        }

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling deleteUserAttributes");
        }

        // create path and map variables
        String localVarPath = "/v4/users/{user_id}/userAttributes/{key}"
            .replaceAll("\\{" + "key" + "\\}", apiClient.escapeString(key.toString()))
            .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};


        return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    }

    /**
     * Get rooms where the user is last admin
     * ### &amp;#128640; Since version 4.10.2  ### Functional Description:   Retrieve a list of all rooms where the user is last admin (except homeroom and its subordinary rooms).  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: List of rooms is returned.   ### &amp;#9432; Further Information: An empty list is returned if no rooms were found where the user is last admin.
     *
     * @param userId        User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return LastAdminUserRoomList
     * @throws ApiException if fails to make API call
     */
    public LastAdminUserRoomList getLastAdminRoomsUsers(Long userId, String xSdsAuthToken) throws ApiException {
        return getLastAdminRoomsUsersWithHttpInfo(userId, xSdsAuthToken).getData();
    }

    /**
     * Get rooms where the user is last admin
     * ### &amp;#128640; Since version 4.10.2  ### Functional Description:   Retrieve a list of all rooms where the user is last admin (except homeroom and its subordinary rooms).  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: List of rooms is returned.   ### &amp;#9432; Further Information: An empty list is returned if no rooms were found where the user is last admin.
     *
     * @param userId        User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;LastAdminUserRoomList&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LastAdminUserRoomList> getLastAdminRoomsUsersWithHttpInfo(Long userId, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'userId' is set
        if(userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getLastAdminRoomsUsers");
        }

        // create path and map variables
        String localVarPath = "/v4/users/{user_id}/last_admin_rooms"
            .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<LastAdminUserRoomList> localVarReturnType = new GenericType<LastAdminUserRoomList>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user
     * ### Functional Description:   Retrieve detailed information about a single user.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | none | none | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @param effectiveRoles Filter users with DIRECT or DIRECT **AND** EFFECTIVE roles. * &#x60;false&#x60;: DIRECT roles * &#x60;true&#x60;: DIRECT **AND** EFFECTIVE roles  DIRECT means: e.g. user gets role **directly** granted from someone with _grant permission_ right. EFFECTIVE means: e.g. user gets role through **group membership**. (optional)
     * @return UserData
     * @throws ApiException if fails to make API call
     */
    public UserData getUser(Long userId, String xSdsAuthToken, String xSdsDateFormat, Boolean effectiveRoles) throws ApiException {
        return getUserWithHttpInfo(userId, xSdsAuthToken, xSdsDateFormat, effectiveRoles).getData();
    }

    /**
     * Get user
     * ### Functional Description:   Retrieve detailed information about a single user.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | none | none | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |
     *
     * @param userId         User ID (required)
     * @param xSdsAuthToken  Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @param effectiveRoles Filter users with DIRECT or DIRECT **AND** EFFECTIVE roles. * &#x60;false&#x60;: DIRECT roles * &#x60;true&#x60;: DIRECT **AND** EFFECTIVE roles  DIRECT means: e.g. user gets role **directly** granted from someone with _grant permission_ right. EFFECTIVE means: e.g. user gets role through **group membership**. (optional)
     * @return ApiResponse&lt;UserData&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserData> getUserWithHttpInfo(Long userId, String xSdsAuthToken, String xSdsDateFormat, Boolean effectiveRoles) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUser");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users/{user_id}"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "effective_roles", effectiveRoles));

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

    GenericType<UserData> localVarReturnType = new GenericType<UserData>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get groups that user is a member of or / and can become a member
     * ### Functional Description:   Retrieves a list of groups a user is member of and / or can become a member.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;is_member:eq:false|name:cn:searchString&#x60;   Get all groups that the user is **NOT** member of **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; | | **&#x60;isMember&#x60;** | Filter the groups which the user is (not) member of | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; |
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit (optional)
     * @param offset Range offset (optional)
     * @return UserGroupList
     * @throws ApiException if fails to make API call
     */
    public UserGroupList getUserGroups(Long userId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
        return getUserGroupsWithHttpInfo(userId, xSdsAuthToken, filter, limit, offset).getData();
    }

    /**
     * Get groups that user is a member of or / and can become a member
     * ### Functional Description:   Retrieves a list of groups a user is member of and / or can become a member.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;is_member:eq:false|name:cn:searchString&#x60;   Get all groups that the user is **NOT** member of **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Group name filter | &#x60;cn&#x60; | Group name contains value. | &#x60;search String&#x60; | | **&#x60;isMember&#x60;** | Filter the groups which the user is (not) member of | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; |
     *
     * @param userId        User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param filter        Filter string (optional)
     * @param limit         Range limit (optional)
     * @param offset        Range offset (optional)
     * @return ApiResponse&lt;UserGroupList&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserGroupList> getUserGroupsWithHttpInfo(Long userId, String xSdsAuthToken, String filter, Integer limit, Integer offset) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUserGroups");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users/{user_id}/groups"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UserGroupList> localVarReturnType = new GenericType<UserGroupList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user&#39;s granted roles
     * ### Functional Description:   Retrieve a list of all roles granted to a user.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param userId User ID (required)
   * @param xSdsAuthToken Authentication token (optional)
     * @return RoleList
     * @throws ApiException if fails to make API call
     */
    public RoleList getUserRoles(Long userId, String xSdsAuthToken) throws ApiException {
        return getUserRolesWithHttpInfo(userId, xSdsAuthToken).getData();
    }

    /**
     * Get user&#39;s granted roles
     * ### Functional Description:   Retrieve a list of all roles granted to a user.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;RoleList&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<RoleList> getUserRolesWithHttpInfo(Long userId, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getUserRoles");
        }

        // create path and map variables
        String localVarPath = "/v4/users/{user_id}/roles"
            .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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
     * Get users
     * ### Functional Description:   Returns a list of DRACOON users.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information:   None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;login&#x60;**, **&#x60;firstName&#x60;** and  **&#x60;lastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    ### Example: &gt; &#x60;login:cn:searchString_1|firstName:cn:searchString_2|lockStatus:eq:2&#x60;   Filter users by login contains &#x60;searchString_1&#x60; **OR** firstName contains &#x60;searchString_2&#x60; **AND** those who are **NOT** locked.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;login&#x60;** | User login filter | &#x60;cn&#x60; | User login contains value. | &#x60;search String&#x60; | | **&#x60;firstName&#x60;** | User first name filter | &#x60;cn&#x60; | User first name contains value. | &#x60;search String&#x60; | | **&#x60;lastName&#x60;** | User last name filter | &#x60;cn&#x60; | User last name contains value. | &#x60;search String&#x60; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status filter | &#x60;eq&#x60; | User lock status equals value. | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - Locked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - Web access allowed&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - Web and mobile access allowed&lt;/li&gt;&lt;/ul&gt; | | **&#x60;isLocked&#x60;** | User lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectiveRoles&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE roles&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT roles&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE roles&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. user gets role **directly** granted from someone with _grant permission_ right.&lt;br&gt;EFFECTIVE means: e.g. user gets role through **group membership**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported. ### Example: &gt; &#x60;firstName:asc|lastLoginSuccessAt:desc&#x60;   Sort by &#x60;firstName&#x60; ascending **AND** by &#x60;lastLoginSuccessAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;login&#x60;** | User login | | **&#x60;firstName&#x60;** | User first name | | **&#x60;lastName&#x60;** | User last name | | **&#x60;gender&#x60;** | Gender | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status | | **&#x60;isLocked&#x60;** | User lock status | | **&#x60;lastLoginSuccessAt&#x60;** | Last successful login date | | **&#x60;expireAt&#x60;** | Expiration date |
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @param filter Filter string (optional)
   * @param includeAttributes Include custom user attributes. (optional)
     * @param limit Range limit (optional)
     * @param offset Range offset (optional)
     * @param sort Sort string (optional)
     * @return UserList
     * @throws ApiException if fails to make API call
     */
    public UserList getUsers(String xSdsAuthToken, String xSdsDateFormat, String filter, Boolean includeAttributes, Integer limit, Integer offset, String sort) throws ApiException {
        return getUsersWithHttpInfo(xSdsAuthToken, xSdsDateFormat, filter, includeAttributes, limit, offset, sort).getData();
    }

    /**
     * Get users
     * ### Functional Description:   Returns a list of DRACOON users.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information:   None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;login&#x60;**, **&#x60;firstName&#x60;** and  **&#x60;lastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    ### Example: &gt; &#x60;login:cn:searchString_1|firstName:cn:searchString_2|lockStatus:eq:2&#x60;   Filter users by login contains &#x60;searchString_1&#x60; **OR** firstName contains &#x60;searchString_2&#x60; **AND** those who are **NOT** locked.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;login&#x60;** | User login filter | &#x60;cn&#x60; | User login contains value. | &#x60;search String&#x60; | | **&#x60;firstName&#x60;** | User first name filter | &#x60;cn&#x60; | User first name contains value. | &#x60;search String&#x60; | | **&#x60;lastName&#x60;** | User last name filter | &#x60;cn&#x60; | User last name contains value. | &#x60;search String&#x60; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status filter | &#x60;eq&#x60; | User lock status equals value. | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - Locked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - Web access allowed&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - Web and mobile access allowed&lt;/li&gt;&lt;/ul&gt; | | **&#x60;isLocked&#x60;** | User lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectiveRoles&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE roles&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT roles&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE roles&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. user gets role **directly** granted from someone with _grant permission_ right.&lt;br&gt;EFFECTIVE means: e.g. user gets role through **group membership**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported. ### Example: &gt; &#x60;firstName:asc|lastLoginSuccessAt:desc&#x60;   Sort by &#x60;firstName&#x60; ascending **AND** by &#x60;lastLoginSuccessAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;login&#x60;** | User login | | **&#x60;firstName&#x60;** | User first name | | **&#x60;lastName&#x60;** | User last name | | **&#x60;gender&#x60;** | Gender | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status | | **&#x60;isLocked&#x60;** | User lock status | | **&#x60;lastLoginSuccessAt&#x60;** | Last successful login date | | **&#x60;expireAt&#x60;** | Expiration date |
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @param filter Filter string (optional)
     * @param includeAttributes Include custom user attributes. (optional)
     * @param limit Range limit (optional)
     * @param offset Range offset (optional)
     * @param sort Sort string (optional)
     * @return ApiResponse&lt;UserList&gt;
     * @throws ApiException if fails to make API call
     */
  public ApiResponse<UserList> getUsersWithHttpInfo(String xSdsAuthToken, String xSdsDateFormat, String filter, Boolean includeAttributes, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/users";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
      Map<String, String> localVarHeaderParams = new HashMap<String, String>();
      Map<String, Object> localVarFormParams = new HashMap<String, Object>();

      localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_attributes", includeAttributes));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
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

    GenericType<UserList> localVarReturnType = new GenericType<UserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }

    /**
     * Get rooms granted to the user or / and rooms that can be granted
     * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   Retrieves a list of rooms granted to the user and / or that can be granted.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:true|isLastAdmin:eq:true|name:cn:searchString&#x60;   Get all rooms that the user is granted **AND** is last admin **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Room name filter | &#x60;cn&#x60; | Room name contains value. | &#x60;search String&#x60; | | **&#x60;isGranted&#x60;** | Filter the rooms which the user is (not) granted. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;isLastAdmin&#x60;** | Filter the rooms which the user is last room administrator.&lt;br&gt;Only in connection with &#x60;isGranted:eq:true&#x60; filter possible. | &#x60;eq&#x60; |  | &#x60;true&#x60; | | **&#x60;effectivePerm&#x60;** | Filter rooms with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;: DIRECT **AND** EFFECTIVE **AND** OVER GROUP permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**.&lt;br&gt;OVER GROUP means: e.g. user gets &#x60;read&#x60; permissions on desired room through **group membership**. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;false&#x60; |
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @param filter Filter string (optional)
     * @param limit Range limit (optional)
     * @param offset Range offset (optional)
     * @return RoomTreeDataList
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public RoomTreeDataList getUsersRooms(Long userId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset) throws ApiException {
        return getUsersRoomsWithHttpInfo(userId, xSdsAuthToken, xSdsDateFormat, filter, limit, offset).getData();
    }

    /**
     * Get rooms granted to the user or / and rooms that can be granted
     * ## &amp;#9888; Deprecated since version 4.10.0  ### Functional Description:   Retrieves a list of rooms granted to the user and / or that can be granted.  ### Precondition: Right _\&quot;read users\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE&#x60;   Example: &gt; &#x60;isGranted:eq:true|isLastAdmin:eq:true|name:cn:searchString&#x60;   Get all rooms that the user is granted **AND** is last admin **AND** whose name is like &#x60;searchString&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Room name filter | &#x60;cn&#x60; | Room name contains value. | &#x60;search String&#x60; | | **&#x60;isGranted&#x60;** | Filter the rooms which the user is (not) granted. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;true&#x60; | | **&#x60;isLastAdmin&#x60;** | Filter the rooms which the user is last room administrator.&lt;br&gt;Only in connection with &#x60;isGranted:eq:true&#x60; filter possible. | &#x60;eq&#x60; |  | &#x60;true&#x60; | | **&#x60;effectivePerm&#x60;** | Filter rooms with DIRECT or DIRECT **AND** EFFECTIVE permissions&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT permissions&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE permissions&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;: DIRECT **AND** EFFECTIVE **AND** OVER GROUP permissions&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. room administrator grants &#x60;read&#x60; permissions to group of users **directly** on desired room.&lt;br&gt;EFFECTIVE means: e.g. group of users gets &#x60;read&#x60; permissions on desired room through **inheritance**.&lt;br&gt;OVER GROUP means: e.g. user gets &#x60;read&#x60; permissions on desired room through **group membership**. | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;true&#x60;&lt;/li&gt;&lt;li&gt;&#x60;false&#x60;&lt;/li&gt;&lt;li&gt;&#x60;any&#x60;&lt;/li&gt;&lt;/ul&gt;default: &#x60;false&#x60; |
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
   * @param filter Filter string (optional)
     * @param limit Range limit (optional)
     * @param offset Range offset (optional)
     * @return ApiResponse&lt;RoomTreeDataList&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
  @Deprecated
  public ApiResponse<RoomTreeDataList> getUsersRoomsWithHttpInfo(Long userId, String xSdsAuthToken, String xSdsDateFormat, String filter, Integer limit, Integer offset) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersRooms");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users/{user_id}/rooms"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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
     * Set custom user attributes
     * ### Functional Description:   Set custom user attributes.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: Custom user attributes gets set.  ### &amp;#9432; Further Information: Batch function.   All existing user attributes will be deleted.     * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
     * @param body body (required)
   * @param userId User ID (required)
   * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @return UserData
     * @throws ApiException if fails to make API call
     */
    public UserData setAllUserAttributes(UserAttributes body, Long userId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
        return setAllUserAttributesWithHttpInfo(body, userId, xSdsAuthToken, xSdsDateFormat).getData();
    }

    /**
     * Set custom user attributes
     * ### Functional Description:   Set custom user attributes.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: Custom user attributes gets set.  ### &amp;#9432; Further Information: Batch function.   All existing user attributes will be deleted.     * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
     * @param body body (required)
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @return ApiResponse&lt;UserData&gt;
     * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserData> setAllUserAttributesWithHttpInfo(UserAttributes body, Long userId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;

      // verify the required parameter 'body' is set
      if(body == null) {
          throw new ApiException(400, "Missing the required parameter 'body' when calling setAllUserAttributes");
      }
    
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling setAllUserAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/users/{user_id}/userAttributes"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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

    GenericType<UserData> localVarReturnType = new GenericType<UserData>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }

    /**
     * Add or edit custom user attributes
     * ### Functional Description:   Set custom user attributes.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: Custom user attributes gets added or edited.  ### &amp;#9432; Further Information: Batch function.   If an entry exists before, it will be overwritten.     * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
     * @param body body (required)
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @return UserData
     * @throws ApiException if fails to make API call
     */
    public UserData setUserAttributes(UserAttributes body, Long userId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
        return setUserAttributesWithHttpInfo(body, userId, xSdsAuthToken, xSdsDateFormat).getData();
    }

    /**
     * Add or edit custom user attributes
     * ### Functional Description:   Set custom user attributes.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: Custom user attributes gets added or edited.  ### &amp;#9432; Further Information: Batch function.   If an entry exists before, it will be overwritten.     * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
     * @param body body (required)
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @return ApiResponse&lt;UserData&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserData> setUserAttributesWithHttpInfo(UserAttributes body, Long userId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling setUserAttributes");
        }

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling setUserAttributes");
        }

        // create path and map variables
        String localVarPath = "/v4/users/{user_id}/userAttributes"
            .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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

        GenericType<UserData> localVarReturnType = new GenericType<UserData>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update user&#39;s metadata
     * ### Functional Description:   Update user&#39;s metadata.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: User&#39;s metadata is updated.  ### &amp;#9432; Further Information: * If a user should **NOT** expire, leave &#x60;expireAt&#x60; empty. * All input fields are limited to **150** characters * **All** characters are allowed.  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | none | none | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |
     * @param body body (required)
   * @param userId User ID (required)
   * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @return UserData
     * @throws ApiException if fails to make API call
     */
    public UserData updateUser(UpdateUserRequest body, Long userId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
        return updateUserWithHttpInfo(body, userId, xSdsAuthToken, xSdsDateFormat).getData();
    }

    /**
     * Update user&#39;s metadata
     * ### Functional Description:   Update user&#39;s metadata.  ### Precondition: Right _\&quot;change users\&quot;_ required.  ### Effects: User&#39;s metadata is updated.  ### &amp;#9432; Further Information: * If a user should **NOT** expire, leave &#x60;expireAt&#x60; empty. * All input fields are limited to **150** characters * **All** characters are allowed.  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | none | none | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |
     * @param body body (required)
     * @param userId User ID (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)) (optional)
     * @return ApiResponse&lt;UserData&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserData> updateUserWithHttpInfo(UpdateUserRequest body, Long userId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateUser");
        }

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updateUser");
        }

        // create path and map variables
        String localVarPath = "/v4/users/{user_id}"
            .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

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

        GenericType<UserData> localVarReturnType = new GenericType<UserData>() {};
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
