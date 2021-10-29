package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrashedItemsApi {
  private ApiClient apiClient;

  public TrashedItemsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TrashedItemsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List trashed items
   * Retrieves the files and folders that have been moved to the trash.  Any attribute in the full files or folders objects can be passed in with the &#x60;fields&#x60; parameter to retrieve those specific attributes that are not returned by default.  This endpoint defaults to use offset-based pagination, yet also supports marker-based pagination using the &#x60;marker&#x60; parameter.
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @param offset The offset of the item at which to begin the response. (optional, default to 0)
   * @param usemarker Specifies whether to use marker-based pagination instead of offset-based pagination. Only one pagination method can be used at a time.  By setting this value to true, the API will return a &#x60;marker&#x60; field that can be passed as a parameter to this endpoint to get the next page of the response. (optional)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param direction The direction to sort results in. This can be either in alphabetical ascending (&#x60;ASC&#x60;) or descending (&#x60;DESC&#x60;) order. (optional)
   * @param sort Defines the **second** attribute by which items are sorted.  Items are always sorted by their &#x60;type&#x60; first, with folders listed before files, and files listed before web links.  This parameter is not supported when using marker-based pagination. (optional)
   * @return Items
   * @throws ApiException if fails to make API call
   */
  public Items getFoldersTrashItems(List<String> fields, Long limit, Long offset, Boolean usemarker, String marker, String direction, String sort) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/folders/trash/items";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "usemarker", usemarker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "direction", direction));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Items> localVarReturnType = new GenericType<Items>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
