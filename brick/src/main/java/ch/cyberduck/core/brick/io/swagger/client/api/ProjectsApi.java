package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ProjectEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class ProjectsApi {
  private ApiClient apiClient;

  public ProjectsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ProjectsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Project
   * Delete Project
   * @param id Project ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteProjectsId(Integer id) throws ApiException {

    deleteProjectsIdWithHttpInfo(id);
  }

  /**
   * Delete Project
   * Delete Project
   * @param id Project ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteProjectsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteProjectsId");
    }
    
    // create path and map variables
    String localVarPath = "/projects/{id}"
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
   * List Projects
   * List Projects
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;ProjectEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ProjectEntity> getProjects(String cursor, Integer perPage) throws ApiException {
    return getProjectsWithHttpInfo(cursor, perPage).getData();
      }

  /**
   * List Projects
   * List Projects
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;ProjectEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<ProjectEntity>> getProjectsWithHttpInfo(String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/projects";

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

    GenericType<List<ProjectEntity>> localVarReturnType = new GenericType<List<ProjectEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Project
   * Show Project
   * @param id Project ID. (required)
   * @return ProjectEntity
   * @throws ApiException if fails to make API call
   */
  public ProjectEntity getProjectsId(Integer id) throws ApiException {
    return getProjectsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Project
   * Show Project
   * @param id Project ID. (required)
   * @return ApiResponse&lt;ProjectEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ProjectEntity> getProjectsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getProjectsId");
    }
    
    // create path and map variables
    String localVarPath = "/projects/{id}"
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

    GenericType<ProjectEntity> localVarReturnType = new GenericType<ProjectEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Project
   * Update Project
   * @param id Project ID. (required)
   * @param globalAccess Global permissions.  Can be: &#x60;none&#x60;, &#x60;anyone_with_read&#x60;, &#x60;anyone_with_full&#x60;. (required)
   * @return ProjectEntity
   * @throws ApiException if fails to make API call
   */
  public ProjectEntity patchProjectsId(Integer id, String globalAccess) throws ApiException {
    return patchProjectsIdWithHttpInfo(id, globalAccess).getData();
      }

  /**
   * Update Project
   * Update Project
   * @param id Project ID. (required)
   * @param globalAccess Global permissions.  Can be: &#x60;none&#x60;, &#x60;anyone_with_read&#x60;, &#x60;anyone_with_full&#x60;. (required)
   * @return ApiResponse&lt;ProjectEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ProjectEntity> patchProjectsIdWithHttpInfo(Integer id, String globalAccess) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchProjectsId");
    }
    
    // verify the required parameter 'globalAccess' is set
    if (globalAccess == null) {
      throw new ApiException(400, "Missing the required parameter 'globalAccess' when calling patchProjectsId");
    }
    
    // create path and map variables
    String localVarPath = "/projects/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (globalAccess != null)
      localVarFormParams.put("global_access", globalAccess);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ProjectEntity> localVarReturnType = new GenericType<ProjectEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Project
   * Create Project
   * @param globalAccess Global permissions.  Can be: &#x60;none&#x60;, &#x60;anyone_with_read&#x60;, &#x60;anyone_with_full&#x60;. (required)
   * @return ProjectEntity
   * @throws ApiException if fails to make API call
   */
  public ProjectEntity postProjects(String globalAccess) throws ApiException {
    return postProjectsWithHttpInfo(globalAccess).getData();
      }

  /**
   * Create Project
   * Create Project
   * @param globalAccess Global permissions.  Can be: &#x60;none&#x60;, &#x60;anyone_with_read&#x60;, &#x60;anyone_with_full&#x60;. (required)
   * @return ApiResponse&lt;ProjectEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ProjectEntity> postProjectsWithHttpInfo(String globalAccess) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'globalAccess' is set
    if (globalAccess == null) {
      throw new ApiException(400, "Missing the required parameter 'globalAccess' when calling postProjects");
    }
    
    // create path and map variables
    String localVarPath = "/projects";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (globalAccess != null)
      localVarFormParams.put("global_access", globalAccess);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ProjectEntity> localVarReturnType = new GenericType<ProjectEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
