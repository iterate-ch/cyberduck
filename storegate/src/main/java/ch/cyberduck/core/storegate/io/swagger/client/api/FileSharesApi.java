package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateFileShareRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileShare;
import ch.cyberduck.core.storegate.io.swagger.client.model.ShareContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.ShareMailRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateShareRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class FileSharesApi {
  private ApiClient apiClient;

  public FileSharesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FileSharesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete a file share.
   * 
   * @param id The share id (required)
   * @throws ApiException if fails to make API call
   */
  public void fileSharesDelete(String id) throws ApiException {

    fileSharesDeleteWithHttpInfo(id);
  }

  /**
   * Delete a file share.
   * 
   * @param id The share id (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> fileSharesDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling fileSharesDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares/{id}"
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


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List file, folder and media shares
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (default 500) (required)
   * @param sortExpression Name, Created, Modified, Size, Owner (desc/asc) (required)
   * @return ShareContents
   * @throws ApiException if fails to make API call
   */
  public ShareContents fileSharesGet(Integer pageIndex, Integer pageSize, String sortExpression) throws ApiException {
    return fileSharesGetWithHttpInfo(pageIndex, pageSize, sortExpression).getData();
      }

  /**
   * List file, folder and media shares
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (default 500) (required)
   * @param sortExpression Name, Created, Modified, Size, Owner (desc/asc) (required)
   * @return ApiResponse&lt;ShareContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ShareContents> fileSharesGetWithHttpInfo(Integer pageIndex, Integer pageSize, String sortExpression) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling fileSharesGet");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling fileSharesGet");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling fileSharesGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<ShareContents> localVarReturnType = new GenericType<ShareContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Return a FileShare with share information and file.
   * 
   * @param id The file id (required)
   * @return FileShare
   * @throws ApiException if fails to make API call
   */
  public FileShare fileSharesGetByFileId(String id) throws ApiException {
    return fileSharesGetByFileIdWithHttpInfo(id).getData();
      }

  /**
   * Return a FileShare with share information and file.
   * 
   * @param id The file id (required)
   * @return ApiResponse&lt;FileShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileShare> fileSharesGetByFileIdWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling fileSharesGetByFileId");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares/fileid/{id}"
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

    GenericType<FileShare> localVarReturnType = new GenericType<FileShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Return a FileShare with share information and file.
   * 
   * @param id The share id (required)
   * @return FileShare
   * @throws ApiException if fails to make API call
   */
  public FileShare fileSharesGetById(String id) throws ApiException {
    return fileSharesGetByIdWithHttpInfo(id).getData();
      }

  /**
   * Return a FileShare with share information and file.
   * 
   * @param id The share id (required)
   * @return ApiResponse&lt;FileShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileShare> fileSharesGetByIdWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling fileSharesGetById");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares/{id}"
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

    GenericType<FileShare> localVarReturnType = new GenericType<FileShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create a new file share without settings.
   * 
   * @param id The FileId (required)
   * @return FileShare
   * @throws ApiException if fails to make API call
   */
  public FileShare fileSharesPost(String id) throws ApiException {
    return fileSharesPostWithHttpInfo(id).getData();
      }

  /**
   * Create a new file share without settings.
   * 
   * @param id The FileId (required)
   * @return ApiResponse&lt;FileShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileShare> fileSharesPostWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling fileSharesPost");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares/fileid/{id}"
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

    GenericType<FileShare> localVarReturnType = new GenericType<FileShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create a new file share with settings.
   * 
   * @param createShareRequest The parameters (required)
   * @return FileShare
   * @throws ApiException if fails to make API call
   */
  public FileShare fileSharesPost_0(CreateFileShareRequest createShareRequest) throws ApiException {
    return fileSharesPost_0WithHttpInfo(createShareRequest).getData();
      }

  /**
   * Create a new file share with settings.
   * 
   * @param createShareRequest The parameters (required)
   * @return ApiResponse&lt;FileShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileShare> fileSharesPost_0WithHttpInfo(CreateFileShareRequest createShareRequest) throws ApiException {
    Object localVarPostBody = createShareRequest;
    
    // verify the required parameter 'createShareRequest' is set
    if (createShareRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createShareRequest' when calling fileSharesPost_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares";

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

    GenericType<FileShare> localVarReturnType = new GenericType<FileShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update a file share.
   * 
   * @param id The resource id (required)
   * @param updateFileShareRequest The parameters (required)
   * @return FileShare
   * @throws ApiException if fails to make API call
   */
  public FileShare fileSharesPut(String id, UpdateShareRequest updateFileShareRequest) throws ApiException {
    return fileSharesPutWithHttpInfo(id, updateFileShareRequest).getData();
      }

  /**
   * Update a file share.
   * 
   * @param id The resource id (required)
   * @param updateFileShareRequest The parameters (required)
   * @return ApiResponse&lt;FileShare&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileShare> fileSharesPutWithHttpInfo(String id, UpdateShareRequest updateFileShareRequest) throws ApiException {
    Object localVarPostBody = updateFileShareRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling fileSharesPut");
    }
    
    // verify the required parameter 'updateFileShareRequest' is set
    if (updateFileShareRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'updateFileShareRequest' when calling fileSharesPut");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares/{id}"
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

    GenericType<FileShare> localVarReturnType = new GenericType<FileShare>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Send a mail for a share. Max 20 recipients and 200 characters in message.
   * 
   * @param id The share id (required)
   * @param shareMailRequest The parameters (required)
   * @throws ApiException if fails to make API call
   */
  public void fileSharesSendShareMail(String id, ShareMailRequest shareMailRequest) throws ApiException {

    fileSharesSendShareMailWithHttpInfo(id, shareMailRequest);
  }

  /**
   * Send a mail for a share. Max 20 recipients and 200 characters in message.
   * 
   * @param id The share id (required)
   * @param shareMailRequest The parameters (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> fileSharesSendShareMailWithHttpInfo(String id, ShareMailRequest shareMailRequest) throws ApiException {
    Object localVarPostBody = shareMailRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling fileSharesSendShareMail");
    }
    
    // verify the required parameter 'shareMailRequest' is set
    if (shareMailRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'shareMailRequest' when calling fileSharesSendShareMail");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/fileshares/{id}/mail"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
