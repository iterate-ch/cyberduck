package ch.cyberduck.core.eue.io.swagger.client.api;

import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.ApiClient;
import ch.cyberduck.core.eue.io.swagger.client.Configuration;
import ch.cyberduck.core.eue.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveChildrenForAliasApiApi {
  private ApiClient apiClient;

  public MoveChildrenForAliasApiApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MoveChildrenForAliasApiApi(ApiClient apiClient) {
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
   * @param alias id of the resource (resourceURI) (required)
   * @param body  (optional)
   * @param cookie cookie (optional)
   * @param ifMatch ifMatchHeader (optional)
   * @param autoRename (deprecated) flag for enforcing automatic rename on conflict (optional)
   * @param conflictResolution conflictResolution - overwrite or rename (optional)
   * @param lockToken the lock token used to access a locked resource (optional)
   * @return ResourceMoveResponseEntries
   * @throws ApiException if fails to make API call
   */
  public ResourceMoveResponseEntries resourceAliasAliasChildrenMovePost(String alias, List<String> body, String cookie, String ifMatch, Boolean autoRename, String conflictResolution, String lockToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'alias' is set
    if (alias == null) {
      throw new ApiException(400, "Missing the required parameter 'alias' when calling resourceAliasAliasChildrenMovePost");
    }
    // create path and map variables
    String localVarPath = "/resourceAlias/{alias}/children/move"
      .replaceAll("\\{" + "alias" + "\\}", apiClient.escapeString(alias.toString()));

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
      "application/json;charset=utf-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "bearerAuth" };

    GenericType<ResourceMoveResponseEntries> localVarReturnType = new GenericType<ResourceMoveResponseEntries>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
