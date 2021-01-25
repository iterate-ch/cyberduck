package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body54;
import ch.cyberduck.core.box.io.swagger.client.model.Body55;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.GroupMembership;
import ch.cyberduck.core.box.io.swagger.client.model.GroupMemberships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class GroupMembershipsApi {
  private ApiClient apiClient;

  public GroupMembershipsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public GroupMembershipsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove user from group
   * Deletes a specific group membership. Only admins of this group or users with admin-level permissions will be able to use this API.
   * @param groupMembershipId The ID of the group membership. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteGroupMembershipsId(String groupMembershipId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'groupMembershipId' is set
    if (groupMembershipId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupMembershipId' when calling deleteGroupMembershipsId");
    }
    // create path and map variables
    String localVarPath = "/group_memberships/{group_membership_id}"
      .replaceAll("\\{" + "group_membership_id" + "\\}", apiClient.escapeString(groupMembershipId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get group membership
   * Retrieves a specific group membership. Only admins of this group or users with admin-level permissions will be able to use this API.
   * @param groupMembershipId The ID of the group membership. (required)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return GroupMembership
   * @throws ApiException if fails to make API call
   */
  public GroupMembership getGroupMembershipsId(String groupMembershipId, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'groupMembershipId' is set
    if (groupMembershipId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupMembershipId' when calling getGroupMembershipsId");
    }
    // create path and map variables
    String localVarPath = "/group_memberships/{group_membership_id}"
      .replaceAll("\\{" + "group_membership_id" + "\\}", apiClient.escapeString(groupMembershipId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<GroupMembership> localVarReturnType = new GenericType<GroupMembership>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List members of group
   * Retrieves all the members for a group. Only members of this group or users with admin-level permissions will be able to use this API.
   * @param groupId The ID of the group. (required)
   * @param limit The maximum number of items to return per page. (optional)
   * @param offset The offset of the item at which to begin the response. (optional, default to 0)
   * @return GroupMemberships
   * @throws ApiException if fails to make API call
   */
  public GroupMemberships getGroupsIdMemberships(String groupId, Long limit, Long offset) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'groupId' is set
    if (groupId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupId' when calling getGroupsIdMemberships");
    }
    // create path and map variables
    String localVarPath = "/groups/{group_id}/memberships"
      .replaceAll("\\{" + "group_id" + "\\}", apiClient.escapeString(groupId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<GroupMemberships> localVarReturnType = new GenericType<GroupMemberships>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List user&#x27;s groups
   * Retrieves all the groups for a user. Only members of this group or users with admin-level permissions will be able to use this API.
   * @param userId The ID of the user. (required)
   * @param limit The maximum number of items to return per page. (optional)
   * @param offset The offset of the item at which to begin the response. (optional, default to 0)
   * @return GroupMemberships
   * @throws ApiException if fails to make API call
   */
  public GroupMemberships getUsersIdMemberships(String userId, Long limit, Long offset) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling getUsersIdMemberships");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/memberships"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<GroupMemberships> localVarReturnType = new GenericType<GroupMemberships>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Add user to group
   * Creates a group membership. Only users with admin-level permissions will be able to use this API.
   * @param body  (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return GroupMembership
   * @throws ApiException if fails to make API call
   */
  public GroupMembership postGroupMemberships(Body54 body, List<String> fields) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/group_memberships";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<GroupMembership> localVarReturnType = new GenericType<GroupMembership>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update group membership
   * Updates a user&#x27;s group membership. Only admins of this group or users with admin-level permissions will be able to use this API.
   * @param groupMembershipId The ID of the group membership. (required)
   * @param body  (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return GroupMembership
   * @throws ApiException if fails to make API call
   */
  public GroupMembership putGroupMembershipsId(String groupMembershipId, Body55 body, List<String> fields) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'groupMembershipId' is set
    if (groupMembershipId == null) {
      throw new ApiException(400, "Missing the required parameter 'groupMembershipId' when calling putGroupMembershipsId");
    }
    // create path and map variables
    String localVarPath = "/group_memberships/{group_membership_id}"
      .replaceAll("\\{" + "group_membership_id" + "\\}", apiClient.escapeString(groupMembershipId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<GroupMembership> localVarReturnType = new GenericType<GroupMembership>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
