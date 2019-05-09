package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-04-02T17:31:35.366+02:00")
public class UploadApi {
  private ApiClient apiClient;

  public UploadApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UploadApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete the resumable upload.
   * 
   * @param uploadId The id associated with the upload (required)
   * @throws ApiException if fails to make API call
   */
  public void uploadDeleteResumable(String uploadId) throws ApiException {

    uploadDeleteResumableWithHttpInfo(uploadId);
  }

  /**
   * Delete the resumable upload.
   * 
   * @param uploadId The id associated with the upload (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> uploadDeleteResumableWithHttpInfo(String uploadId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadDeleteResumable");
    }
    
    // create path and map variables
    String localVarPath = "/v4/upload/resumable";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "uploadId", uploadId));

    
    
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
   * Uploads a file using a multipart request containing a metadata part and a filedata part.
   * 
   * @return FileMetadata
   * @throws ApiException if fails to make API call
   */
  public FileMetadata uploadPostMultipart() throws ApiException {
    return uploadPostMultipartWithHttpInfo().getData();
      }

  /**
   * Uploads a file using a multipart request containing a metadata part and a filedata part.
   * 
   * @return ApiResponse&lt;FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileMetadata> uploadPostMultipartWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/upload";

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

    GenericType<FileMetadata> localVarReturnType = new GenericType<FileMetadata>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Uploads a file to a share using a multipart request containing a metadata part and a filedata part.
   * 
   * @param shareid  (required)
   * @param accessid  (required)
   * @return FileMetadata
   * @throws ApiException if fails to make API call
   */
  public FileMetadata uploadPostMultipartShare(String shareid, String accessid) throws ApiException {
    return uploadPostMultipartShareWithHttpInfo(shareid, accessid).getData();
      }

  /**
   * Uploads a file to a share using a multipart request containing a metadata part and a filedata part.
   * 
   * @param shareid  (required)
   * @param accessid  (required)
   * @return ApiResponse&lt;FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileMetadata> uploadPostMultipartShareWithHttpInfo(String shareid, String accessid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling uploadPostMultipartShare");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling uploadPostMultipartShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/upload/shares/{shareid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<FileMetadata> localVarReturnType = new GenericType<FileMetadata>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Starts a resumable upload.
   * 
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uploadPostResumable() throws ApiException {
    return uploadPostResumableWithHttpInfo().getData();
      }

  /**
   * Starts a resumable upload.
   * 
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> uploadPostResumableWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/upload/resumable";

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
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Starts a resumable upload to a share.
   * 
   * @param shareid  (required)
   * @param accessid  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uploadPostResumableShare(String shareid, String accessid) throws ApiException {
    return uploadPostResumableShareWithHttpInfo(shareid, accessid).getData();
      }

  /**
   * Starts a resumable upload to a share.
   * 
   * @param shareid  (required)
   * @param accessid  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> uploadPostResumableShareWithHttpInfo(String shareid, String accessid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling uploadPostResumableShare");
    }
    
    // verify the required parameter 'accessid' is set
    if (accessid == null) {
      throw new ApiException(400, "Missing the required parameter 'accessid' when calling uploadPostResumableShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/upload/shares/{shareid}/resumable"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "accessid", accessid));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Upload the chunkes to the resumable upload.              Use Content-Length: 0 and Content-Range: *_/Length to query upload status
   * 
   * @param uploadId The id associated with the upload (required)
   * @return FileMetadata
   * @throws ApiException if fails to make API call
   */
  public FileMetadata uploadPutResumable(String uploadId) throws ApiException {
    return uploadPutResumableWithHttpInfo(uploadId).getData();
      }

  /**
   * Upload the chunkes to the resumable upload.              Use Content-Length: 0 and Content-Range: *_/Length to query upload status
   * 
   * @param uploadId The id associated with the upload (required)
   * @return ApiResponse&lt;FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<FileMetadata> uploadPutResumableWithHttpInfo(String uploadId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'uploadId' is set
    if (uploadId == null) {
      throw new ApiException(400, "Missing the required parameter 'uploadId' when calling uploadPutResumable");
    }
    
    // create path and map variables
    String localVarPath = "/v4/upload/resumable";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "uploadId", uploadId));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<FileMetadata> localVarReturnType = new GenericType<FileMetadata>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
