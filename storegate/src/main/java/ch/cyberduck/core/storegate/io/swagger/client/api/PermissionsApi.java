package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.FilePermission;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdatePermissionRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class PermissionsApi {
  private ApiClient apiClient;

  public PermissionsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PermissionsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Evaluates permission for current user on the Common folder or sub folders in the Common folder
   * 
   * @param id The folder id. This must be the Common folder or a sub folder in the Common folder (required)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer permissionsEvaluatePermission(String id) throws ApiException {
    return permissionsEvaluatePermissionWithHttpInfo(id).getData();
      }

  /**
   * Evaluates permission for current user on the Common folder or sub folders in the Common folder
   * 
   * @param id The folder id. This must be the Common folder or a sub folder in the Common folder (required)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> permissionsEvaluatePermissionWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling permissionsEvaluatePermission");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/permissions/{id}/evaluate"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Gets the permission for all users and groups on the Common folder or sub folders in the Common folder
   * 
   * @param id The folder id. This must be the Common folder or a sub folder in the Common folder (required)
   * @return List&lt;FilePermission&gt;
   * @throws ApiException if fails to make API call
   */
  public List<FilePermission> permissionsGet(String id) throws ApiException {
    return permissionsGetWithHttpInfo(id).getData();
      }

  /**
   * Gets the permission for all users and groups on the Common folder or sub folders in the Common folder
   * 
   * @param id The folder id. This must be the Common folder or a sub folder in the Common folder (required)
   * @return ApiResponse&lt;List&lt;FilePermission&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<FilePermission>> permissionsGetWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling permissionsGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/permissions/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<List<FilePermission>> localVarReturnType = new GenericType<List<FilePermission>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Sets permission for users and groups on the Common folder or sub folder in the Common folder.
   * 
   * @param id The folder id. This must be the Common folder or a sub folder in the Common folder (required)
   * @param permissionRequest The userId or groupId. (required)
   * @throws ApiException if fails to make API call
   */
  public void permissionsPut(String id, UpdatePermissionRequest permissionRequest) throws ApiException {

    permissionsPutWithHttpInfo(id, permissionRequest);
  }

  /**
   * Sets permission for users and groups on the Common folder or sub folder in the Common folder.
   * 
   * @param id The folder id. This must be the Common folder or a sub folder in the Common folder (required)
   * @param permissionRequest The userId or groupId. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> permissionsPutWithHttpInfo(String id, UpdatePermissionRequest permissionRequest) throws ApiException {
    Object localVarPostBody = permissionRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling permissionsPut");
    }
    
    // verify the required parameter 'permissionRequest' is set
    if (permissionRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'permissionRequest' when calling permissionsPut");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/permissions/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
