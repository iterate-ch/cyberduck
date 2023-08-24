package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.FileLock;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileLockRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class FileLocksApi {
  private ApiClient apiClient;

  public FileLocksApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FileLocksApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create lock
   * 
   * @param fileId  (required)
   * @param request  (required)
   * @return FileLock
   * @throws ApiException if fails to make API call
   */
  public FileLock fileLocksCreateLock(String fileId, FileLockRequest request) throws ApiException {
    return fileLocksCreateLockWithHttpInfo(fileId, request).getData();
      }

  /**
   * Create lock
   * 
   * @param fileId  (required)
   * @param request  (required)
   * @return ApiResponse&lt;FileLock&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileLock> fileLocksCreateLockWithHttpInfo(String fileId, FileLockRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling fileLocksCreateLock");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling fileLocksCreateLock");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/filelocks/{fileId}"
      .replaceAll("\\{" + "fileId" + "\\}", apiClient.escapeString(fileId.toString()));

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

    GenericType<FileLock> localVarReturnType = new GenericType<FileLock>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete lock.
   * 
   * @param fileId  (required)
   * @param lockId  (required)
   * @throws ApiException if fails to make API call
   */
  public void fileLocksDeleteLock(String fileId, String lockId) throws ApiException {

    fileLocksDeleteLockWithHttpInfo(fileId, lockId);
  }

  /**
   * Delete lock.
   * 
   * @param fileId  (required)
   * @param lockId  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> fileLocksDeleteLockWithHttpInfo(String fileId, String lockId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling fileLocksDeleteLock");
    }
    
    // verify the required parameter 'lockId' is set
    if (lockId == null) {
      throw new ApiException(400, "Missing the required parameter 'lockId' when calling fileLocksDeleteLock");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/filelocks/{fileId}/{lockId}"
      .replaceAll("\\{" + "fileId" + "\\}", apiClient.escapeString(fileId.toString()))
      .replaceAll("\\{" + "lockId" + "\\}", apiClient.escapeString(lockId.toString()));

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
   * Get lock.
   * 
   * @param fileId  (required)
   * @return FileLock
   * @throws ApiException if fails to make API call
   */
  public FileLock fileLocksFindLock(String fileId) throws ApiException {
    return fileLocksFindLockWithHttpInfo(fileId).getData();
      }

  /**
   * Get lock.
   * 
   * @param fileId  (required)
   * @return ApiResponse&lt;FileLock&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileLock> fileLocksFindLockWithHttpInfo(String fileId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling fileLocksFindLock");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/filelocks/{fileId}"
      .replaceAll("\\{" + "fileId" + "\\}", apiClient.escapeString(fileId.toString()));

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

    GenericType<FileLock> localVarReturnType = new GenericType<FileLock>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update lock
   * 
   * @param fileId  (required)
   * @param lockId  (required)
   * @param request  (required)
   * @return FileLock
   * @throws ApiException if fails to make API call
   */
  public FileLock fileLocksUpdateLock(String fileId, String lockId, FileLockRequest request) throws ApiException {
    return fileLocksUpdateLockWithHttpInfo(fileId, lockId, request).getData();
      }

  /**
   * Update lock
   * 
   * @param fileId  (required)
   * @param lockId  (required)
   * @param request  (required)
   * @return ApiResponse&lt;FileLock&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileLock> fileLocksUpdateLockWithHttpInfo(String fileId, String lockId, FileLockRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling fileLocksUpdateLock");
    }
    
    // verify the required parameter 'lockId' is set
    if (lockId == null) {
      throw new ApiException(400, "Missing the required parameter 'lockId' when calling fileLocksUpdateLock");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling fileLocksUpdateLock");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/filelocks/{fileId}/{lockId}"
      .replaceAll("\\{" + "fileId" + "\\}", apiClient.escapeString(fileId.toString()))
      .replaceAll("\\{" + "lockId" + "\\}", apiClient.escapeString(lockId.toString()));

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

    GenericType<FileLock> localVarReturnType = new GenericType<FileLock>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
