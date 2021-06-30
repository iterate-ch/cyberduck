package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import java.io.File;
import ch.cyberduck.core.brick.io.swagger.client.model.StyleEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class StylesApi {
  private ApiClient apiClient;

  public StylesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public StylesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Style
   * Delete Style
   * @param path Style path. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteStylesPath(String path) throws ApiException {

    deleteStylesPathWithHttpInfo(path);
  }

  /**
   * Delete Style
   * Delete Style
   * @param path Style path. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteStylesPathWithHttpInfo(String path) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling deleteStylesPath");
    }
    
    // create path and map variables
    String localVarPath = "/styles/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

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
   * Show Style
   * Show Style
   * @param path Style path. (required)
   * @return StyleEntity
   * @throws ApiException if fails to make API call
   */
  public StyleEntity getStylesPath(String path) throws ApiException {
    return getStylesPathWithHttpInfo(path).getData();
      }

  /**
   * Show Style
   * Show Style
   * @param path Style path. (required)
   * @return ApiResponse&lt;StyleEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<StyleEntity> getStylesPathWithHttpInfo(String path) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling getStylesPath");
    }
    
    // create path and map variables
    String localVarPath = "/styles/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

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

    GenericType<StyleEntity> localVarReturnType = new GenericType<StyleEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Style
   * Update Style
   * @param path Style path. (required)
   * @param file Logo for custom branding. (required)
   * @return StyleEntity
   * @throws ApiException if fails to make API call
   */
  public StyleEntity patchStylesPath(String path, File file) throws ApiException {
    return patchStylesPathWithHttpInfo(path, file).getData();
      }

  /**
   * Update Style
   * Update Style
   * @param path Style path. (required)
   * @param file Logo for custom branding. (required)
   * @return ApiResponse&lt;StyleEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<StyleEntity> patchStylesPathWithHttpInfo(String path, File file) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling patchStylesPath");
    }
    
    // verify the required parameter 'file' is set
    if (file == null) {
      throw new ApiException(400, "Missing the required parameter 'file' when calling patchStylesPath");
    }
    
    // create path and map variables
    String localVarPath = "/styles/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (file != null)
      localVarFormParams.put("file", file);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<StyleEntity> localVarReturnType = new GenericType<StyleEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
