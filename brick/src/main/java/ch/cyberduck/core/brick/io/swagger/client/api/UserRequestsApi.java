package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.UserRequestEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class UserRequestsApi {
  private ApiClient apiClient;

  public UserRequestsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UserRequestsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete User Request
   * Delete User Request
   * @param id User Request ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteUserRequestsId(Integer id) throws ApiException {

    deleteUserRequestsIdWithHttpInfo(id);
  }

  /**
   * Delete User Request
   * Delete User Request
   * @param id User Request ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteUserRequestsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteUserRequestsId");
    }
    
    // create path and map variables
    String localVarPath = "/user_requests/{id}"
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
   * List User Requests
   * List User Requests
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;UserRequestEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<UserRequestEntity> getUserRequests(String cursor, Integer perPage) throws ApiException {
    return getUserRequestsWithHttpInfo(cursor, perPage).getData();
      }

  /**
   * List User Requests
   * List User Requests
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;UserRequestEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<UserRequestEntity>> getUserRequestsWithHttpInfo(String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/user_requests";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

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

    GenericType<List<UserRequestEntity>> localVarReturnType = new GenericType<List<UserRequestEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show User Request
   * Show User Request
   * @param id User Request ID. (required)
   * @return UserRequestEntity
   * @throws ApiException if fails to make API call
   */
  public UserRequestEntity getUserRequestsId(Integer id) throws ApiException {
    return getUserRequestsIdWithHttpInfo(id).getData();
      }

  /**
   * Show User Request
   * Show User Request
   * @param id User Request ID. (required)
   * @return ApiResponse&lt;UserRequestEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserRequestEntity> getUserRequestsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getUserRequestsId");
    }
    
    // create path and map variables
    String localVarPath = "/user_requests/{id}"
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

    GenericType<UserRequestEntity> localVarReturnType = new GenericType<UserRequestEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create User Request
   * Create User Request
   * @param name Name of user requested (required)
   * @param email Email of user requested (required)
   * @param details Details of the user request (required)
   * @return UserRequestEntity
   * @throws ApiException if fails to make API call
   */
  public UserRequestEntity postUserRequests(String name, String email, String details) throws ApiException {
    return postUserRequestsWithHttpInfo(name, email, details).getData();
      }

  /**
   * Create User Request
   * Create User Request
   * @param name Name of user requested (required)
   * @param email Email of user requested (required)
   * @param details Details of the user request (required)
   * @return ApiResponse&lt;UserRequestEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserRequestEntity> postUserRequestsWithHttpInfo(String name, String email, String details) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'name' is set
    if (name == null) {
      throw new ApiException(400, "Missing the required parameter 'name' when calling postUserRequests");
    }
    
    // verify the required parameter 'email' is set
    if (email == null) {
      throw new ApiException(400, "Missing the required parameter 'email' when calling postUserRequests");
    }
    
    // verify the required parameter 'details' is set
    if (details == null) {
      throw new ApiException(400, "Missing the required parameter 'details' when calling postUserRequests");
    }
    
    // create path and map variables
    String localVarPath = "/user_requests";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (name != null)
      localVarFormParams.put("name", name);
if (email != null)
      localVarFormParams.put("email", email);
if (details != null)
      localVarFormParams.put("details", details);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UserRequestEntity> localVarReturnType = new GenericType<UserRequestEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
