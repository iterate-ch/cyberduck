package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.SettingsChangeEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")public class SettingsChangesApi {
  private ApiClient apiClient;

  public SettingsChangesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SettingsChangesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List Settings Changes
   * List Settings Changes
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;api_key_id&#x60;, &#x60;created_at&#x60; or &#x60;user_id&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;api_key_id&#x60; and &#x60;user_id&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;api_key_id&#x60; and &#x60;user_id&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;api_key_id&#x60; and &#x60;user_id&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;api_key_id&#x60; and &#x60;user_id&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;api_key_id&#x60; and &#x60;user_id&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;api_key_id&#x60; and &#x60;user_id&#x60;. (optional)
   * @return List&lt;SettingsChangeEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<SettingsChangeEntity> getSettingsChanges(String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/settings_changes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<SettingsChangeEntity>> localVarReturnType = new GenericType<List<SettingsChangeEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
