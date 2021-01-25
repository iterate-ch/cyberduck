package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.DevicePinner;
import ch.cyberduck.core.box.io.swagger.client.model.DevicePinners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class DevicePinnersApi {
  private ApiClient apiClient;

  public DevicePinnersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DevicePinnersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove device pin
   * Deletes an individual device pin.
   * @param devicePinnerId The ID of the device pin (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteDevicePinnersId(String devicePinnerId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'devicePinnerId' is set
    if (devicePinnerId == null) {
      throw new ApiException(400, "Missing the required parameter 'devicePinnerId' when calling deleteDevicePinnersId");
    }
    // create path and map variables
    String localVarPath = "/device_pinners/{device_pinner_id}"
      .replaceAll("\\{" + "device_pinner_id" + "\\}", apiClient.escapeString(devicePinnerId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get device pin
   * Retrieves information about an individual device pin.
   * @param devicePinnerId The ID of the device pin (required)
   * @return DevicePinner
   * @throws ApiException if fails to make API call
   */
  public DevicePinner getDevicePinnersId(String devicePinnerId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'devicePinnerId' is set
    if (devicePinnerId == null) {
      throw new ApiException(400, "Missing the required parameter 'devicePinnerId' when calling getDevicePinnersId");
    }
    // create path and map variables
    String localVarPath = "/device_pinners/{device_pinner_id}"
      .replaceAll("\\{" + "device_pinner_id" + "\\}", apiClient.escapeString(devicePinnerId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<DevicePinner> localVarReturnType = new GenericType<DevicePinner>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * List enterprise device pins
   * Retrieves all the device pins within an enterprise.  The user must have admin privileges, and the application needs the \&quot;manage enterprise\&quot; scope to make this call.
   * @param enterpriseId The ID of the enterprise (required)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @param direction The direction to sort results in. This can be either in alphabetical ascending (&#x60;ASC&#x60;) or descending (&#x60;DESC&#x60;) order. (optional)
   * @return DevicePinners
   * @throws ApiException if fails to make API call
   */
  public DevicePinners getEnterprisesIdDevicePinners(String enterpriseId, String marker, Long limit, String direction) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'enterpriseId' is set
    if (enterpriseId == null) {
      throw new ApiException(400, "Missing the required parameter 'enterpriseId' when calling getEnterprisesIdDevicePinners");
    }
    // create path and map variables
    String localVarPath = "/enterprises/{enterprise_id}/device_pinners"
      .replaceAll("\\{" + "enterprise_id" + "\\}", apiClient.escapeString(enterpriseId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "direction", direction));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<DevicePinners> localVarReturnType = new GenericType<DevicePinners>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
