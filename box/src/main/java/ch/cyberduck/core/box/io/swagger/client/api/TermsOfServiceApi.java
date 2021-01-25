package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body65;
import ch.cyberduck.core.box.io.swagger.client.model.Body66;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.Task;
import ch.cyberduck.core.box.io.swagger.client.model.TermsOfService;
import ch.cyberduck.core.box.io.swagger.client.model.TermsOfServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class TermsOfServiceApi {
  private ApiClient apiClient;

  public TermsOfServiceApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TermsOfServiceApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List terms of services
   * Returns the current terms of service text and settings for the enterprise.
   * @param tosType Limits the results to the terms of service of the given type. (optional)
   * @return TermsOfServices
   * @throws ApiException if fails to make API call
   */
  public TermsOfServices getTermsOfServices(String tosType) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/terms_of_services";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "tos_type", tosType));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<TermsOfServices> localVarReturnType = new GenericType<TermsOfServices>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get terms of service
   * Fetches a specific terms of service.
   * @param termsOfServiceId The ID of the terms of service. (required)
   * @return TermsOfService
   * @throws ApiException if fails to make API call
   */
  public TermsOfService getTermsOfServicesId(String termsOfServiceId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'termsOfServiceId' is set
    if (termsOfServiceId == null) {
      throw new ApiException(400, "Missing the required parameter 'termsOfServiceId' when calling getTermsOfServicesId");
    }
    // create path and map variables
    String localVarPath = "/terms_of_services/{terms_of_service_id}"
      .replaceAll("\\{" + "terms_of_service_id" + "\\}", apiClient.escapeString(termsOfServiceId.toString()));

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

    GenericType<TermsOfService> localVarReturnType = new GenericType<TermsOfService>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create terms of service
   * Creates a terms of service for a given enterprise and type of user.
   * @param body  (optional)
   * @return Task
   * @throws ApiException if fails to make API call
   */
  public Task postTermsOfServices(Body65 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/terms_of_services";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Task> localVarReturnType = new GenericType<Task>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update terms of service
   * Updates a specific terms of service.
   * @param termsOfServiceId The ID of the terms of service. (required)
   * @param body  (optional)
   * @return TermsOfService
   * @throws ApiException if fails to make API call
   */
  public TermsOfService putTermsOfServicesId(String termsOfServiceId, Body66 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'termsOfServiceId' is set
    if (termsOfServiceId == null) {
      throw new ApiException(400, "Missing the required parameter 'termsOfServiceId' when calling putTermsOfServicesId");
    }
    // create path and map variables
    String localVarPath = "/terms_of_services/{terms_of_service_id}"
      .replaceAll("\\{" + "terms_of_service_id" + "\\}", apiClient.escapeString(termsOfServiceId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<TermsOfService> localVarReturnType = new GenericType<TermsOfService>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
