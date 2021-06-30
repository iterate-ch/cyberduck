package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.As2KeyEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class As2KeysApi {
  private ApiClient apiClient;

  public As2KeysApi() {
    this(Configuration.getDefaultApiClient());
  }

  public As2KeysApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete As2 Key
   * Delete As2 Key
   * @param id As2 Key ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteAs2KeysId(Integer id) throws ApiException {

    deleteAs2KeysIdWithHttpInfo(id);
  }

  /**
   * Delete As2 Key
   * Delete As2 Key
   * @param id As2 Key ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteAs2KeysIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteAs2KeysId");
    }
    
    // create path and map variables
    String localVarPath = "/as2_keys/{id}"
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
   * List As2 Keys
   * List As2 Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;As2KeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<As2KeyEntity> getAs2Keys(Integer userId, String cursor, Integer perPage) throws ApiException {
    return getAs2KeysWithHttpInfo(userId, cursor, perPage).getData();
      }

  /**
   * List As2 Keys
   * List As2 Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;As2KeyEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<As2KeyEntity>> getAs2KeysWithHttpInfo(Integer userId, String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/as2_keys";

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

    GenericType<List<As2KeyEntity>> localVarReturnType = new GenericType<List<As2KeyEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show As2 Key
   * Show As2 Key
   * @param id As2 Key ID. (required)
   * @return As2KeyEntity
   * @throws ApiException if fails to make API call
   */
  public As2KeyEntity getAs2KeysId(Integer id) throws ApiException {
    return getAs2KeysIdWithHttpInfo(id).getData();
      }

  /**
   * Show As2 Key
   * Show As2 Key
   * @param id As2 Key ID. (required)
   * @return ApiResponse&lt;As2KeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<As2KeyEntity> getAs2KeysIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getAs2KeysId");
    }
    
    // create path and map variables
    String localVarPath = "/as2_keys/{id}"
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

    GenericType<As2KeyEntity> localVarReturnType = new GenericType<As2KeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update As2 Key
   * Update As2 Key
   * @param id As2 Key ID. (required)
   * @param as2PartnershipName AS2 Partnership Name (required)
   * @return As2KeyEntity
   * @throws ApiException if fails to make API call
   */
  public As2KeyEntity patchAs2KeysId(Integer id, String as2PartnershipName) throws ApiException {
    return patchAs2KeysIdWithHttpInfo(id, as2PartnershipName).getData();
      }

  /**
   * Update As2 Key
   * Update As2 Key
   * @param id As2 Key ID. (required)
   * @param as2PartnershipName AS2 Partnership Name (required)
   * @return ApiResponse&lt;As2KeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<As2KeyEntity> patchAs2KeysIdWithHttpInfo(Integer id, String as2PartnershipName) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchAs2KeysId");
    }
    
    // verify the required parameter 'as2PartnershipName' is set
    if (as2PartnershipName == null) {
      throw new ApiException(400, "Missing the required parameter 'as2PartnershipName' when calling patchAs2KeysId");
    }
    
    // create path and map variables
    String localVarPath = "/as2_keys/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (as2PartnershipName != null)
      localVarFormParams.put("as2_partnership_name", as2PartnershipName);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<As2KeyEntity> localVarReturnType = new GenericType<As2KeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create As2 Key
   * Create As2 Key
   * @param as2PartnershipName AS2 Partnership Name (required)
   * @param publicKey Actual contents of Public key. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return As2KeyEntity
   * @throws ApiException if fails to make API call
   */
  public As2KeyEntity postAs2Keys(String as2PartnershipName, String publicKey, Integer userId) throws ApiException {
    return postAs2KeysWithHttpInfo(as2PartnershipName, publicKey, userId).getData();
      }

  /**
   * Create As2 Key
   * Create As2 Key
   * @param as2PartnershipName AS2 Partnership Name (required)
   * @param publicKey Actual contents of Public key. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @return ApiResponse&lt;As2KeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<As2KeyEntity> postAs2KeysWithHttpInfo(String as2PartnershipName, String publicKey, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'as2PartnershipName' is set
    if (as2PartnershipName == null) {
      throw new ApiException(400, "Missing the required parameter 'as2PartnershipName' when calling postAs2Keys");
    }
    
    // verify the required parameter 'publicKey' is set
    if (publicKey == null) {
      throw new ApiException(400, "Missing the required parameter 'publicKey' when calling postAs2Keys");
    }
    
    // create path and map variables
    String localVarPath = "/as2_keys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (as2PartnershipName != null)
      localVarFormParams.put("as2_partnership_name", as2PartnershipName);
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

    GenericType<As2KeyEntity> localVarReturnType = new GenericType<As2KeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
