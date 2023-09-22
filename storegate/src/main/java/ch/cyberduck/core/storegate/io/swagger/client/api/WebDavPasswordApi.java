package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateWebDavPasswordRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.WebDavAlias;
import ch.cyberduck.core.storegate.io.swagger.client.model.WebDavPassword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class WebDavPasswordApi {
  private ApiClient apiClient;

  public WebDavPasswordApi() {
    this(Configuration.getDefaultApiClient());
  }

  public WebDavPasswordApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create a WebDAV password.
   * 
   * @param createWebDavPasswordRequest The CreateUserRequest (required)
   * @return WebDavPassword
   * @throws ApiException if fails to make API call
   */
  public WebDavPassword webDavPasswordCreate(CreateWebDavPasswordRequest createWebDavPasswordRequest) throws ApiException {
    return webDavPasswordCreateWithHttpInfo(createWebDavPasswordRequest).getData();
      }

  /**
   * Create a WebDAV password.
   * 
   * @param createWebDavPasswordRequest The CreateUserRequest (required)
   * @return ApiResponse&lt;WebDavPassword&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebDavPassword> webDavPasswordCreateWithHttpInfo(CreateWebDavPasswordRequest createWebDavPasswordRequest) throws ApiException {
    Object localVarPostBody = createWebDavPasswordRequest;
    
    // verify the required parameter 'createWebDavPasswordRequest' is set
    if (createWebDavPasswordRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createWebDavPasswordRequest' when calling webDavPasswordCreate");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/webdavpassword";

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

    GenericType<WebDavPassword> localVarReturnType = new GenericType<WebDavPassword>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create a WebDAV password and return a list of hostname alias
   * 
   * @param createWebDavPasswordRequest The CreateUserRequest (required)
   * @return WebDavAlias
   * @throws ApiException if fails to make API call
   */
  public WebDavAlias webDavPasswordCreateAlias(CreateWebDavPasswordRequest createWebDavPasswordRequest) throws ApiException {
    return webDavPasswordCreateAliasWithHttpInfo(createWebDavPasswordRequest).getData();
      }

  /**
   * Create a WebDAV password and return a list of hostname alias
   * 
   * @param createWebDavPasswordRequest The CreateUserRequest (required)
   * @return ApiResponse&lt;WebDavAlias&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebDavAlias> webDavPasswordCreateAliasWithHttpInfo(CreateWebDavPasswordRequest createWebDavPasswordRequest) throws ApiException {
    Object localVarPostBody = createWebDavPasswordRequest;
    
    // verify the required parameter 'createWebDavPasswordRequest' is set
    if (createWebDavPasswordRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createWebDavPasswordRequest' when calling webDavPasswordCreateAlias");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/webdavpassword/alias";

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

    GenericType<WebDavAlias> localVarReturnType = new GenericType<WebDavAlias>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Remove a WebDAV password to stop it from beeing used.
   * 
   * @param id The WebDAV password id (required)
   * @throws ApiException if fails to make API call
   */
  public void webDavPasswordDelete(String id) throws ApiException {

    webDavPasswordDeleteWithHttpInfo(id);
  }

  /**
   * Remove a WebDAV password to stop it from beeing used.
   * 
   * @param id The WebDAV password id (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> webDavPasswordDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling webDavPasswordDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/webdavpassword/{id}"
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
   * Get a list of all createtd WebDAV passwords.
   * 
   * @return List&lt;WebDavPassword&gt;
   * @throws ApiException if fails to make API call
   */
  public List<WebDavPassword> webDavPasswordGet() throws ApiException {
    return webDavPasswordGetWithHttpInfo().getData();
      }

  /**
   * Get a list of all createtd WebDAV passwords.
   * 
   * @return ApiResponse&lt;List&lt;WebDavPassword&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<WebDavPassword>> webDavPasswordGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/webdavpassword";

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

    GenericType<List<WebDavPassword>> localVarReturnType = new GenericType<List<WebDavPassword>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
