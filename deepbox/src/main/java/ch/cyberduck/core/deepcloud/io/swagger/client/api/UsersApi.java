package ch.cyberduck.core.deepcloud.io.swagger.client.api;

import ch.cyberduck.core.deepcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.deepcloud.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepcloud.io.swagger.client.Configuration;
import ch.cyberduck.core.deepcloud.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepcloud.io.swagger.client.model.UserFull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UsersApi {
  private ApiClient apiClient;
  private Map<String, String> headers;

  public UsersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UsersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public void setHeadersOverrides(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * 
   * Endpoint to get the user details (name, email, ...), linked companies and units.  As a service, you can use the parameter &#x60;asSubjectId&#x60; to get other data on other objects using their UUID: - On users, to get the same info as they would get by calling this API. - On pending users, to get the companies that they are invited to and any available data. - On companies, to get information about the company (name, address, structure, ...) - On units, to get information about the unit (name, parent, address, ...)  Note: This feature (using this API to get info on subjects instead of on its own user) is not recommended. For almost any case, it is better to use the specific APIs that are specialized for this use. Example: using &#x60;/api/v1/users/{id}&#x60; instead of &#x60;/api/v1/users/me?asSubjectId&#x3D;{id}&#x60;.  **Caching Support**  Supports server-side caching via &#x60;cache-control&#x60; header:  *Cache-Control Directives:* - &#x60;max-age&#x60;: Cache duration in seconds, e.g. &#x27;max-age&#x3D;10&#x27;. &#x27;max-age&#x3D;0&#x27; disables caching. Limited to 30 seconds. - &#x60;no-store&#x60;: Prevents caching and removes existing cache entries. - &#x60;no-cache&#x60;: Refreshes and stores the updated response.  *Cache Key Composition:* - Request URL, query parameters, and a cache-id (default is the trace-id from the &#x60;sentry-trace&#x60; header). - If no Sentry trace-id is provided, the jwt &#x60;subject&#x60; will be used. - Optional: Custom cache-id via &#x60;x-cache-id&#x60; directive, e.g. &#x27;x-cache-id&#x3D;&lt;valid-uuid&gt;&#x27; (must be a UUID). - Optional: Enforce jwt subject usage with &#x60;x-use-subject&#x60; directive.  *Cache Status in Response Headers:* - &#x60;X-Cache: HIT&#x60;: Loaded from cache. - &#x60;X-Cache: MISS&#x60;: Not cached or cache expired.  *Cache-Control Header Example:* - Cache for 10 seconds: &#x60;cache-control: max-age&#x3D;10&#x60; - Custom cache-id: &#x60;cache-control: max-age&#x3D;10, x-cache-id&#x3D;&lt;uuid&gt;&#x60;
   * @return UserFull
   * @throws ApiException if fails to make API call
   */
  public UserFull usersMeList() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/api/v1/users/me";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UserFull> localVarReturnType = new GenericType<UserFull>() {};

    if (headers != null) {
      localVarHeaderParams.putAll(headers);
    }

    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
