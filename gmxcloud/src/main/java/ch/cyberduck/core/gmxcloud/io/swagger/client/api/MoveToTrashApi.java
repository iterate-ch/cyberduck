package ch.cyberduck.core.gmxcloud.io.swagger.client.api;

import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiClient;
import ch.cyberduck.core.gmxcloud.io.swagger.client.Configuration;
import ch.cyberduck.core.gmxcloud.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-10-14T22:10:10.297090+02:00[Europe/Zurich]")public class MoveToTrashApi {
  private ApiClient apiClient;

  public MoveToTrashApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MoveToTrashApi(ApiClient apiClient) {
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
   * Move resources to a container
   * @param body  (optional)
   * @param cookie cookie (optional)
   * @param ifMatch ifMatchHeader (optional)
   * @param autoRename (deprecated) flag for enforcing automatic rename on conflict (optional)
   * @param conflictResolution conflictResolution - overwrite or rename (optional)
   * @param lockToken the lock token used to access a locked resource (optional)
   * @throws ApiException if fails to make API call
   */
  public void resourceAliasTRASHChildrenMovePost(List<String> body, String cookie, String ifMatch, Boolean autoRename, String conflictResolution, String lockToken) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/resourceAlias/TRASH/children/move";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "autoRename", autoRename));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "conflictResolution", conflictResolution));
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

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
