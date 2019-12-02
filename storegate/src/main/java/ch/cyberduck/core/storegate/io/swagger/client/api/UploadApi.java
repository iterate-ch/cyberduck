package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-12-02T20:20:31.369+01:00")
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
   * Cancel a resumable upload.
   * 
   * @param uploadId The uploadId (required)
   * @throws ApiException if fails to make API call
   */
  public void uploadDeleteResumable(String uploadId) throws ApiException {

    uploadDeleteResumableWithHttpInfo(uploadId);
  }

  /**
   * Cancel a resumable upload.
   * 
   * @param uploadId The uploadId (required)
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
   * Upload a file using a multipart request containing first a metadata (see reponse) part and a then the filedata part.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @return ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata
   * @throws ApiException if fails to make API call
   */
  public ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata uploadPostMultipart() throws ApiException {
    return uploadPostMultipartWithHttpInfo().getData();
      }

  /**
   * Upload a file using a multipart request containing first a metadata (see reponse) part and a then the filedata part.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @return ApiResponse&lt;ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata> uploadPostMultipartWithHttpInfo() throws ApiException {
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

    GenericType<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata> localVarReturnType = new GenericType<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Upload a file to a share using a multipart request containing first a metadata (see reponse) part and a then the filedata part.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param shareid The shareId (required)
   * @return ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata
   * @throws ApiException if fails to make API call
   */
  public ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata uploadPostMultipartShare(String shareid) throws ApiException {
    return uploadPostMultipartShareWithHttpInfo(shareid).getData();
      }

  /**
   * Upload a file to a share using a multipart request containing first a metadata (see reponse) part and a then the filedata part.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param shareid The shareId (required)
   * @return ApiResponse&lt;ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata> uploadPostMultipartShareWithHttpInfo(String shareid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling uploadPostMultipartShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/upload/shares/{shareid}"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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

    GenericType<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata> localVarReturnType = new GenericType<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Starts a resumable (chunked) upload.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param metadata The metadata (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uploadPostResumable(ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata metadata) throws ApiException {
    return uploadPostResumableWithHttpInfo(metadata).getData();
      }

  /**
   * Starts a resumable (chunked) upload.              Use header \&quot;X-Lock-Id\&quot; to send lock id if needed
   * 
   * @param metadata The metadata (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> uploadPostResumableWithHttpInfo(ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata metadata) throws ApiException {
    Object localVarPostBody = metadata;
    
    // verify the required parameter 'metadata' is set
    if (metadata == null) {
      throw new ApiException(400, "Missing the required parameter 'metadata' when calling uploadPostResumable");
    }
    
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
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Starts a resumable (chunked) upload to a share.
   * 
   * @param shareid The shareId (required)
   * @param metadata The metadata (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String uploadPostResumableShare(String shareid, ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata metadata) throws ApiException {
    return uploadPostResumableShareWithHttpInfo(shareid, metadata).getData();
      }

  /**
   * Starts a resumable (chunked) upload to a share.
   * 
   * @param shareid The shareId (required)
   * @param metadata The metadata (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> uploadPostResumableShareWithHttpInfo(String shareid, ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata metadata) throws ApiException {
    Object localVarPostBody = metadata;
    
    // verify the required parameter 'shareid' is set
    if (shareid == null) {
      throw new ApiException(400, "Missing the required parameter 'shareid' when calling uploadPostResumableShare");
    }
    
    // verify the required parameter 'metadata' is set
    if (metadata == null) {
      throw new ApiException(400, "Missing the required parameter 'metadata' when calling uploadPostResumableShare");
    }
    
    // create path and map variables
    String localVarPath = "/v4/upload/shares/{shareid}/resumable"
      .replaceAll("\\{" + "shareid" + "\\}", apiClient.escapeString(shareid.toString()));

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
   * Upload a chunk to the resumable upload.              Use Content-Length and Content-Range to describe the chunk size and offset.              Use Content-Length &#x3D; 0 and Content-Range &#x3D; *_/Length to query upload status.
   * 
   * @param uploadId The uploadId (required)
   * @return ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata
   * @throws ApiException if fails to make API call
   */
  public ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata uploadPutResumable(String uploadId) throws ApiException {
    return uploadPutResumableWithHttpInfo(uploadId).getData();
      }

  /**
   * Upload a chunk to the resumable upload.              Use Content-Length and Content-Range to describe the chunk size and offset.              Use Content-Length &#x3D; 0 and Content-Range &#x3D; *_/Length to query upload status.
   * 
   * @param uploadId The uploadId (required)
   * @return ApiResponse&lt;ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata> uploadPutResumableWithHttpInfo(String uploadId) throws ApiException {
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

    GenericType<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata> localVarReturnType = new GenericType<ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
