package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.CreateFavoriteRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateFavoriteRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class FavoritesApi {
  private ApiClient apiClient;

  public FavoritesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FavoritesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Creates a new Favorite.
   * 
   * @param createFavoriteRequest createFolderRequest (required)
   * @return Object
   * @throws ApiException if fails to make API call
   */
  public Object favoritesCreate(CreateFavoriteRequest createFavoriteRequest) throws ApiException {
    return favoritesCreateWithHttpInfo(createFavoriteRequest).getData();
      }

  /**
   * Creates a new Favorite.
   * 
   * @param createFavoriteRequest createFolderRequest (required)
   * @return ApiResponse&lt;Object&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Object> favoritesCreateWithHttpInfo(CreateFavoriteRequest createFavoriteRequest) throws ApiException {
    Object localVarPostBody = createFavoriteRequest;
    
    // verify the required parameter 'createFavoriteRequest' is set
    if (createFavoriteRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'createFavoriteRequest' when calling favoritesCreate");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/favorites";

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
   * Deletes a directory or file.
   * 
   * @param id The FileId of the favorite to delete. (required)
   * @throws ApiException if fails to make API call
   */
  public void favoritesDelete(String id) throws ApiException {

    favoritesDeleteWithHttpInfo(id);
  }

  /**
   * Deletes a directory or file.
   * 
   * @param id The FileId of the favorite to delete. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> favoritesDeleteWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling favoritesDelete");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/favorites/{id}"
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
   * Get all fovorites
   * 
   * @return List&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public List<File> favoritesGetAll() throws ApiException {
    return favoritesGetAllWithHttpInfo().getData();
      }

  /**
   * Get all fovorites
   * 
   * @return ApiResponse&lt;List&lt;File&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<File>> favoritesGetAllWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/favorites";

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
  /**
   * Update Favorite Order
   * 
   * @param id  (required)
   * @param updateFavoriteRequest  (required)
   * @return Object
   * @throws ApiException if fails to make API call
   */
  public Object favoritesUpdate(String id, UpdateFavoriteRequest updateFavoriteRequest) throws ApiException {
    return favoritesUpdateWithHttpInfo(id, updateFavoriteRequest).getData();
      }

  /**
   * Update Favorite Order
   * 
   * @param id  (required)
   * @param updateFavoriteRequest  (required)
   * @return ApiResponse&lt;Object&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Object> favoritesUpdateWithHttpInfo(String id, UpdateFavoriteRequest updateFavoriteRequest) throws ApiException {
    Object localVarPostBody = updateFavoriteRequest;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling favoritesUpdate");
    }
    
    // verify the required parameter 'updateFavoriteRequest' is set
    if (updateFavoriteRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'updateFavoriteRequest' when calling favoritesUpdate");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/favorites/{id}"
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
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Object> localVarReturnType = new GenericType<Object>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
