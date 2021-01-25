package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body49;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class TransferFoldersApi {
  private ApiClient apiClient;

  public TransferFoldersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TransferFoldersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Transfer owned folders
   * Move all of the items (files, folders and workflows) owned by a user into another user&#x27;s account  Only the root folder (&#x60;0&#x60;) can be transferred.  Folders can only be moved across users by users with administrative permissions.  This call will be performed synchronously which might lead to a slow response when the source user has a large number of items in all of its folders.  If the destination path has a metadata cascade policy attached to any of the parent folders, a metadata cascade operation will be kicked off asynchronously.  There is currently no way to check for when this operation is finished.  The destination folder&#x27;s name will be in the format &#x60;{User}&#x27;s Files and Folders&#x60;, where &#x60;{User}&#x60; is the display name of the user.  To make this API call your application will need to have the \&quot;Read and write all files and folders stored in Box\&quot; scope enabled.  Please make sure the destination user has access to &#x60;Relay&#x60; or &#x60;Relay Lite&#x60;, and has access to the files and folders involved in the workflows being transferred.  Admins will receive an email when the operation is completed.
   * @param userId The ID of the user. (required)
   * @param body  (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @param notify Determines if users should receive email notification for the action performed. (optional)
   * @return Folder
   * @throws ApiException if fails to make API call
   */
  public Folder putUsersIdFolders0(String userId, Body49 body, List<String> fields, Boolean notify) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'userId' is set
    if (userId == null) {
      throw new ApiException(400, "Missing the required parameter 'userId' when calling putUsersIdFolders0");
    }
    // create path and map variables
    String localVarPath = "/users/{user_id}/folders/0"
      .replaceAll("\\{" + "user_id" + "\\}", apiClient.escapeString(userId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "notify", notify));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Folder> localVarReturnType = new GenericType<Folder>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
