package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import org.joda.time.DateTime;
import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class FilesApi {
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

    deleteFilesPathWithHttpInfo(path, recursive);
  }

  /**
   * Delete file/folder
   * Delete file/folder
   * @param path Path to operate on. (required)
   * @param recursive If true, will recursively delete folers.  Otherwise, will error on non-empty folders. (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteFilesPathWithHttpInfo(String path, Boolean recursive) throws ApiException {
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
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
    return downloadWithHttpInfo(path, action, previewSize, withPreviews, withPriorityColor).getData();
      }

  /**
   * Download file
   * Download file
   * @param path Path to operate on. (required)
   * @param action Can be blank, &#x60;redirect&#x60; or &#x60;stat&#x60;.  If set to &#x60;stat&#x60;, we will return file information but without a download URL, and without logging a download.  If set to &#x60;redirect&#x60; we will serve a 302 redirect directly to the file.  This is used for integrations with Zapier, and is not recommended for most integrations. (optional)
   * @param previewSize Request a preview size.  Can be &#x60;small&#x60; (default), &#x60;large&#x60;, &#x60;xlarge&#x60;, or &#x60;pdf&#x60;. (optional)
   * @param withPreviews Include file preview information? (optional)
   * @param withPriorityColor Include file priority color information? (optional)
   * @return ApiResponse&lt;FileEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileEntity> downloadWithHttpInfo(String path, String action, String previewSize, Boolean withPreviews, Boolean withPriorityColor) throws ApiException {
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
   * @param providedMtime Modified time of file. (optional)
   * @param priorityColor Priority/Bookmark color of file. (optional)
   * @return FileEntity
   * @throws ApiException if fails to make API call
   */
  public FileEntity patchFilesPath(String path, DateTime providedMtime, String priorityColor) throws ApiException {
    return patchFilesPathWithHttpInfo(path, providedMtime, priorityColor).getData();
      }

  /**
   * Update file/folder metadata
   * Update file/folder metadata
   * @param path Path to operate on. (required)
   * @param providedMtime Modified time of file. (optional)
   * @param priorityColor Priority/Bookmark color of file. (optional)
   * @return ApiResponse&lt;FileEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileEntity> patchFilesPathWithHttpInfo(String path, DateTime providedMtime, String priorityColor) throws ApiException {
    Object localVarPostBody = null;
    
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


    
    if (providedMtime != null)
      localVarFormParams.put("provided_mtime", providedMtime);
if (priorityColor != null)
      localVarFormParams.put("priority_color", priorityColor);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<FileEntity> localVarReturnType = new GenericType<FileEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Upload file
   * Upload file
   * @param path Path to operate on. (required)
   * @param etagsEtag etag identifier. (required)
   * @param etagsPart Part number. (required)
   * @param action The action to perform.  Can be &#x60;append&#x60;, &#x60;attachment&#x60;, &#x60;end&#x60;, &#x60;upload&#x60;, &#x60;put&#x60;, or may not exist (optional)
   * @param length Length of file. (optional)
   * @param mkdirParents Create parent directories if they do not exist? (optional)
   * @param part Part if uploading a part. (optional)
   * @param parts How many parts to fetch? (optional)
   * @param providedMtime User provided modification time. (optional)
   * @param ref  (optional)
   * @param restart File byte offset to restart from. (optional)
   * @param size Size of file. (optional)
   * @param structure If copying folder, copy just the structure? (optional)
   * @param withRename Allow file rename instead of overwrite? (optional)
   * @return FileEntity
   * @throws ApiException if fails to make API call
   */
  public FileEntity postFilesPath(String path, List<String> etagsEtag, List<Integer> etagsPart, String action, Integer length, Boolean mkdirParents, Integer part, Integer parts, DateTime providedMtime, String ref, Integer restart, Integer size, String structure, Boolean withRename) throws ApiException {
    return postFilesPathWithHttpInfo(path, etagsEtag, etagsPart, action, length, mkdirParents, part, parts, providedMtime, ref, restart, size, structure, withRename).getData();
      }

  /**
   * Upload file
   * Upload file
   * @param path Path to operate on. (required)
   * @param etagsEtag etag identifier. (required)
   * @param etagsPart Part number. (required)
   * @param action The action to perform.  Can be &#x60;append&#x60;, &#x60;attachment&#x60;, &#x60;end&#x60;, &#x60;upload&#x60;, &#x60;put&#x60;, or may not exist (optional)
   * @param length Length of file. (optional)
   * @param mkdirParents Create parent directories if they do not exist? (optional)
   * @param part Part if uploading a part. (optional)
   * @param parts How many parts to fetch? (optional)
   * @param providedMtime User provided modification time. (optional)
   * @param ref  (optional)
   * @param restart File byte offset to restart from. (optional)
   * @param size Size of file. (optional)
   * @param structure If copying folder, copy just the structure? (optional)
   * @param withRename Allow file rename instead of overwrite? (optional)
   * @return ApiResponse&lt;FileEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileEntity> postFilesPathWithHttpInfo(String path, List<String> etagsEtag, List<Integer> etagsPart, String action, Integer length, Boolean mkdirParents, Integer part, Integer parts, DateTime providedMtime, String ref, Integer restart, Integer size, String structure, Boolean withRename) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling postFilesPath");
    }
    
    // verify the required parameter 'etagsEtag' is set
    if (etagsEtag == null) {
      throw new ApiException(400, "Missing the required parameter 'etagsEtag' when calling postFilesPath");
    }
    
    // verify the required parameter 'etagsPart' is set
    if (etagsPart == null) {
      throw new ApiException(400, "Missing the required parameter 'etagsPart' when calling postFilesPath");
    }
    
    // create path and map variables
    String localVarPath = "/files/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (action != null)
      localVarFormParams.put("action", action);
if (etagsEtag != null)
      localVarFormParams.put("etags[etag]", etagsEtag);
if (etagsPart != null)
      localVarFormParams.put("etags[part]", etagsPart);
if (length != null)
      localVarFormParams.put("length", length);
if (mkdirParents != null)
      localVarFormParams.put("mkdir_parents", mkdirParents);
if (part != null)
      localVarFormParams.put("part", part);
if (parts != null)
      localVarFormParams.put("parts", parts);
if (providedMtime != null)
      localVarFormParams.put("provided_mtime", providedMtime);
if (ref != null)
      localVarFormParams.put("ref", ref);
if (restart != null)
      localVarFormParams.put("restart", restart);
if (size != null)
      localVarFormParams.put("size", size);
if (structure != null)
      localVarFormParams.put("structure", structure);
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

    GenericType<FileEntity> localVarReturnType = new GenericType<FileEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
