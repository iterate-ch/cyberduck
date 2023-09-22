package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateDirectoryNotificationRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class DirectoryNotificationsApi {
  private ApiClient apiClient;

  public DirectoryNotificationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DirectoryNotificationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Creates a new directory notification for current user.
   * 
   * @param request createFolderRequest (required)
   * @return Object
   * @throws ApiException if fails to make API call
   */
  public Object directoryNotificationsCreate(CreateDirectoryNotificationRequest request) throws ApiException {
    return directoryNotificationsCreateWithHttpInfo(request).getData();
      }

  /**
   * Creates a new directory notification for current user.
   * 
   * @param request createFolderRequest (required)
   * @return ApiResponse&lt;Object&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Object> directoryNotificationsCreateWithHttpInfo(CreateDirectoryNotificationRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling directoryNotificationsCreate");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/directorynotifications";

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

    GenericType<Object> localVarReturnType = new GenericType<Object>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Removes a directory notification.
   * 
   * @param id Id of the directory (required)
   * @throws ApiException if fails to make API call
   */
  public void directoryNotificationsDelete(String id) throws ApiException {

    directoryNotificationsDeleteWithHttpInfo(id);
  }

  /**
   * Removes a directory notification.
   * 
   * @param id Id of the directory (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> directoryNotificationsDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling directoryNotificationsDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/directorynotifications/{id}"
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
   * Get all directory notifications for current user
   * 
   * @return List&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public List<File> directoryNotificationsGetAll() throws ApiException {
    return directoryNotificationsGetAllWithHttpInfo().getData();
      }

  /**
   * Get all directory notifications for current user
   * 
   * @return ApiResponse&lt;List&lt;File&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<File>> directoryNotificationsGetAllWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/directorynotifications";

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

    GenericType<List<File>> localVarReturnType = new GenericType<List<File>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
