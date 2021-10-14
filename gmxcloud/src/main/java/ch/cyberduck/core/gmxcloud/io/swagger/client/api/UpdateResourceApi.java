package ch.cyberduck.core.gmxcloud.io.swagger.client.api;

import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiClient;
import ch.cyberduck.core.gmxcloud.io.swagger.client.Configuration;
import ch.cyberduck.core.gmxcloud.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceUpdateModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-10-14T22:10:10.297090+02:00[Europe/Zurich]")public class UpdateResourceApi {
  private ApiClient apiClient;

  public UpdateResourceApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UpdateResourceApi(ApiClient apiClient) {
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
   * Change properties of resources.
   * @param resourceId id of the resource (resourceURI) (required)
   * @param body  (optional)
   * @param cookie cookie (optional)
   * @param ifMatch ifMatchHeader (optional)
   * @param lockToken  (optional)
   * @throws ApiException if fails to make API call
   */
  public void resourceResourceIdPatch(String resourceId, ResourceUpdateModel body, String cookie, String ifMatch, String lockToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'resourceId' is set
    if (resourceId == null) {
      throw new ApiException(400, "Missing the required parameter 'resourceId' when calling resourceResourceIdPatch");
    }
    // create path and map variables
    String localVarPath = "/resource/{resourceId}"
      .replaceAll("\\{" + "resourceId" + "\\}", apiClient.escapeString(resourceId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "lockToken", lockToken));

    if (cookie != null)
      localVarHeaderParams.put("cookie", apiClient.parameterToString(cookie));
    if (ifMatch != null)
      localVarHeaderParams.put("If-Match", apiClient.parameterToString(ifMatch));

    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "bearerAuth" };

    apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
