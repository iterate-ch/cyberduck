package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxUpdateSystem;
import ch.cyberduck.core.deepbox.io.swagger.client.model.OnboardData;
import ch.cyberduck.core.deepbox.io.swagger.client.model.OnboardResult;
import ch.cyberduck.core.deepbox.io.swagger.client.model.RemoveUser;
import ch.cyberduck.core.deepbox.io.swagger.client.model.TransformToUser;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SystemRestControllerApi {
  private ApiClient apiClient;

  public SystemRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SystemRestControllerApi(ApiClient apiClient) {
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
   * @param body  (required)
   * @return OnboardResult
   * @throws ApiException if fails to make API call
   */
  public OnboardResult onboard(OnboardData body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling onboard");
    }
    // create path and map variables
    String localVarPath = "/api/v1/system/onboard";

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

    GenericType<OnboardResult> localVarReturnType = new GenericType<OnboardResult>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Remove user from company
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void removeUserFromCompany(RemoveUser body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling removeUserFromCompany");
    }
    // create path and map variables
    String localVarPath = "/api/v1/system/removeUserFromCompany";

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

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Transform pending user to regular (deepadmin)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void transformToUser(TransformToUser body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling transformToUser");
    }
    // create path and map variables
    String localVarPath = "/api/v1/system/transformToUser";

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

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param body  (required)
   * @param deepBoxNodeId  (required)
   * @throws ApiException if fails to make API call
   */
  public void updateDeepBoxSystem(DeepBoxUpdateSystem body, UUID deepBoxNodeId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateDeepBoxSystem");
    }
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling updateDeepBoxSystem");
    }
    // create path and map variables
    String localVarPath = "/api/v1/system/deepBoxes/{deepBoxNodeId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()));

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
