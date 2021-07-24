package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FilesPathBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-12T12:23:43.971535+02:00[Europe/Paris]")public class FilesApi {
  private ApiClient apiClient;

  public FilesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FilesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete file/folder
   * Delete file/folder
   * @param path Path to operate on. (required)
   * @param recursive If true, will recursively delete folers.  Otherwise, will error on non-empty folders. (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteFilesPath(String path, Boolean recursive) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling deleteFilesPath");
    }
    // create path and map variables
    String localVarPath = "/files/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "recursive", recursive));


    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Download file
   * Download file
   * @param path Path to operate on. (required)
   * @param action Can be blank, &#x60;redirect&#x60; or &#x60;stat&#x60;.  If set to &#x60;stat&#x60;, we will return file information but without a download URL, and without logging a download.  If set to &#x60;redirect&#x60; we will serve a 302 redirect directly to the file.  This is used for integrations with Zapier, and is not recommended for most integrations. (optional)
   * @param previewSize Request a preview size.  Can be &#x60;small&#x60; (default), &#x60;large&#x60;, &#x60;xlarge&#x60;, or &#x60;pdf&#x60;. (optional)
   * @param withPreviews Include file preview information? (optional)
   * @param withPriorityColor Include file priority color information? (optional)
   * @return FileEntity
   * @throws ApiException if fails to make API call
   */
  public FileEntity download(String path, String action, String previewSize, Boolean withPreviews, Boolean withPriorityColor) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling download");
    }
    // create path and map variables
    String localVarPath = "/files/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "action", action));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "preview_size", previewSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "with_previews", withPreviews));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "with_priority_color", withPriorityColor));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileEntity> localVarReturnType = new GenericType<FileEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update file/folder metadata
   * Update file/folder metadata
   * @param path Path to operate on. (required)
   * @param body  (optional)
   * @return FileEntity
   * @throws ApiException if fails to make API call
   */
  public FileEntity patchFilesPath(String path, FilesPathBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling patchFilesPath");
    }
    // create path and map variables
    String localVarPath = "/files/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileEntity> localVarReturnType = new GenericType<FileEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Upload file
   * Upload file
   * @param body  (required)
   * @param path Path to operate on. (required)
   * @return FileEntity
   * @throws ApiException if fails to make API call
   */
  public FileEntity postFilesPath(FilesPathBody body, String path) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postFilesPath");
    }
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling postFilesPath");
    }
    // create path and map variables
    String localVarPath = "/files/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileEntity> localVarReturnType = new GenericType<FileEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
