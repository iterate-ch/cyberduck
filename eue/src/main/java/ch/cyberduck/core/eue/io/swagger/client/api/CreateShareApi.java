package ch.cyberduck.core.eue.io.swagger.client.api;

import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.ApiClient;
import ch.cyberduck.core.eue.io.swagger.client.Configuration;
import ch.cyberduck.core.eue.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationRequestEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-10-14T22:10:10.297090+02:00[Europe/Zurich]")public class CreateShareApi {
  private ApiClient apiClient;

  public CreateShareApi() {
    this(Configuration.getDefaultApiClient());
  }

  public CreateShareApi(ApiClient apiClient) {
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
   * Create a share for a file or container
   * @param resourceId  (required)
   * @param body  (optional)
   * @param cookie cookie (optional)
   * @param option options to specify additional resource data (optional)
   * @return ShareCreationResponseModel
   * @throws ApiException if fails to make API call
   */
  public ShareCreationResponseModel resourceResourceIdSharePost(String resourceId, List<ShareCreationRequestEntry> body, String cookie, String option) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'resourceId' is set
    if (resourceId == null) {
      throw new ApiException(400, "Missing the required parameter 'resourceId' when calling resourceResourceIdSharePost");
    }
    // create path and map variables
    String localVarPath = "/resource/{resourceId}/share"
      .replaceAll("\\{" + "resourceId" + "\\}", apiClient.escapeString(resourceId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "option", option));

    if (cookie != null)
      localVarHeaderParams.put("cookie", apiClient.parameterToString(cookie));

    final String[] localVarAccepts = {
      "application/json:charset=utf-8", "application/json;charset=utf-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "bearerAuth" };

    GenericType<ShareCreationResponseModel> localVarReturnType = new GenericType<ShareCreationResponseModel>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
