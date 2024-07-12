package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.Me;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserRestControllerApi {
  private ApiClient apiClient;

  public UserRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UserRestControllerApi(ApiClient apiClient) {
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
   * @param deepBoxAppKey  (required)
   * @param keys  (required)
   * @return Map&lt;String, String&gt;
   * @throws ApiException if fails to make API call
   */
  public Map<String, String> readUserMeta(String deepBoxAppKey, List<String> keys) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxAppKey' is set
    if (deepBoxAppKey == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxAppKey' when calling readUserMeta");
    }
    // verify the required parameter 'keys' is set
    if (keys == null) {
      throw new ApiException(400, "Missing the required parameter 'keys' when calling readUserMeta");
    }
    // create path and map variables
    String localVarPath = "/api/v1/users/me/meta";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "keys", keys));

    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Map<String, String>> localVarReturnType = new GenericType<Map<String, String>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxAppKey  (optional)
   * @param metaKeys  (optional)
   * @return Me
   * @throws ApiException if fails to make API call
   */
  public Me usersMe(String deepBoxAppKey, List<String> metaKeys) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/api/v1/users/me";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "meta-keys", metaKeys));

    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Me> localVarReturnType = new GenericType<Me>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Set or update meta values
   * @param body  (required)
   * @param deepBoxAppKey  (required)
   * @throws ApiException if fails to make API call
   */
  public void writeUserMeta(Map<String, String> body, String deepBoxAppKey) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling writeUserMeta");
    }
    // verify the required parameter 'deepBoxAppKey' is set
    if (deepBoxAppKey == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxAppKey' when calling writeUserMeta");
    }
    // create path and map variables
    String localVarPath = "/api/v1/users/me/meta";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

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
}
