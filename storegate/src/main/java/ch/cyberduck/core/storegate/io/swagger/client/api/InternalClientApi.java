package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata;
import ch.cyberduck.core.storegate.io.swagger.client.model.UploadAttachmentRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UploadAttachmentsFromServerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class InternalClientApi {
  private ApiClient apiClient;

  public InternalClientApi() {
    this(Configuration.getDefaultApiClient());
  }

  public InternalClientApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get the directory list with only folders
   * 
   * @param folderId Root folder (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String internalClientGetDirList(String folderId) throws ApiException {
    return internalClientGetDirListWithHttpInfo(folderId).getData();
      }

  /**
   * Get the directory list with only folders
   * 
   * @param folderId Root folder (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> internalClientGetDirListWithHttpInfo(String folderId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling internalClientGetDirList");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/client/dirlist/{folderId}"
      .replaceAll("\\{" + "folderId" + "\\}", apiClient.escapeString(folderId.toString()));

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get the file list with both folders and files
   * 
   * @param folderId Root folder (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String internalClientGetFileList(String folderId) throws ApiException {
    return internalClientGetFileListWithHttpInfo(folderId).getData();
      }

  /**
   * Get the file list with both folders and files
   * 
   * @param folderId Root folder (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> internalClientGetFileListWithHttpInfo(String folderId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'folderId' is set
    if (folderId == null) {
      throw new ApiException(400, "Missing the required parameter 'folderId' when calling internalClientGetFileList");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/client/filelist/{folderId}"
      .replaceAll("\\{" + "folderId" + "\\}", apiClient.escapeString(folderId.toString()));

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * 
   * @param request  (required)
   * @return List&lt;FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public List<FileMetadata> internalClientOutlookServerUpload(UploadAttachmentsFromServerRequest request) throws ApiException {
    return internalClientOutlookServerUploadWithHttpInfo(request).getData();
      }

  /**
   * 
   * 
   * @param request  (required)
   * @return ApiResponse&lt;List&lt;FileMetadata&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<FileMetadata>> internalClientOutlookServerUploadWithHttpInfo(UploadAttachmentsFromServerRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling internalClientOutlookServerUpload");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/client/outlook/uploadfromserver";

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

    GenericType<List<FileMetadata>> localVarReturnType = new GenericType<List<FileMetadata>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * 
   * @param request  (required)
   * @return FileMetadata
   * @throws ApiException if fails to make API call
   */
  public FileMetadata internalClientOutlookUpload(UploadAttachmentRequest request) throws ApiException {
    return internalClientOutlookUploadWithHttpInfo(request).getData();
      }

  /**
   * 
   * 
   * @param request  (required)
   * @return ApiResponse&lt;FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileMetadata> internalClientOutlookUploadWithHttpInfo(UploadAttachmentRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling internalClientOutlookUpload");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/client/outlook/upload";

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

    GenericType<FileMetadata> localVarReturnType = new GenericType<FileMetadata>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
