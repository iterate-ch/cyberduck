package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CopyFileRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.CreateOfficeRequest;
import org.joda.time.DateTime;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileVersion;
import ch.cyberduck.core.storegate.io.swagger.client.model.IndexContent;
import ch.cyberduck.core.storegate.io.swagger.client.model.MoveFileRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.NameRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.RecentGroupedContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.SearchFileContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateFilePropertiesRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
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
   * Copy a resource with overwrite mode option.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The id of the reaource to copy (required)
   * @param copyFileRequest The request data (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File filesCopy(String id, CopyFileRequest copyFileRequest) throws ApiException {
    return filesCopyWithHttpInfo(id, copyFileRequest).getData();
      }

  /**
   * Copy a resource with overwrite mode option.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The id of the reaource to copy (required)
   * @param copyFileRequest The request data (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> filesCopyWithHttpInfo(String id, CopyFileRequest copyFileRequest) throws ApiException {
    Object localVarPostBody = copyFileRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesCopy");
    }
    
    // verify the required parameter 'copyFileRequest' is set
    if (copyFileRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'copyFileRequest' when calling filesCopy");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/copy"
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

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates a new file of type folder.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param createFolderRequest createFolderRequest (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File filesCreateFolder(CreateFolderRequest createFolderRequest) throws ApiException {
    return filesCreateFolderWithHttpInfo(createFolderRequest).getData();
      }

  /**
   * Creates a new file of type folder.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param createFolderRequest createFolderRequest (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> filesCreateFolderWithHttpInfo(CreateFolderRequest createFolderRequest) throws ApiException {
    Object localVarPostBody = createFolderRequest;
    
    // verify the required parameter 'createFolderRequest' is set
    if (createFolderRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createFolderRequest' when calling filesCreateFolder");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files";

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

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create an empty Office file
   * 
   * @param createOfficeRequest createOfficeRequest (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File filesCreateOfficeFile(CreateOfficeRequest createOfficeRequest) throws ApiException {
    return filesCreateOfficeFileWithHttpInfo(createOfficeRequest).getData();
      }

  /**
   * Create an empty Office file
   * 
   * @param createOfficeRequest createOfficeRequest (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> filesCreateOfficeFileWithHttpInfo(CreateOfficeRequest createOfficeRequest) throws ApiException {
    Object localVarPostBody = createOfficeRequest;
    
    // verify the required parameter 'createOfficeRequest' is set
    if (createOfficeRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createOfficeRequest' when calling filesCreateOfficeFile");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/office";

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

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Deletes a directory or file.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The resource to delete. (required)
   * @throws ApiException if fails to make API call
   */
  public void filesDelete(String id) throws ApiException {

    filesDeleteWithHttpInfo(id);
  }

  /**
   * Deletes a directory or file.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The resource to delete. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> filesDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}"
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
   * Deletes all chindren in a directory.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The resource to delete children in. (required)
   * @throws ApiException if fails to make API call
   */
  public void filesDeleteContents(String id) throws ApiException {

    filesDeleteContentsWithHttpInfo(id);
  }

  /**
   * Deletes all chindren in a directory.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The resource to delete children in. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> filesDeleteContentsWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesDeleteContents");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/contents"
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
   * Deletes a file version.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The file to delete version from. (required)
   * @param fileVersion What version to delete. (required)
   * @throws ApiException if fails to make API call
   */
  public void filesDelete_0(String id, Integer fileVersion) throws ApiException {

    filesDelete_0WithHttpInfo(id, fileVersion);
  }

  /**
   * Deletes a file version.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The file to delete version from. (required)
   * @param fileVersion What version to delete. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> filesDelete_0WithHttpInfo(String id, Integer fileVersion) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesDelete_0");
    }
    
    // verify the required parameter 'fileVersion' is set
    if (fileVersion == null) {
      throw new ApiException(400, "Missing the required parameter 'fileVersion' when calling filesDelete_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/versions/{fileVersion}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()))
      .replaceAll("\\{" + "fileVersion" + "\\}", apiClient.escapeString(fileVersion.toString()));

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
   * Gets a file by id
   * 
   * @param id  (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File filesGet(String id) throws ApiException {
    return filesGetWithHttpInfo(id).getData();
      }

  /**
   * Gets a file by id
   * 
   * @param id  (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> filesGetWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}"
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
   * Browse fileContents.
   * 
   * @param id The id to get filecontents from (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression \&quot;Name desc\&quot;  is acceptable Name, Created, Modified, Size (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeHidden Include hidden folders and files (required)
   * @param includeParent Include parent in the response (File) if set to true (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @return FileContents
   * @throws ApiException if fails to make API call
   */
  public FileContents filesGetById(String id, Integer pageIndex, Integer pageSize, String sortExpression, Integer filter, Boolean includeHidden, Boolean includeParent, Boolean sortTogether) throws ApiException {
    return filesGetByIdWithHttpInfo(id, pageIndex, pageSize, sortExpression, filter, includeHidden, includeParent, sortTogether).getData();
      }

  /**
   * Browse fileContents.
   * 
   * @param id The id to get filecontents from (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression \&quot;Name desc\&quot;  is acceptable Name, Created, Modified, Size (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeHidden Include hidden folders and files (required)
   * @param includeParent Include parent in the response (File) if set to true (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @return ApiResponse&lt;FileContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileContents> filesGetByIdWithHttpInfo(String id, Integer pageIndex, Integer pageSize, String sortExpression, Integer filter, Boolean includeHidden, Boolean includeParent, Boolean sortTogether) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetById");
    }
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling filesGetById");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling filesGetById");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling filesGetById");
    }
    
    // verify the required parameter 'filter' is set
    if (filter == null) {
      throw new ApiException(400, "Missing the required parameter 'filter' when calling filesGetById");
    }
    
    // verify the required parameter 'includeHidden' is set
    if (includeHidden == null) {
      throw new ApiException(400, "Missing the required parameter 'includeHidden' when calling filesGetById");
    }
    
    // verify the required parameter 'includeParent' is set
    if (includeParent == null) {
      throw new ApiException(400, "Missing the required parameter 'includeParent' when calling filesGetById");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/contents"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeHidden", includeHidden));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeParent", includeParent));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortTogether", sortTogether));

    
    
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
   * Get folder size
   * 
   * @param id  (required)
   * @return Long
   * @throws ApiException if fails to make API call
   */
  public Long filesGetFolderSize(String id) throws ApiException {
    return filesGetFolderSizeWithHttpInfo(id).getData();
      }

  /**
   * Get folder size
   * 
   * @param id  (required)
   * @return ApiResponse&lt;Long&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Long> filesGetFolderSizeWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetFolderSize");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/size"
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

    GenericType<Long> localVarReturnType = new GenericType<Long>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get index in fileContents.
   * 
   * @param parentId The id to get filecontents from (required)
   * @param id The id to get index for (required)
   * @param sortExpression \&quot;Name desc\&quot;  is acceptable Name, Created, Modified, Size (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeHidden Include hidden folders and files (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @return IndexContent
   * @throws ApiException if fails to make API call
   */
  public IndexContent filesGetIndexById(String parentId, String id, String sortExpression, Integer filter, Boolean includeHidden, Boolean sortTogether) throws ApiException {
    return filesGetIndexByIdWithHttpInfo(parentId, id, sortExpression, filter, includeHidden, sortTogether).getData();
      }

  /**
   * Get index in fileContents.
   * 
   * @param parentId The id to get filecontents from (required)
   * @param id The id to get index for (required)
   * @param sortExpression \&quot;Name desc\&quot;  is acceptable Name, Created, Modified, Size (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeHidden Include hidden folders and files (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @return ApiResponse&lt;IndexContent&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<IndexContent> filesGetIndexByIdWithHttpInfo(String parentId, String id, String sortExpression, Integer filter, Boolean includeHidden, Boolean sortTogether) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'parentId' is set
    if (parentId == null) {
      throw new ApiException(400, "Missing the required parameter 'parentId' when calling filesGetIndexById");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetIndexById");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling filesGetIndexById");
    }
    
    // verify the required parameter 'filter' is set
    if (filter == null) {
      throw new ApiException(400, "Missing the required parameter 'filter' when calling filesGetIndexById");
    }
    
    // verify the required parameter 'includeHidden' is set
    if (includeHidden == null) {
      throw new ApiException(400, "Missing the required parameter 'includeHidden' when calling filesGetIndexById");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{parentId}/contents/index/{id}"
      .replaceAll("\\{" + "parentId" + "\\}", apiClient.escapeString(parentId.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeHidden", includeHidden));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortTogether", sortTogether));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<IndexContent> localVarReturnType = new GenericType<IndexContent>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get index in recent contents from entire space.
   * 
   * @param id The id to get index for (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, 4&#x3D;Video, 5&#x3D;Media, 6&#x3D;Files (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param sortExpression Sort expression (optional)
   * @param reversed Reverse list with oldest first (Obsolete, only used if sortExpression is empty) (optional)
   * @param rootFolder Root folder where recent should be found, if null it will search entire account (optional)
   * @return IndexContent
   * @throws ApiException if fails to make API call
   */
  public IndexContent filesGetIndexRecent(String id, Integer filter, String sortExpression, Boolean reversed, String rootFolder) throws ApiException {
    return filesGetIndexRecentWithHttpInfo(id, filter, sortExpression, reversed, rootFolder).getData();
      }

  /**
   * Get index in recent contents from entire space.
   * 
   * @param id The id to get index for (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, 4&#x3D;Video, 5&#x3D;Media, 6&#x3D;Files (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param sortExpression Sort expression (optional)
   * @param reversed Reverse list with oldest first (Obsolete, only used if sortExpression is empty) (optional)
   * @param rootFolder Root folder where recent should be found, if null it will search entire account (optional)
   * @return ApiResponse&lt;IndexContent&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<IndexContent> filesGetIndexRecentWithHttpInfo(String id, Integer filter, String sortExpression, Boolean reversed, String rootFolder) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetIndexRecent");
    }
    
    // verify the required parameter 'filter' is set
    if (filter == null) {
      throw new ApiException(400, "Missing the required parameter 'filter' when calling filesGetIndexRecent");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/recent/index/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "reversed", reversed));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "rootFolder", rootFolder));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<IndexContent> localVarReturnType = new GenericType<IndexContent>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * 
   * @param id  (required)
   * @param edit  (optional)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String filesGetOfficeUrl(String id, Boolean edit) throws ApiException {
    return filesGetOfficeUrlWithHttpInfo(id, edit).getData();
      }

  /**
   * 
   * 
   * @param id  (required)
   * @param edit  (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> filesGetOfficeUrlWithHttpInfo(String id, Boolean edit) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetOfficeUrl");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/officeurl"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "edit", edit));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * 
   * @param id  (required)
   * @param request  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String filesGetOfficeUrl2(String id, NameRequest request) throws ApiException {
    return filesGetOfficeUrl2WithHttpInfo(id, request).getData();
      }

  /**
   * 
   * 
   * @param id  (required)
   * @param request  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> filesGetOfficeUrl2WithHttpInfo(String id, NameRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetOfficeUrl2");
    }
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling filesGetOfficeUrl2");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/officeurl/template"
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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Gets a list of random photos from a specific folder.
   * 
   * @param id The id of the reaource (required)
   * @param numberOfPhotos Number of photos (optional)
   * @return List&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public List<File> filesGetRandomPhotos(String id, Integer numberOfPhotos) throws ApiException {
    return filesGetRandomPhotosWithHttpInfo(id, numberOfPhotos).getData();
      }

  /**
   * Gets a list of random photos from a specific folder.
   * 
   * @param id The id of the reaource (required)
   * @param numberOfPhotos Number of photos (optional)
   * @return ApiResponse&lt;List&lt;File&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<File>> filesGetRandomPhotosWithHttpInfo(String id, Integer numberOfPhotos) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetRandomPhotos");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/randomphotos"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "numberOfPhotos", numberOfPhotos));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<File>> localVarReturnType = new GenericType<List<File>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * This lists grouped recent contents from entire space.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, 4&#x3D;Video, 5&#x3D;Media, 6&#x3D;Files (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param sortExpression Sort expression (optional)
   * @param reversed Reverse list with oldest first (Obsolete, only used if sortExpression is empty) (optional)
   * @param rootFolder Root folder where recent should be found, if null it will search entire account (optional)
   * @return SearchFileContents
   * @throws ApiException if fails to make API call
   */
  public SearchFileContents filesGetRecent(Integer pageIndex, Integer pageSize, Integer filter, String sortExpression, Boolean reversed, String rootFolder) throws ApiException {
    return filesGetRecentWithHttpInfo(pageIndex, pageSize, filter, sortExpression, reversed, rootFolder).getData();
      }

  /**
   * This lists grouped recent contents from entire space.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, 4&#x3D;Video, 5&#x3D;Media, 6&#x3D;Files (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param sortExpression Sort expression (optional)
   * @param reversed Reverse list with oldest first (Obsolete, only used if sortExpression is empty) (optional)
   * @param rootFolder Root folder where recent should be found, if null it will search entire account (optional)
   * @return ApiResponse&lt;SearchFileContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SearchFileContents> filesGetRecentWithHttpInfo(Integer pageIndex, Integer pageSize, Integer filter, String sortExpression, Boolean reversed, String rootFolder) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling filesGetRecent");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling filesGetRecent");
    }
    
    // verify the required parameter 'filter' is set
    if (filter == null) {
      throw new ApiException(400, "Missing the required parameter 'filter' when calling filesGetRecent");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/recent";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "reversed", reversed));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "rootFolder", rootFolder));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<SearchFileContents> localVarReturnType = new GenericType<SearchFileContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * This lists recent grouped contents from the entire space.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, 4&#x3D;Video, 5&#x3D;Media, 6&#x3D;Files (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param groupBy Day, Month (0 &#x3D; Day, 1 &#x3D; Month) (required)
   * @param sortExpression Sort expression (optional)
   * @param reversed Reverse list with oldest first (Obsolete, only used if sortExpression is empty) (optional)
   * @param rootFolder Root folder where recent should be found, if null it will search entire account (optional)
   * @return RecentGroupedContents
   * @throws ApiException if fails to make API call
   */
  public RecentGroupedContents filesGetRecentGroupes(Integer pageIndex, Integer pageSize, Integer filter, Integer groupBy, String sortExpression, Boolean reversed, String rootFolder) throws ApiException {
    return filesGetRecentGroupesWithHttpInfo(pageIndex, pageSize, filter, groupBy, sortExpression, reversed, rootFolder).getData();
      }

  /**
   * This lists recent grouped contents from the entire space.
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, 4&#x3D;Video, 5&#x3D;Media, 6&#x3D;Files (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param groupBy Day, Month (0 &#x3D; Day, 1 &#x3D; Month) (required)
   * @param sortExpression Sort expression (optional)
   * @param reversed Reverse list with oldest first (Obsolete, only used if sortExpression is empty) (optional)
   * @param rootFolder Root folder where recent should be found, if null it will search entire account (optional)
   * @return ApiResponse&lt;RecentGroupedContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RecentGroupedContents> filesGetRecentGroupesWithHttpInfo(Integer pageIndex, Integer pageSize, Integer filter, Integer groupBy, String sortExpression, Boolean reversed, String rootFolder) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling filesGetRecentGroupes");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling filesGetRecentGroupes");
    }
    
    // verify the required parameter 'filter' is set
    if (filter == null) {
      throw new ApiException(400, "Missing the required parameter 'filter' when calling filesGetRecentGroupes");
    }
    
    // verify the required parameter 'groupBy' is set
    if (groupBy == null) {
      throw new ApiException(400, "Missing the required parameter 'groupBy' when calling filesGetRecentGroupes");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/recent/groups";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "groupBy", groupBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "reversed", reversed));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "rootFolder", rootFolder));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RecentGroupedContents> localVarReturnType = new GenericType<RecentGroupedContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Gets all versions on a file.
   * 
   * @param id The file to delete version from. (required)
   * @return List&lt;FileVersion&gt;
   * @throws ApiException if fails to make API call
   */
  public List<FileVersion> filesGetResourceVersions(String id) throws ApiException {
    return filesGetResourceVersionsWithHttpInfo(id).getData();
      }

  /**
   * Gets all versions on a file.
   * 
   * @param id The file to delete version from. (required)
   * @return ApiResponse&lt;List&lt;FileVersion&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<FileVersion>> filesGetResourceVersionsWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesGetResourceVersions");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/versions"
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

    GenericType<List<FileVersion>> localVarReturnType = new GenericType<List<FileVersion>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Gets a file by path.
   * 
   * @param path The path to the file to get, this path should be URL encoded (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File filesGet_0(String path) throws ApiException {
    return filesGet_0WithHttpInfo(path).getData();
      }

  /**
   * Gets a file by path.
   * 
   * @param path The path to the file to get, this path should be URL encoded (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> filesGet_0WithHttpInfo(String path) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling filesGet_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "path", path));

    
    
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
   * Browse fileContents.
   * 
   * @param path The path to list filecontents from. \&quot;/\&quot; lists the root (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression \&quot;Name desc\&quot;  is acceptable Name, Created, Modified, Size (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeHidden Include hidden folders and files (required)
   * @param includeParent Include parent in the response (File) if set to true (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @return FileContents
   * @throws ApiException if fails to make API call
   */
  public FileContents filesGet_1(String path, Integer pageIndex, Integer pageSize, String sortExpression, Integer filter, Boolean includeHidden, Boolean includeParent, Boolean sortTogether) throws ApiException {
    return filesGet_1WithHttpInfo(path, pageIndex, pageSize, sortExpression, filter, includeHidden, includeParent, sortTogether).getData();
      }

  /**
   * Browse fileContents.
   * 
   * @param path The path to list filecontents from. \&quot;/\&quot; lists the root (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression \&quot;Name desc\&quot;  is acceptable Name, Created, Modified, Size (required)
   * @param filter 0&#x3D;All, 1&#x3D;Folder, 2&#x3D;Image, 3&#x3D;Doc, Video&#x3D;4, Media&#x3D;5, Files&#x3D;6 (0 &#x3D; All, 1 &#x3D; Folder, 2 &#x3D; Image, 3 &#x3D; Doc, 4 &#x3D; Video, 5 &#x3D; Media, 6 &#x3D; Files) (required)
   * @param includeHidden Include hidden folders and files (required)
   * @param includeParent Include parent in the response (File) if set to true (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @return ApiResponse&lt;FileContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileContents> filesGet_1WithHttpInfo(String path, Integer pageIndex, Integer pageSize, String sortExpression, Integer filter, Boolean includeHidden, Boolean includeParent, Boolean sortTogether) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling filesGet_1");
    }
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling filesGet_1");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling filesGet_1");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling filesGet_1");
    }
    
    // verify the required parameter 'filter' is set
    if (filter == null) {
      throw new ApiException(400, "Missing the required parameter 'filter' when calling filesGet_1");
    }
    
    // verify the required parameter 'includeHidden' is set
    if (includeHidden == null) {
      throw new ApiException(400, "Missing the required parameter 'includeHidden' when calling filesGet_1");
    }
    
    // verify the required parameter 'includeParent' is set
    if (includeParent == null) {
      throw new ApiException(400, "Missing the required parameter 'includeParent' when calling filesGet_1");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/contents";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "path", path));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeHidden", includeHidden));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeParent", includeParent));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortTogether", sortTogether));

    
    
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
   * Move or rename a file with overwrite mode option.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The id of the reaource to move (required)
   * @param moveFileRequest The request data (required)
   * @throws ApiException if fails to make API call
   */
  public void filesMove(String id, MoveFileRequest moveFileRequest) throws ApiException {

    filesMoveWithHttpInfo(id, moveFileRequest);
  }

  /**
   * Move or rename a file with overwrite mode option.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id The id of the reaource to move (required)
   * @param moveFileRequest The request data (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> filesMoveWithHttpInfo(String id, MoveFileRequest moveFileRequest) throws ApiException {
    Object localVarPostBody = moveFileRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesMove");
    }
    
    // verify the required parameter 'moveFileRequest' is set
    if (moveFileRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'moveFileRequest' when calling filesMove");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/move"
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
  /**
   * Search contents, this can list contents in a specific folder or all files in entire space.
   * 
   * @param searchCriteria Searchstring (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param searchRoot Specify root, empty string to searches entire space (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return SearchFileContents
   * @throws ApiException if fails to make API call
   */
  public SearchFileContents filesSearch(String searchCriteria, Integer pageIndex, Integer pageSize, String sortExpression, String searchRoot, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    return filesSearchWithHttpInfo(searchCriteria, pageIndex, pageSize, sortExpression, searchRoot, sortTogether, fromDate, toDate, userId).getData();
      }

  /**
   * Search contents, this can list contents in a specific folder or all files in entire space.
   * 
   * @param searchCriteria Searchstring (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param searchRoot Specify root, empty string to searches entire space (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return ApiResponse&lt;SearchFileContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SearchFileContents> filesSearchWithHttpInfo(String searchCriteria, Integer pageIndex, Integer pageSize, String sortExpression, String searchRoot, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling filesSearch");
    }
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling filesSearch");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling filesSearch");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling filesSearch");
    }
    
    // verify the required parameter 'searchRoot' is set
    if (searchRoot == null) {
      throw new ApiException(400, "Missing the required parameter 'searchRoot' when calling filesSearch");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/search";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchRoot", searchRoot));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortTogether", sortTogether));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fromDate", fromDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toDate", toDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<SearchFileContents> localVarReturnType = new GenericType<SearchFileContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get index in Search contents, this can list contents in entire space.
   * 
   * @param id The id to get index for (required)
   * @param searchCriteria Searchstring (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return IndexContent
   * @throws ApiException if fails to make API call
   */
  public IndexContent filesSearchById(String id, String searchCriteria, String sortExpression, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    return filesSearchByIdWithHttpInfo(id, searchCriteria, sortExpression, sortTogether, fromDate, toDate, userId).getData();
      }

  /**
   * Get index in Search contents, this can list contents in entire space.
   * 
   * @param id The id to get index for (required)
   * @param searchCriteria Searchstring (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return ApiResponse&lt;IndexContent&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<IndexContent> filesSearchByIdWithHttpInfo(String id, String searchCriteria, String sortExpression, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesSearchById");
    }
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling filesSearchById");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling filesSearchById");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/search/index/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortTogether", sortTogether));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fromDate", fromDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toDate", toDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<IndexContent> localVarReturnType = new GenericType<IndexContent>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get index in Search contents, this can list contents in a specific folder.
   * 
   * @param parentId The id of the reaource to search in (required)
   * @param id The id to get index for (required)
   * @param searchCriteria Searchstring (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return IndexContent
   * @throws ApiException if fails to make API call
   */
  public IndexContent filesSearchById_0(String parentId, String id, String searchCriteria, String sortExpression, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    return filesSearchById_0WithHttpInfo(parentId, id, searchCriteria, sortExpression, sortTogether, fromDate, toDate, userId).getData();
      }

  /**
   * Get index in Search contents, this can list contents in a specific folder.
   * 
   * @param parentId The id of the reaource to search in (required)
   * @param id The id to get index for (required)
   * @param searchCriteria Searchstring (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return ApiResponse&lt;IndexContent&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<IndexContent> filesSearchById_0WithHttpInfo(String parentId, String id, String searchCriteria, String sortExpression, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'parentId' is set
    if (parentId == null) {
      throw new ApiException(400, "Missing the required parameter 'parentId' when calling filesSearchById_0");
    }
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesSearchById_0");
    }
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling filesSearchById_0");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling filesSearchById_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{parentId}/search/index/{id}"
      .replaceAll("\\{" + "parentId" + "\\}", apiClient.escapeString(parentId.toString()))
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortTogether", sortTogether));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fromDate", fromDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toDate", toDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<IndexContent> localVarReturnType = new GenericType<IndexContent>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Search contents, this can list contents in a specific folder.
   * 
   * @param id The id of the reaource to search in (required)
   * @param searchCriteria Searchstring (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return SearchFileContents
   * @throws ApiException if fails to make API call
   */
  public SearchFileContents filesSearchById_1(String id, String searchCriteria, Integer pageIndex, Integer pageSize, String sortExpression, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    return filesSearchById_1WithHttpInfo(id, searchCriteria, pageIndex, pageSize, sortExpression, sortTogether, fromDate, toDate, userId).getData();
      }

  /**
   * Search contents, this can list contents in a specific folder.
   * 
   * @param id The id of the reaource to search in (required)
   * @param searchCriteria Searchstring (required)
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression Name, Created, Modified, Accessed (desc/asc) (required)
   * @param sortTogether Set to true to sort folders and files together (optional)
   * @param fromDate from Date to search (optional)
   * @param toDate to Date to search (optional)
   * @param userId userId to search (optional)
   * @return ApiResponse&lt;SearchFileContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SearchFileContents> filesSearchById_1WithHttpInfo(String id, String searchCriteria, Integer pageIndex, Integer pageSize, String sortExpression, Boolean sortTogether, DateTime fromDate, DateTime toDate, String userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesSearchById_1");
    }
    
    // verify the required parameter 'searchCriteria' is set
    if (searchCriteria == null) {
      throw new ApiException(400, "Missing the required parameter 'searchCriteria' when calling filesSearchById_1");
    }
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling filesSearchById_1");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling filesSearchById_1");
    }
    
    // verify the required parameter 'sortExpression' is set
    if (sortExpression == null) {
      throw new ApiException(400, "Missing the required parameter 'sortExpression' when calling filesSearchById_1");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}/search"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchCriteria", searchCriteria));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortTogether", sortTogether));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fromDate", fromDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toDate", toDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<SearchFileContents> localVarReturnType = new GenericType<SearchFileContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update file properties              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id  (required)
   * @param updateFilePropertiesRequest  (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File filesUpdateFile(String id, UpdateFilePropertiesRequest updateFilePropertiesRequest) throws ApiException {
    return filesUpdateFileWithHttpInfo(id, updateFilePropertiesRequest).getData();
      }

  /**
   * Update file properties              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param id  (required)
   * @param updateFilePropertiesRequest  (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> filesUpdateFileWithHttpInfo(String id, UpdateFilePropertiesRequest updateFilePropertiesRequest) throws ApiException {
    Object localVarPostBody = updateFilePropertiesRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling filesUpdateFile");
    }
    
    // verify the required parameter 'updateFilePropertiesRequest' is set
    if (updateFilePropertiesRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'updateFilePropertiesRequest' when calling filesUpdateFile");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/files/{id}"
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

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
