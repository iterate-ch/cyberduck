package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.SearchData;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchRestControllerApi {
  private ApiClient apiClient;

  public SearchRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SearchRestControllerApi(ApiClient apiClient) {
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
   * Query Search
   * @param parentNodeId Box or folder nodeId to query. (required)
   * @param q Search query (optional)
   * @param t Tags (optional)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @return SearchData
   * @throws ApiException if fails to make API call
   */
  public SearchData search(UUID parentNodeId, String q, List<String> t, Integer offset, Integer limit) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'parentNodeId' is set
    if (parentNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'parentNodeId' when calling search");
    }
    // create path and map variables
    String localVarPath = "/api/v1/search";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentNodeId", parentNodeId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q", q));
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "t", t));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<SearchData> localVarReturnType = new GenericType<SearchData>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
