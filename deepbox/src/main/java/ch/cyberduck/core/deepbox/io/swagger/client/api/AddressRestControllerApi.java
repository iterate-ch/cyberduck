package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.Address;
import ch.cyberduck.core.deepbox.io.swagger.client.model.AddressAdd;
import ch.cyberduck.core.deepbox.io.swagger.client.model.AddressUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddressRestControllerApi {
  private ApiClient apiClient;

  public AddressRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AddressRestControllerApi(ApiClient apiClient) {
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
   * Create new address.
   * @param nodeId box, queue or any folder (required)
   * @param body  (optional)
   * @return Address
   * @throws ApiException if fails to make API call
   */
  public Address createAddress(String nodeId, AddressAdd body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling createAddress");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/addresses"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Address> localVarReturnType = new GenericType<Address>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param addressId  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteAddress(String addressId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'addressId' is set
    if (addressId == null) {
      throw new ApiException(400, "Missing the required parameter 'addressId' when calling deleteAddress");
    }
    // create path and map variables
    String localVarPath = "/api/v1/addresses/{addressId}"
      .replaceAll("\\{" + "addressId" + "\\}", apiClient.escapeString(addressId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param addressId  (required)
   * @return Address
   * @throws ApiException if fails to make API call
   */
  public Address getAddress(String addressId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'addressId' is set
    if (addressId == null) {
      throw new ApiException(400, "Missing the required parameter 'addressId' when calling getAddress");
    }
    // create path and map variables
    String localVarPath = "/api/v1/addresses/{addressId}"
      .replaceAll("\\{" + "addressId" + "\\}", apiClient.escapeString(addressId.toString()));

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

    GenericType<Address> localVarReturnType = new GenericType<Address>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param boxNodeId  (required)
   * @return List&lt;Address&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Address> listAllBoxAddresses(String boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listAllBoxAddresses");
    }
    // create path and map variables
    String localVarPath = "/api/v1/addresses/all";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "boxNodeId", boxNodeId));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<List<Address>> localVarReturnType = new GenericType<List<Address>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param body  (required)
   * @param addressId  (required)
   * @return Address
   * @throws ApiException if fails to make API call
   */
  public Address updateAddress(AddressUpdate body, String addressId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateAddress");
    }
    // verify the required parameter 'addressId' is set
    if (addressId == null) {
      throw new ApiException(400, "Missing the required parameter 'addressId' when calling updateAddress");
    }
    // create path and map variables
    String localVarPath = "/api/v1/addresses/{addressId}"
      .replaceAll("\\{" + "addressId" + "\\}", apiClient.escapeString(addressId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Address> localVarReturnType = new GenericType<Address>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
