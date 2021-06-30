package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.PublicKeyEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class PublicKeysApi {
  private ApiClient apiClient;

  public PublicKeysApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PublicKeysApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Public Key
   * Delete Public Key
   * @param id Public Key ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deletePublicKeysId(Integer id) throws ApiException {

    deletePublicKeysIdWithHttpInfo(id);
  }

  /**
   * Delete Public Key
   * Delete Public Key
   * @param id Public Key ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deletePublicKeysIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deletePublicKeysId");
    }
    
    // create path and map variables
    String localVarPath = "/public_keys/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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
   * List Public Keys
   * List Public Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;PublicKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PublicKeyEntity> getPublicKeys(Integer userId, String cursor, Integer perPage) throws ApiException {
    return getPublicKeysWithHttpInfo(userId, cursor, perPage).getData();
      }

  /**
   * List Public Keys
   * List Public Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;PublicKeyEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<PublicKeyEntity>> getPublicKeysWithHttpInfo(Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/public_keys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<PublicKeyEntity>> localVarReturnType = new GenericType<List<PublicKeyEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Public Key
   * Show Public Key
   * @param id Public Key ID. (required)
   * @return PublicKeyEntity
   * @throws ApiException if fails to make API call
   */
  public PublicKeyEntity getPublicKeysId(Integer id) throws ApiException {
    return getPublicKeysIdWithHttpInfo(id).getData();
      }

  /**
   * Show Public Key
   * Show Public Key
   * @param id Public Key ID. (required)
   * @return ApiResponse&lt;PublicKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicKeyEntity> getPublicKeysIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getPublicKeysId");
    }
    
    // create path and map variables
    String localVarPath = "/public_keys/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<PublicKeyEntity> localVarReturnType = new GenericType<PublicKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Public Key
   * Update Public Key
   * @param id Public Key ID. (required)
   * @param title Internal reference for key. (required)
   * @return PublicKeyEntity
   * @throws ApiException if fails to make API call
   */
  public PublicKeyEntity patchPublicKeysId(Integer id, String title) throws ApiException {
    return patchPublicKeysIdWithHttpInfo(id, title).getData();
      }

  /**
   * Update Public Key
   * Update Public Key
   * @param id Public Key ID. (required)
   * @param title Internal reference for key. (required)
   * @return ApiResponse&lt;PublicKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicKeyEntity> patchPublicKeysIdWithHttpInfo(Integer id, String title) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchPublicKeysId");
    }
    
    // verify the required parameter 'title' is set
    if (title == null) {
      throw new ApiException(400, "Missing the required parameter 'title' when calling patchPublicKeysId");
    }
    
    // create path and map variables
    String localVarPath = "/public_keys/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (title != null)
      localVarFormParams.put("title", title);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicKeyEntity> localVarReturnType = new GenericType<PublicKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Public Key
   * Create Public Key
   * @param title Internal reference for key. (required)
   * @param publicKey Actual contents of SSH key. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return PublicKeyEntity
   * @throws ApiException if fails to make API call
   */
  public PublicKeyEntity postPublicKeys(String title, String publicKey, Integer userId) throws ApiException {
    return postPublicKeysWithHttpInfo(title, publicKey, userId).getData();
      }

  /**
   * Create Public Key
   * Create Public Key
   * @param title Internal reference for key. (required)
   * @param publicKey Actual contents of SSH key. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return ApiResponse&lt;PublicKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicKeyEntity> postPublicKeysWithHttpInfo(String title, String publicKey, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'title' is set
    if (title == null) {
      throw new ApiException(400, "Missing the required parameter 'title' when calling postPublicKeys");
    }
    
    // verify the required parameter 'publicKey' is set
    if (publicKey == null) {
      throw new ApiException(400, "Missing the required parameter 'publicKey' when calling postPublicKeys");
    }
    
    // create path and map variables
    String localVarPath = "/public_keys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (title != null)
      localVarFormParams.put("title", title);
if (publicKey != null)
      localVarFormParams.put("public_key", publicKey);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<PublicKeyEntity> localVarReturnType = new GenericType<PublicKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
