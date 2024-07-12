package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FavoriteRestControllerApi {
  private ApiClient apiClient;

  public FavoriteRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FavoriteRestControllerApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @throws ApiException if fails to make API call
   */
  public void createBoxFavorite(String deepBoxNodeId, String boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling createBoxFavorite");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling createBoxFavorite");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/favorites"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

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

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteBoxFavorite(String deepBoxNodeId, String boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling deleteBoxFavorite");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling deleteBoxFavorite");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/favorites"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

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

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
