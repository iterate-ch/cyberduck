package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import org.joda.time.DateTime;
import ch.cyberduck.core.storegate.io.swagger.client.model.Subscription;
import ch.cyberduck.core.storegate.io.swagger.client.model.TerminateSubscriptionRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdateSubscriptionRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpgradeSalepackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class SubscriptionApi {
  private ApiClient apiClient;

  public SubscriptionApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SubscriptionApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get current subscription
   * 
   * @return Subscription
   * @throws ApiException if fails to make API call
   */
  public Subscription subscriptionGetSubscription() throws ApiException {
    return subscriptionGetSubscriptionWithHttpInfo().getData();
      }

  /**
   * Get current subscription
   * 
   * @return ApiResponse&lt;Subscription&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Subscription> subscriptionGetSubscriptionWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/subscription";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Subscription> localVarReturnType = new GenericType<Subscription>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get current subscription end date
   * 
   * @return DateTime
   * @throws ApiException if fails to make API call
   */
  public DateTime subscriptionGetSubscriptionEndDate() throws ApiException {
    return subscriptionGetSubscriptionEndDateWithHttpInfo().getData();
      }

  /**
   * Get current subscription end date
   * 
   * @return ApiResponse&lt;DateTime&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DateTime> subscriptionGetSubscriptionEndDateWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/subscription/enddate";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DateTime> localVarReturnType = new GenericType<DateTime>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get a list of available upgrades
   * 
   * @return List&lt;UpgradeSalepackage&gt;
   * @throws ApiException if fails to make API call
   */
  public List<UpgradeSalepackage> subscriptionGetSubscriptionUpgrades() throws ApiException {
    return subscriptionGetSubscriptionUpgradesWithHttpInfo().getData();
      }

  /**
   * Get a list of available upgrades
   * 
   * @return ApiResponse&lt;List&lt;UpgradeSalepackage&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<UpgradeSalepackage>> subscriptionGetSubscriptionUpgradesWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/subscription/upgrades";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<List<UpgradeSalepackage>> localVarReturnType = new GenericType<List<UpgradeSalepackage>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Terminates the subscription
   * 
   * @param terminateSubscription  (required)
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean subscriptionTerminateSubscription(TerminateSubscriptionRequest terminateSubscription) throws ApiException {
    return subscriptionTerminateSubscriptionWithHttpInfo(terminateSubscription).getData();
      }

  /**
   * Terminates the subscription
   * 
   * @param terminateSubscription  (required)
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> subscriptionTerminateSubscriptionWithHttpInfo(TerminateSubscriptionRequest terminateSubscription) throws ApiException {
    Object localVarPostBody = terminateSubscription;
    
    // verify the required parameter 'terminateSubscription' is set
    if (terminateSubscription == null) {
      throw new ApiException(400, "Missing the required parameter 'terminateSubscription' when calling subscriptionTerminateSubscription");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/subscription";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Changes the subscription
   * 
   * @param updateSubscription  (required)
   * @return Subscription
   * @throws ApiException if fails to make API call
   */
  public Subscription subscriptionUpdateSubscription(UpdateSubscriptionRequest updateSubscription) throws ApiException {
    return subscriptionUpdateSubscriptionWithHttpInfo(updateSubscription).getData();
      }

  /**
   * Changes the subscription
   * 
   * @param updateSubscription  (required)
   * @return ApiResponse&lt;Subscription&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Subscription> subscriptionUpdateSubscriptionWithHttpInfo(UpdateSubscriptionRequest updateSubscription) throws ApiException {
    Object localVarPostBody = updateSubscription;
    
    // verify the required parameter 'updateSubscription' is set
    if (updateSubscription == null) {
      throw new ApiException(400, "Missing the required parameter 'updateSubscription' when calling subscriptionUpdateSubscription");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/subscription";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Subscription> localVarReturnType = new GenericType<Subscription>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
