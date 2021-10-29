package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersFolderIdaddSharedLinkBody;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersFolderIdremoveSharedLinkBody;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersFolderIdupdateSharedLinkBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedLinksFoldersApi {
  private ApiClient apiClient;

  public SharedLinksFoldersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SharedLinksFoldersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get shared link for folder
   * Gets the information for a shared link on a folder.
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param fields Explicitly request the &#x60;shared_link&#x60; fields to be returned for this item. (required)
   * @return Folder
   * @throws ApiException if fails to make API call
   */
  public Folder getFoldersIdGetSharedLink(String folderId, String fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling getFoldersIdGetSharedLink");
    }
    // verify the required parameter 'fields' is set
    if (fields == null) {
      throw new ApiException(400, "Missing the required parameter 'fields' when calling getFoldersIdGetSharedLink");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}#get_shared_link"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fields", fields));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Folder> localVarReturnType = new GenericType<Folder>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Add shared link to folder
   * Adds a shared link to a folder.
   * @param fields Explicitly request the &#x60;shared_link&#x60; fields to be returned for this item. (required)
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param body  (optional)
   * @return Folder
   * @throws ApiException if fails to make API call
   */
  public Folder putFoldersIdAddSharedLink(String fields, String folderId, FoldersFolderIdaddSharedLinkBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fields' is set
    if (fields == null) {
      throw new ApiException(400, "Missing the required parameter 'fields' when calling putFoldersIdAddSharedLink");
    }
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling putFoldersIdAddSharedLink");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}#add_shared_link"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fields", fields));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Folder> localVarReturnType = new GenericType<Folder>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Remove shared link from folder
   * Removes a shared link from a folder.
   * @param fields Explicitly request the &#x60;shared_link&#x60; fields to be returned for this item. (required)
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param body  (optional)
   * @return Folder
   * @throws ApiException if fails to make API call
   */
  public Folder putFoldersIdRemoveSharedLink(String fields, String folderId, FoldersFolderIdremoveSharedLinkBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fields' is set
    if (fields == null) {
      throw new ApiException(400, "Missing the required parameter 'fields' when calling putFoldersIdRemoveSharedLink");
    }
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling putFoldersIdRemoveSharedLink");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}#remove_shared_link"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fields", fields));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Folder> localVarReturnType = new GenericType<Folder>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update shared link on folder
   * Updates a shared link on a folder.
   * @param fields Explicitly request the &#x60;shared_link&#x60; fields to be returned for this item. (required)
   * @param folderId The unique identifier that represent a folder.  The ID for any folder can be determined by visiting this folder in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/folder/123&#x60; the &#x60;folder_id&#x60; is &#x60;123&#x60;.  The root folder of a Box account is always represented by the ID &#x60;0&#x60;. (required)
   * @param body  (optional)
   * @return Folder
   * @throws ApiException if fails to make API call
   */
  public Folder putFoldersIdUpdateSharedLink(String fields, String folderId, FoldersFolderIdupdateSharedLinkBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fields' is set
    if (fields == null) {
      throw new ApiException(400, "Missing the required parameter 'fields' when calling putFoldersIdUpdateSharedLink");
    }
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling putFoldersIdUpdateSharedLink");
    }
    // create path and map variables
    String localVarPath = "/folders/{folder_id}#update_shared_link"
      .replaceAll("\\{" + "folder_id" + "\\}", apiClient.escapeString(folderId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fields", fields));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Folder> localVarReturnType = new GenericType<Folder>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
