package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.FileActionEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FileUploadPartEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class FileActionsApi {
  private ApiClient apiClient;

  public FileActionsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FileActionsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Begin file upload
   * Begin file upload
   * @param path Path to operate on. (required)
   * @param mkdirParents Create parent directories if they do not exist? (optional)
   * @param part Part if uploading a part. (optional)
   * @param parts How many parts to fetch? (optional)
   * @param ref  (optional)
   * @param restart File byte offset to restart from. (optional)
   * @param withRename Allow file rename instead of overwrite? (optional)
   * @return List&lt;FileUploadPartEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<FileUploadPartEntity> beginUpload(String path, Boolean mkdirParents, Integer part, Integer parts, String ref, Integer restart, Boolean withRename) throws ApiException {
    return beginUploadWithHttpInfo(path, mkdirParents, part, parts, ref, restart, withRename).getData();
      }

  /**
   * Begin file upload
   * Begin file upload
   * @param path Path to operate on. (required)
   * @param mkdirParents Create parent directories if they do not exist? (optional)
   * @param part Part if uploading a part. (optional)
   * @param parts How many parts to fetch? (optional)
   * @param ref  (optional)
   * @param restart File byte offset to restart from. (optional)
   * @param withRename Allow file rename instead of overwrite? (optional)
   * @return ApiResponse&lt;List&lt;FileUploadPartEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<FileUploadPartEntity>> beginUploadWithHttpInfo(String path, Boolean mkdirParents, Integer part, Integer parts, String ref, Integer restart, Boolean withRename) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling beginUpload");
    }
    
    // create path and map variables
    String localVarPath = "/file_actions/begin_upload/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (mkdirParents != null)
      localVarFormParams.put("mkdir_parents", mkdirParents);
if (part != null)
      localVarFormParams.put("part", part);
if (parts != null)
      localVarFormParams.put("parts", parts);
if (ref != null)
      localVarFormParams.put("ref", ref);
if (restart != null)
      localVarFormParams.put("restart", restart);
if (withRename != null)
      localVarFormParams.put("with_rename", withRename);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<FileUploadPartEntity>> localVarReturnType = new GenericType<List<FileUploadPartEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Copy file/folder
   * Copy file/folder
   * @param path Path to operate on. (required)
   * @param destination Copy destination path. (required)
   * @param structure Copy structure only? (optional)
   * @return FileActionEntity
   * @throws ApiException if fails to make API call
   */
  public FileActionEntity copy(String path, String destination, Boolean structure) throws ApiException {
    return copyWithHttpInfo(path, destination, structure).getData();
      }

  /**
   * Copy file/folder
   * Copy file/folder
   * @param path Path to operate on. (required)
   * @param destination Copy destination path. (required)
   * @param structure Copy structure only? (optional)
   * @return ApiResponse&lt;FileActionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileActionEntity> copyWithHttpInfo(String path, String destination, Boolean structure) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling copy");
    }
    
    // verify the required parameter 'destination' is set
    if (destination == null) {
      throw new ApiException(400, "Missing the required parameter 'destination' when calling copy");
    }
    
    // create path and map variables
    String localVarPath = "/file_actions/copy/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (destination != null)
      localVarFormParams.put("destination", destination);
if (structure != null)
      localVarFormParams.put("structure", structure);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileActionEntity> localVarReturnType = new GenericType<FileActionEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Move file/folder
   * Move file/folder
   * @param path Path to operate on. (required)
   * @param destination Move destination path. (required)
   * @return FileActionEntity
   * @throws ApiException if fails to make API call
   */
  public FileActionEntity move(String path, String destination) throws ApiException {
    return moveWithHttpInfo(path, destination).getData();
      }

  /**
   * Move file/folder
   * Move file/folder
   * @param path Path to operate on. (required)
   * @param destination Move destination path. (required)
   * @return ApiResponse&lt;FileActionEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileActionEntity> moveWithHttpInfo(String path, String destination) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling move");
    }
    
    // verify the required parameter 'destination' is set
    if (destination == null) {
      throw new ApiException(400, "Missing the required parameter 'destination' when calling move");
    }
    
    // create path and map variables
    String localVarPath = "/file_actions/move/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (destination != null)
      localVarFormParams.put("destination", destination);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileActionEntity> localVarReturnType = new GenericType<FileActionEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
