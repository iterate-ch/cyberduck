package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.S3Config;
import ch.cyberduck.core.sds.io.swagger.client.model.S3ConfigCreateRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.S3ConfigUpdateRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.S3Tag;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagCreateRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.S3TagList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class SystemStorageConfigApi {
  private ApiClient apiClient;

  public SystemStorageConfigApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SystemStorageConfigApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create S3 storage configuration
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Create new S3 configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New S3 configuration is created.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3Config
   * @throws ApiException if fails to make API call
   */
  public S3Config createS3Config(S3ConfigCreateRequest body, String xSdsAuthToken) throws ApiException {
    return createS3ConfigWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Create S3 storage configuration
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Create new S3 configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New S3 configuration is created.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3Config&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3Config> createS3ConfigWithHttpInfo(S3ConfigCreateRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createS3Config");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/storage/s3";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3Config> localVarReturnType = new GenericType<S3Config>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create S3 tag
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Create new S3 tag.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New S3 tag is created.  ### &amp;#9432; Further Information: * Maximum key length: **128** characters.   * Maximum value length: **256** characters.   * Both S3 tag key and value are **case-sensitive** strings.   * Maximum of **20 mandatory S3 tags** is allowed.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3Tag
   * @throws ApiException if fails to make API call
   */
  public S3Tag createS3Tag(S3TagCreateRequest body, String xSdsAuthToken) throws ApiException {
    return createS3TagWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Create S3 tag
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Create new S3 tag.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: New S3 tag is created.  ### &amp;#9432; Further Information: * Maximum key length: **128** characters.   * Maximum value length: **256** characters.   * Both S3 tag key and value are **case-sensitive** strings.   * Maximum of **20 mandatory S3 tags** is allowed.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3Tag&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3Tag> createS3TagWithHttpInfo(S3TagCreateRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createS3Tag");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/storage/s3/tags";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3Tag> localVarReturnType = new GenericType<S3Tag>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete S3 tag
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Delete S3 tag.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: S3 tag gets deleted.  ### &amp;#9432; Further Information: None.
   * @param id S3 tag ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteS3Tag(Long id, String xSdsAuthToken) throws ApiException {

    deleteS3TagWithHttpInfo(id, xSdsAuthToken);
  }

  /**
   * Delete S3 tag
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Delete S3 tag.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: S3 tag gets deleted.  ### &amp;#9432; Further Information: None.
   * @param id S3 tag ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteS3TagWithHttpInfo(Long id, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteS3Tag");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/storage/s3/tags/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get S3 storage configuration
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve S3 configuration.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3Config
   * @throws ApiException if fails to make API call
   */
  public S3Config getS3Config(String xSdsAuthToken) throws ApiException {
    return getS3ConfigWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get S3 storage configuration
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Retrieve S3 configuration.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3Config&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3Config> getS3ConfigWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/storage/s3";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3Config> localVarReturnType = new GenericType<S3Config>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get S3 tag
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Retrieve single S3 tag.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param id S3 tag ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3Tag
   * @throws ApiException if fails to make API call
   */
  public S3Tag getS3Tag(Long id, String xSdsAuthToken) throws ApiException {
    return getS3TagWithHttpInfo(id, xSdsAuthToken).getData();
      }

  /**
   * Get S3 tag
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Retrieve single S3 tag.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param id S3 tag ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3Tag&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3Tag> getS3TagWithHttpInfo(Long id, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getS3Tag");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/storage/s3/tags/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3Tag> localVarReturnType = new GenericType<S3Tag>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of configured S3 tags
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Retrieve all configured S3 tags.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: An empty list is returned if no S3 tags are found / configured.
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3TagList
   * @throws ApiException if fails to make API call
   */
  public S3TagList getS3TagList(String xSdsAuthToken) throws ApiException {
    return getS3TagListWithHttpInfo(xSdsAuthToken).getData();
      }

  /**
   * Get list of configured S3 tags
   * ### &amp;#128640; Since version 4.9.0  ### Functional Description:   Retrieve all configured S3 tags.  ### Precondition: Right _\&quot;read global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: None.  ### &amp;#9432; Further Information: An empty list is returned if no S3 tags are found / configured.
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3TagList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3TagList> getS3TagListWithHttpInfo(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/system/config/storage/s3/tags";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3TagList> localVarReturnType = new GenericType<S3TagList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update S3 storage configuration
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Update existing S3 configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: S3 configuration is updated.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return S3Config
   * @throws ApiException if fails to make API call
   */
  public S3Config updateS3Config(S3ConfigUpdateRequest body, String xSdsAuthToken) throws ApiException {
    return updateS3ConfigWithHttpInfo(body, xSdsAuthToken).getData();
      }

  /**
   * Update S3 storage configuration
   * ### &amp;#128640; Since version 4.3.0  ### Functional Description:   Update existing S3 configuration.  ### Precondition: Right _\&quot;change global config\&quot;_ required.   Role _Config Manager_ of the Provider Customer.  ### Effects: S3 configuration is updated.  ### &amp;#9432; Further Information: None.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @return ApiResponse&lt;S3Config&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<S3Config> updateS3ConfigWithHttpInfo(S3ConfigUpdateRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateS3Config");
    }
    
    // create path and map variables
    String localVarPath = "/v4/system/config/storage/s3";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<S3Config> localVarReturnType = new GenericType<S3Config>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
