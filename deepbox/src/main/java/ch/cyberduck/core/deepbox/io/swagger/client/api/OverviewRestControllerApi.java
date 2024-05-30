package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.BoxInfo;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxOverview;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Overview;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OverviewRestControllerApi {
  private ApiClient apiClient;

  public OverviewRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public OverviewRestControllerApi(ApiClient apiClient) {
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
   * @param boxNodeId  (required)
   * @return BoxInfo
   * @throws ApiException if fails to make API call
   */
  public BoxInfo getBoxInfo(UUID boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling getBoxInfo");
    }
    // create path and map variables
    String localVarPath = "/api/v1/boxes/{boxNodeId}/info"
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<BoxInfo> localVarReturnType = new GenericType<BoxInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param q Filter (optional)
   * @return DeepBoxOverview
   * @throws ApiException if fails to make API call
   */
  public DeepBoxOverview getDeepBoxOverview(UUID deepBoxNodeId, Integer offset, Integer limit, String q) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling getDeepBoxOverview");
    }
    // create path and map variables
    String localVarPath = "/api/v1/overview/deepBoxes/{deepBoxNodeId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q", q));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<DeepBoxOverview> localVarReturnType = new GenericType<DeepBoxOverview>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param companyId  (optional)
   * @param deepBoxesBoxesLimit  (optional, default to 10)
   * @param q Filter (optional)
   * @return Overview
   * @throws ApiException if fails to make API call
   */
  public Overview getOverview(UUID companyId, Integer deepBoxesBoxesLimit, String q) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/api/v1/overview";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "companyId", companyId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deepBoxes.boxes.limit", deepBoxesBoxesLimit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q", q));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Overview> localVarReturnType = new GenericType<Overview>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
