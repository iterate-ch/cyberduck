package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ChunkUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import java.io.File;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class UploadsApi {
  private ApiClient apiClient;

  public UploadsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UploadsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Cancel file upload
   * ### Functional Description: Cancel file upload.  ### Precondition: Valid upload token.  ### Effects: Upload canceled, token invalidated and all already transfered chunks removed.  ### &amp;#9432; Further Information: It is recommended to notify the API about cancelled uploads if possible.
   * @param token Upload token (required)
   * @throws ApiException if fails to make API call
   */
  public void cancelFileUploadByToken(String token) throws ApiException {

    cancelFileUploadByTokenWithHttpInfo(token);
  }

  /**
   * Cancel file upload
   * ### Functional Description: Cancel file upload.  ### Precondition: Valid upload token.  ### Effects: Upload canceled, token invalidated and all already transfered chunks removed.  ### &amp;#9432; Further Information: It is recommended to notify the API about cancelled uploads if possible.
   * @param token Upload token (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> cancelFileUploadByTokenWithHttpInfo(String token) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling cancelFileUploadByToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/uploads/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
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
   * Complete file upload
   * ### Functional Description: Finish uploading a file.  ### Precondition: Valid upload token.  ### Effects: File created.  ### &amp;#9432; Further Information: The provided file name might be changed in accordance with the resolution strategy:  * **autorename**: changes the file name and adds a number to avoid conflicts. * **overwrite**: deletes any old file with the same file name. * **fail**: returns an error; in this case, another &#x60;PUT&#x60; request with a different file name may be sent.  Please ensure that all chunks have been transferred correctly before finishing the upload. ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### 200 OK is **NOT** used by this API
   * @param token Upload token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param body The body must be empty if public upload token is used. The &#x60;resolutionStrategy&#x60; in that case will be always &#x60;autorename&#x60; (optional)
   * @return Node
   * @throws ApiException if fails to make API call
   */
  public Node completeFileUploadByToken(String token, String xSdsDateFormat, CompleteUploadRequest body) throws ApiException {
    return completeFileUploadByTokenWithHttpInfo(token, xSdsDateFormat, body).getData();
      }

  /**
   * Complete file upload
   * ### Functional Description: Finish uploading a file.  ### Precondition: Valid upload token.  ### Effects: File created.  ### &amp;#9432; Further Information: The provided file name might be changed in accordance with the resolution strategy:  * **autorename**: changes the file name and adds a number to avoid conflicts. * **overwrite**: deletes any old file with the same file name. * **fail**: returns an error; in this case, another &#x60;PUT&#x60; request with a different file name may be sent.  Please ensure that all chunks have been transferred correctly before finishing the upload. ## #### &amp;#9888; Download share id (if exists) gets changed if: - node with the same name exists in the target container - **&#x60;resolutionStrategy&#x60;** is **&#x60;overwrite&#x60;** - **&#x60;keepShareLinks&#x60;** is **&#x60;true&#x60;**  ### 200 OK is **NOT** used by this API
   * @param token Upload token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param body The body must be empty if public upload token is used. The &#x60;resolutionStrategy&#x60; in that case will be always &#x60;autorename&#x60; (optional)
   * @return ApiResponse&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Node> completeFileUploadByTokenWithHttpInfo(String token, String xSdsDateFormat, CompleteUploadRequest body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling completeFileUploadByToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/uploads/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Node> localVarReturnType = new GenericType<Node>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Upload file
   * ### Functional Description:   Upload a (chunk of a) file.  ### Precondition: Valid upload token.  ### Effects: Chunk uploaded.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).    Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;  For both file upload types set the correct &#x60;Content-Type&#x60; header and body.   Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/uploads/{token} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;  &#x60;&#x60;&#x60; POST /api/v4/uploads/{token} HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60; 
   * @param file File (required)
   * @param token Upload token (required)
   * @param contentRange Content-Range e.g. &#x60;bytes 0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @return ChunkUploadResponse
   * @throws ApiException if fails to make API call
   */
  public ChunkUploadResponse uploadFileByToken(File file, String token, String contentRange) throws ApiException {
    return uploadFileByTokenWithHttpInfo(file, token, contentRange).getData();
      }

  /**
   * Upload file
   * ### Functional Description:   Upload a (chunk of a) file.  ### Precondition: Valid upload token.  ### Effects: Chunk uploaded.  ### &amp;#9432; Further Information: Range requests are supported (please cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) for details).    Following &#x60;Content-Types&#x60; are supported by this API: * &#x60;multipart/form-data&#x60; * provided &#x60;Content-Type&#x60;  For both file upload types set the correct &#x60;Content-Type&#x60; header and body.   Examples:    * &#x60;multipart/form-data&#x60; &#x60;&#x60;&#x60; POST /api/v4/uploads/{token} HTTP/1.1  Header: ... Content-Type: multipart/form-data; boundary&#x3D;----WebKitFormBoundary7MA4YWxkTrZu0gW ...  Body: ------WebKitFormBoundary7MA4YWxkTrZu0gW Content-Disposition: form-data; name&#x3D;\&quot;file\&quot;; filename&#x3D;\&quot;file.txt\&quot; Content-Type: text/plain  Content of file.txt ------WebKitFormBoundary7MA4YWxkTrZu0gW-- &#x60;&#x60;&#x60;  * any other &#x60;Content-Type&#x60;  &#x60;&#x60;&#x60; POST /api/v4/uploads/{token} HTTP/1.1  Header: ... Content-Type: { ... } ...  Body: raw content &#x60;&#x60;&#x60; 
   * @param file File (required)
   * @param token Upload token (required)
   * @param contentRange Content-Range e.g. &#x60;bytes 0-999/3980&#x60; cf. [RFC 7233](https://tools.ietf.org/html/rfc7233) (optional)
   * @return ApiResponse&lt;ChunkUploadResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ChunkUploadResponse> uploadFileByTokenWithHttpInfo(File file, String token, String contentRange) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'file' is set
    if (file == null) {
      throw new ApiException(400, "Missing the required parameter 'file' when calling uploadFileByToken");
    }
    
    // verify the required parameter 'token' is set
    if (token == null) {
      throw new ApiException(400, "Missing the required parameter 'token' when calling uploadFileByToken");
    }
    
    // create path and map variables
    String localVarPath = "/v4/uploads/{token}"
      .replaceAll("\\{" + "token" + "\\}", apiClient.escapeString(token.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (contentRange != null)
      localVarHeaderParams.put("Content-Range", apiClient.parameterToString(contentRange));

    if (file != null)
      localVarFormParams.put("file", file);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ChunkUploadResponse> localVarReturnType = new GenericType<ChunkUploadResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
