package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import java.util.UUID;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Watch;
import ch.cyberduck.core.deepbox.io.swagger.client.model.WatchAdd;
import ch.cyberduck.core.deepbox.io.swagger.client.model.WatchUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WatchRestControllerApi {
  private ApiClient apiClient;

  public WatchRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public WatchRestControllerApi(ApiClient apiClient) {
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
   * @param nodeId  (required)
   * @param body  (optional)
   * @return Watch
   * @throws ApiException if fails to make API call
   */
  public Watch addWatch(UUID nodeId, WatchAdd body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling addWatch");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/watches"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Watch> localVarReturnType = new GenericType<Watch>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param watchId  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteWatch(UUID watchId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'watchId' is set
    if (watchId == null) {
      throw new ApiException(400, "Missing the required parameter 'watchId' when calling deleteWatch");
    }
    // create path and map variables
    String localVarPath = "/api/v1/watches/{watchId}"
      .replaceAll("\\{" + "watchId" + "\\}", apiClient.escapeString(watchId.toString()));

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
  /**
   * 
   * 
   * @param body  (required)
   * @param watchId  (required)
   * @throws ApiException if fails to make API call
   */
  public void updateWatch(WatchUpdate body, UUID watchId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateWatch");
    }
    // verify the required parameter 'watchId' is set
    if (watchId == null) {
      throw new ApiException(400, "Missing the required parameter 'watchId' when calling updateWatch");
    }
    // create path and map variables
    String localVarPath = "/api/v1/watches/{watchId}"
      .replaceAll("\\{" + "watchId" + "\\}", apiClient.escapeString(watchId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
