package ch.cyberduck.core.eue.io.swagger.client.api;

import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.ApiClient;
import ch.cyberduck.core.eue.io.swagger.client.Configuration;
import ch.cyberduck.core.eue.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.eue.io.swagger.client.model.UserInfoResponseModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoApi {
  private ApiClient apiClient;

  public UserInfoApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UserInfoApi(ApiClient apiClient) {
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
   * Retrieve the metadata of a file or of a container.
   * @param cookie cookie (optional)
   * @param option decorator option (&lt; string &gt; array(csv)) (optional)
   * @return UserInfoResponseModel
   * @throws ApiException if fails to make API call
   */
  public UserInfoResponseModel userinfoGet(String cookie, String option) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/userinfo";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "option", option));

    if (cookie != null)
      localVarHeaderParams.put("cookie", apiClient.parameterToString(cookie));

    final String[] localVarAccepts = {
      "application/json;charset=utf-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "bearerAuth" };

    GenericType<UserInfoResponseModel> localVarReturnType = new GenericType<UserInfoResponseModel>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
