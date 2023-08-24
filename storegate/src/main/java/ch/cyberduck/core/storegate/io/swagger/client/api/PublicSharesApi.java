package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.MediaItemContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.PublicShareInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class PublicSharesApi {
  private ApiClient apiClient;

  public PublicSharesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PublicSharesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Gets the contensts on a folder/file share. Use the path variable to traverse folders.
   * 
   * @param id The share id (required)
   * @param path Path inside the share (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Created, Modified, Size (desc/asc) (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeParent Include the parent (required)
   * @return FileContents
   * @throws ApiException if fails to make API call
   */
  public FileContents publicSharesGetPublicFileShareContents(String id, String path, Integer pageIndex, Integer pageSize, String sortExpression, Integer filter, Boolean includeParent) throws ApiException {
    return publicSharesGetPublicFileShareContentsWithHttpInfo(id, path, pageIndex, pageSize, sortExpression, filter, includeParent).getData();
      }

  /**
   * Gets the contensts on a folder/file share. Use the path variable to traverse folders.
   * 
   * @param id The share id (required)
   * @param path Path inside the share (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Created, Modified, Size (desc/asc) (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeParent Include the parent (required)
   * @return ApiResponse&lt;FileContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileContents> publicSharesGetPublicFileShareContentsWithHttpInfo(String id, String path, Integer pageIndex, Integer pageSize, String sortExpression, Integer filter, Boolean includeParent) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling publicSharesGetPublicFileShareContents");
    }
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling publicSharesGetPublicFileShareContents");
    }
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling publicSharesGetPublicFileShareContents");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling publicSharesGetPublicFileShareContents");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling publicSharesGetPublicFileShareContents");
    }
    
    // verify the required parameter 'filter' is set
    if (filter == null) {
      throw new ApiException(400, "Missing the required parameter 'filter' when calling publicSharesGetPublicFileShareContents");
    }
    
    // verify the required parameter 'includeParent' is set
    if (includeParent == null) {
      throw new ApiException(400, "Missing the required parameter 'includeParent' when calling publicSharesGetPublicFileShareContents");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/publicshares/{id}/files"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "path", path));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeParent", includeParent));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<FileContents> localVarReturnType = new GenericType<FileContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Gets the root folder/file on a share.
   * 
   * @param id The share id (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File publicSharesGetPublicFileShareFile(String id) throws ApiException {
    return publicSharesGetPublicFileShareFileWithHttpInfo(id).getData();
      }

  /**
   * Gets the root folder/file on a share.
   * 
   * @param id The share id (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> publicSharesGetPublicFileShareFileWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling publicSharesGetPublicFileShareFile");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/publicshares/{id}/file"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Gets the contensts on a media share.
   * 
   * @param id The share id (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Description, CreatedDate, ModifiedDate (desc/asc) (required)
   * @param includeParent Include album (required)
   * @return MediaItemContents
   * @throws ApiException if fails to make API call
   */
  public MediaItemContents publicSharesGetPublicMediaShareContents(String id, Integer pageIndex, Integer pageSize, String sortExpression, Boolean includeParent) throws ApiException {
    return publicSharesGetPublicMediaShareContentsWithHttpInfo(id, pageIndex, pageSize, sortExpression, includeParent).getData();
      }

  /**
   * Gets the contensts on a media share.
   * 
   * @param id The share id (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Description, CreatedDate, ModifiedDate (desc/asc) (required)
   * @param includeParent Include album (required)
   * @return ApiResponse&lt;MediaItemContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MediaItemContents> publicSharesGetPublicMediaShareContentsWithHttpInfo(String id, Integer pageIndex, Integer pageSize, String sortExpression, Boolean includeParent) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling publicSharesGetPublicMediaShareContents");
    }
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling publicSharesGetPublicMediaShareContents");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling publicSharesGetPublicMediaShareContents");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling publicSharesGetPublicMediaShareContents");
    }
    
    // verify the required parameter 'includeParent' is set
    if (includeParent == null) {
      throw new ApiException(400, "Missing the required parameter 'includeParent' when calling publicSharesGetPublicMediaShareContents");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/publicshares/{id}/media"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeParent", includeParent));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<MediaItemContents> localVarReturnType = new GenericType<MediaItemContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get public information for a share. No authorisation header needed.
   * 
   * @param id The share id, Guid or short string (required)
   * @return PublicShareInfo
   * @throws ApiException if fails to make API call
   */
  public PublicShareInfo publicSharesGetPublicShare(String id) throws ApiException {
    return publicSharesGetPublicShareWithHttpInfo(id).getData();
      }

  /**
   * Get public information for a share. No authorisation header needed.
   * 
   * @param id The share id, Guid or short string (required)
   * @return ApiResponse&lt;PublicShareInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicShareInfo> publicSharesGetPublicShareWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling publicSharesGetPublicShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/publicshares/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<PublicShareInfo> localVarReturnType = new GenericType<PublicShareInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Sends an email to the addresses entered on the share. Call this after a upload to the share.
   * 
   * @param id The share id (required)
   * @throws ApiException if fails to make API call
   */
  public void publicSharesSendUploadNotification(String id) throws ApiException {

    publicSharesSendUploadNotificationWithHttpInfo(id);
  }

  /**
   * Sends an email to the addresses entered on the share. Call this after a upload to the share.
   * 
   * @param id The share id (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> publicSharesSendUploadNotificationWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling publicSharesSendUploadNotification");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/publicshares/{id}/uploadnotification"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Validate password for shared with PasswordIDWithBankID, has brute force protection.
   * 
   * @param id The share id (required)
   * @param password The password as a body parameter (required)
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean publicSharesVerifyPassword(String id, String password) throws ApiException {
    return publicSharesVerifyPasswordWithHttpInfo(id, password).getData();
      }

  /**
   * Validate password for shared with PasswordIDWithBankID, has brute force protection.
   * 
   * @param id The share id (required)
   * @param password The password as a body parameter (required)
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> publicSharesVerifyPasswordWithHttpInfo(String id, String password) throws ApiException {
    Object localVarPostBody = password;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling publicSharesVerifyPassword");
    }
    
    // verify the required parameter 'password' is set
    if (password == null) {
      throw new ApiException(400, "Missing the required parameter 'password' when calling publicSharesVerifyPassword");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/publicshares/{id}/password"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
