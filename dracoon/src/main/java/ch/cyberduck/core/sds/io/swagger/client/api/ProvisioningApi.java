package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.AttributesResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateWebhookRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Customer;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerAttributes;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerList;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.EventTypeList;
import ch.cyberduck.core.sds.io.swagger.client.model.NewCustomerRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.NewCustomerResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateCustomerRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateCustomerResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateWebhookRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserList;
import ch.cyberduck.core.sds.io.swagger.client.model.Webhook;
import ch.cyberduck.core.sds.io.swagger.client.model.WebhookList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class ProvisioningApi {
  private ApiClient apiClient;

  public ProvisioningApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ProvisioningApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Create a new webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is created for given event types.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;customer.created&#x60;** | Triggered when a new customer is created | Tenant Webhook | | **&#x60;customer.deleted&#x60;** | Triggered when a user is deleted | Tenant Webhook | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Tenant Webhook | 
   * @param body body (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook createTenantWebhook(CreateWebhookRequest body, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    return createTenantWebhookWithHttpInfo(body, xSdsDateFormat, xSdsServiceToken).getData();
      }

  /**
   * Create tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Create a new webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is created for given event types.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;customer.created&#x60;** | Triggered when a new customer is created | Tenant Webhook | | **&#x60;customer.deleted&#x60;** | Triggered when a user is deleted | Tenant Webhook | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Tenant Webhook | 
   * @param body body (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> createTenantWebhookWithHttpInfo(CreateWebhookRequest body, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createTenantWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/webhooks";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete customer
   * ### Functional Description: Delete a customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: Customer is deleted.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCustomer(String xSdsServiceToken, Long customerId) throws ApiException {

    deleteCustomerWithHttpInfo(xSdsServiceToken, customerId);
  }

  /**
   * Delete customer
   * ### Functional Description: Delete a customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: Customer is deleted.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteCustomerWithHttpInfo(String xSdsServiceToken, Long customerId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling deleteCustomer");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling deleteCustomer");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description: Delete a custom customer attribute.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attribute gets deleted.  ### &amp;#9432; Further Information: * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param key Key (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCustomerAttributes(String xSdsServiceToken, Long customerId, String key) throws ApiException {

    deleteCustomerAttributesWithHttpInfo(xSdsServiceToken, customerId, key);
  }

  /**
   * Delete customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description: Delete a custom customer attribute.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attribute gets deleted.  ### &amp;#9432; Further Information: * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param key Key (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteCustomerAttributesWithHttpInfo(String xSdsServiceToken, Long customerId, String key) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling deleteCustomerAttributes");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling deleteCustomerAttributes");
    }
    
    // verify the required parameter 'key' is set
    if (key == null) {
      throw new ApiException(400, "Missing the required parameter 'key' when calling deleteCustomerAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/customerAttributes/{key}"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()))
      .replaceAll("\\{" + "key" + "\\}", apiClient.escapeString(key.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Delete a webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is deleted.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteTenantWebhook(Long webhookId, String xSdsServiceToken) throws ApiException {

    deleteTenantWebhookWithHttpInfo(webhookId, xSdsServiceToken);
  }

  /**
   * Delete tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Delete a webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is deleted.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteTenantWebhookWithHttpInfo(Long webhookId, String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling deleteTenantWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/webhooks/{webhook_id}"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get customer
   * ### Functional Description:   Receive details of a selected customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param includeAttributes Include custom customer attributes. (optional)
   * @return Customer
   * @throws ApiException if fails to make API call
   */
  public Customer getCustomer(String xSdsServiceToken, Long customerId, String xSdsDateFormat, Boolean includeAttributes) throws ApiException {
    return getCustomerWithHttpInfo(xSdsServiceToken, customerId, xSdsDateFormat, includeAttributes).getData();
      }

  /**
   * Get customer
   * ### Functional Description:   Receive details of a selected customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param includeAttributes Include custom customer attributes. (optional)
   * @return ApiResponse&lt;Customer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Customer> getCustomerWithHttpInfo(String xSdsServiceToken, Long customerId, String xSdsDateFormat, Boolean includeAttributes) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling getCustomer");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling getCustomer");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_attributes", includeAttributes));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Customer> localVarReturnType = new GenericType<Customer>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description:   Retrieve a list of customer attributes.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.   Right _\&quot;Read all customers\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: Filters are case insensitive.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;key:cn:searchString_1|value:cn:searchString_2&#x60;   Filter by attribute key contains &#x60;searchString_1&#x60; **AND** attribute value contains &#x60;searchString_2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;key&#x60;** | Customer attribute key filter | &#x60;cn, eq, sw&#x60; | Attribute key contains / equals / starts with value. | &#x60;search String&#x60; | | **&#x60;value&#x60;** | Customer attribute value filter | &#x60;cn, eq, sw&#x60; | Attribute value contains / equals / starts with value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;key:asc|value:desc&#x60;   Sort by &#x60;key&#x60; ascending **AND** by &#x60;value&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;key&#x60;** | Customer attribute key | | **&#x60;value&#x60;** | Customer attribute value |
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return AttributesResponse
   * @throws ApiException if fails to make API call
   */
  public AttributesResponse getCustomerAttributes(String xSdsServiceToken, Long customerId, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getCustomerAttributesWithHttpInfo(xSdsServiceToken, customerId, filter, limit, offset, sort).getData();
      }

  /**
   * Get customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description:   Retrieve a list of customer attributes.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.   Right _\&quot;Read all customers\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: Filters are case insensitive.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;key:cn:searchString_1|value:cn:searchString_2&#x60;   Filter by attribute key contains &#x60;searchString_1&#x60; **AND** attribute value contains &#x60;searchString_2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;key&#x60;** | Customer attribute key filter | &#x60;cn, eq, sw&#x60; | Attribute key contains / equals / starts with value. | &#x60;search String&#x60; | | **&#x60;value&#x60;** | Customer attribute value filter | &#x60;cn, eq, sw&#x60; | Attribute value contains / equals / starts with value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;key:asc|value:desc&#x60;   Sort by &#x60;key&#x60; ascending **AND** by &#x60;value&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;key&#x60;** | Customer attribute key | | **&#x60;value&#x60;** | Customer attribute value |
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;AttributesResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AttributesResponse> getCustomerAttributesWithHttpInfo(String xSdsServiceToken, Long customerId, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling getCustomerAttributes");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling getCustomerAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/customerAttributes"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AttributesResponse> localVarReturnType = new GenericType<AttributesResponse>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of customer users
   * ### Functional Description:   Receive a list of users associated with a certain customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;login&#x60;**, **&#x60;firstName&#x60;** and  **&#x60;lastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    ### Example: &gt; &#x60;login:cn:searchString_1|firstName:cn:searchString_2|lockStatus:eq:2&#x60;   Filter users by login contains &#x60;searchString_1&#x60; **OR** firstName contains &#x60;searchString_2&#x60; **AND** those who are **NOT** locked.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;email&#x60;** | Email filter | &#x60;eq&#x60;, &#x60;cn&#x60; | Email contains value. | &#x60;search String&#x60; | | **&#x60;userName&#x60;** | User name filter | &#x60;eq&#x60;, &#x60;cn&#x60; | UserName contains value. | &#x60;search String&#x60; | | **&#x60;firstName&#x60;** | User first name filter | &#x60;cn&#x60; | User first name contains value. | &#x60;search String&#x60; | | **&#x60;lastName&#x60;** | User last name filter | &#x60;cn&#x60; | User last name contains value. | &#x60;search String&#x60; | | **&#x60;isLocked&#x60;** | User lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectiveRoles&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE roles&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT roles&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE roles&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. user gets role **directly** granted from someone with _grant permission_ right.&lt;br&gt;EFFECTIVE means: e.g. user gets role through **group membership**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;createdAt&#x60;** | (**&#x60;NEW&#x60;**) Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;phone&#x60;** | (**&#x60;NEW&#x60;**) Phone filter | &#x60;eq&#x60; | Phone equals value. | &#x60;search String&#x60; | | **&#x60;isEncryptionEnabled&#x60;** | (**&#x60;NEW&#x60;**) Encryption status filter&lt;ul&gt;&lt;li&gt;client-side encryption&lt;/li&gt;&lt;li&gt;private key possession&lt;/li&gt;&lt;/ul&gt; | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;hasRole&#x60;** | (**&#x60;NEW&#x60;**) User role filter&lt;br&gt;Depends on **effectiveRoles**.&lt;br&gt;For more Roles information please call **&#x60;GET /roles API&#x60;** | &#x60;eq&#x60; | User role  equals value. | &lt;ul&gt;&lt;li&gt;&#x60;CONFIG_MANAGER&#x60; - Manage global configs&lt;/li&gt;&lt;li&gt;&#x60;USER_MANAGER&#x60; - Manage Users&lt;/li&gt;&lt;li&gt;&#x60;GROUP_MANAGER&#x60; - Manage User-Groups&lt;/li&gt;&lt;li&gt;&#x60;ROOM_MANAGER&#x60; - Manage top level Data Rooms&lt;/li&gt;&lt;li&gt;&#x60;LOG_AUDITOR&#x60; - Read logs&lt;/li&gt;&lt;li&gt;&#x60;NONMEMBER_VIEWER&#x60; - View users and groups when having room manage permission&lt;/li&gt;&lt;/ul&gt; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status filter | &#x60;eq&#x60; | User lock status equals value. | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - Locked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - Web access allowed&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - Web and mobile access allowed&lt;/li&gt;&lt;/ul&gt; | | **&#x60;login&#x60;** | (**&#x60;DEPRECATED&#x60;**) User login filter | &#x60;cn&#x60; | User login contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported. ### Example: &gt; &#x60;firstName:asc|lastLoginSuccessAt:desc&#x60;   Sort by &#x60;firstName&#x60; ascending **AND** by &#x60;lastLoginSuccessAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;userName&#x60;** | User name | | **&#x60;email&#x60;** | User email | | **&#x60;firstName&#x60;** | User first name | | **&#x60;lastName&#x60;** | User last name | | **&#x60;isLocked&#x60;** | User lock status | | **&#x60;lastLoginSuccessAt&#x60;** | Last successful login date | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;createdAt&#x60;** | (**&#x60;NEW&#x60;**) Creation date | | **&#x60;gender&#x60;** | (**&#x60;DEPRECATED&#x60;**) Gender | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status | | **&#x60;login&#x60;** | (**&#x60;DEPRECATED&#x60;**) User login | 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return UserList
   * @throws ApiException if fails to make API call
   */
  public UserList getCustomerUsers(String xSdsServiceToken, Long customerId, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getCustomerUsersWithHttpInfo(xSdsServiceToken, customerId, xSdsDateFormat, filter, limit, offset, sort).getData();
      }

  /**
   * Get list of customer users
   * ### Functional Description:   Receive a list of users associated with a certain customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;login&#x60;**, **&#x60;firstName&#x60;** and  **&#x60;lastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    ### Example: &gt; &#x60;login:cn:searchString_1|firstName:cn:searchString_2|lockStatus:eq:2&#x60;   Filter users by login contains &#x60;searchString_1&#x60; **OR** firstName contains &#x60;searchString_2&#x60; **AND** those who are **NOT** locked.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;email&#x60;** | Email filter | &#x60;eq&#x60;, &#x60;cn&#x60; | Email contains value. | &#x60;search String&#x60; | | **&#x60;userName&#x60;** | User name filter | &#x60;eq&#x60;, &#x60;cn&#x60; | UserName contains value. | &#x60;search String&#x60; | | **&#x60;firstName&#x60;** | User first name filter | &#x60;cn&#x60; | User first name contains value. | &#x60;search String&#x60; | | **&#x60;lastName&#x60;** | User last name filter | &#x60;cn&#x60; | User last name contains value. | &#x60;search String&#x60; | | **&#x60;isLocked&#x60;** | User lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectiveRoles&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE roles&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT roles&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE roles&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. user gets role **directly** granted from someone with _grant permission_ right.&lt;br&gt;EFFECTIVE means: e.g. user gets role through **group membership**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; | | **&#x60;createdAt&#x60;** | (**&#x60;NEW&#x60;**) Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;phone&#x60;** | (**&#x60;NEW&#x60;**) Phone filter | &#x60;eq&#x60; | Phone equals value. | &#x60;search String&#x60; | | **&#x60;isEncryptionEnabled&#x60;** | (**&#x60;NEW&#x60;**) Encryption status filter&lt;ul&gt;&lt;li&gt;client-side encryption&lt;/li&gt;&lt;li&gt;private key possession&lt;/li&gt;&lt;/ul&gt; | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;hasRole&#x60;** | (**&#x60;NEW&#x60;**) User role filter&lt;br&gt;Depends on **effectiveRoles**.&lt;br&gt;For more Roles information please call **&#x60;GET /roles API&#x60;** | &#x60;eq&#x60; | User role  equals value. | &lt;ul&gt;&lt;li&gt;&#x60;CONFIG_MANAGER&#x60; - Manage global configs&lt;/li&gt;&lt;li&gt;&#x60;USER_MANAGER&#x60; - Manage Users&lt;/li&gt;&lt;li&gt;&#x60;GROUP_MANAGER&#x60; - Manage User-Groups&lt;/li&gt;&lt;li&gt;&#x60;ROOM_MANAGER&#x60; - Manage top level Data Rooms&lt;/li&gt;&lt;li&gt;&#x60;LOG_AUDITOR&#x60; - Read logs&lt;/li&gt;&lt;li&gt;&#x60;NONMEMBER_VIEWER&#x60; - View users and groups when having room manage permission&lt;/li&gt;&lt;/ul&gt; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status filter | &#x60;eq&#x60; | User lock status equals value. | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - Locked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - Web access allowed&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - Web and mobile access allowed&lt;/li&gt;&lt;/ul&gt; | | **&#x60;login&#x60;** | (**&#x60;DEPRECATED&#x60;**) User login filter | &#x60;cn&#x60; | User login contains value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported. ### Example: &gt; &#x60;firstName:asc|lastLoginSuccessAt:desc&#x60;   Sort by &#x60;firstName&#x60; ascending **AND** by &#x60;lastLoginSuccessAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;userName&#x60;** | User name | | **&#x60;email&#x60;** | User email | | **&#x60;firstName&#x60;** | User first name | | **&#x60;lastName&#x60;** | User last name | | **&#x60;isLocked&#x60;** | User lock status | | **&#x60;lastLoginSuccessAt&#x60;** | Last successful login date | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;createdAt&#x60;** | (**&#x60;NEW&#x60;**) Creation date | | **&#x60;gender&#x60;** | (**&#x60;DEPRECATED&#x60;**) Gender | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status | | **&#x60;login&#x60;** | (**&#x60;DEPRECATED&#x60;**) User login | 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;UserList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UserList> getCustomerUsersWithHttpInfo(String xSdsServiceToken, Long customerId, String xSdsDateFormat, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling getCustomerUsers");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling getCustomerUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/users"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UserList> localVarReturnType = new GenericType<UserList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of customers
   * ### Functional Description:   Receive a list of customers.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: This list returns a maximum of **1000** entries.    ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;trialDaysLeft:le:10|userMax:le:100&#x60;   Get all customers with &#x60;10&#x60; trial days left **AND** user maximum **&lt;&#x3D;** &#x60;100&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;id&#x60;** | Customer ID filter | &#x60;eq&#x60; | Customer ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;companyName&#x60;** | Company name filter | &#x60;cn&#x60; | Company name contains value. | &#x60;search String&#x60; | | **&#x60;customerContractType&#x60;** | Customer contract type filter | &#x60;eq&#x60; | Customer contract type equals value. | &lt;ul&gt;&lt;li&gt;&#x60;demo&#x60;&lt;/li&gt;&lt;li&gt;&#x60;free&#x60;&lt;/li&gt;&lt;li&gt;&#x60;pay&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;trialDaysLeft&#x60;** | Left trial days filter | &#x60;ge, le&#x60; | Left trial days are greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;trialDaysLeft:ge:5&#x60;&amp;#124;&#x60;trialDaysLeft:le:10&#x60; | | **&#x60;providerCustomerId&#x60;** | Provider Customer ID filter | &#x60;cn, eq&#x60; | Provider Customer ID contains / equals value. | &#x60;search String&#x60; | | **&#x60;quotaMax&#x60;** | Maximum quota filter | &#x60;ge, le&#x60; | Maximum quota is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;quotaMax:ge:1024&#x60;&amp;#124;&#x60;quotaMax:le:1073741824&#x60; | &#x60;positive Integer&#x60; | | **&#x60;quotaUsed&#x60;** | Used quota filter | &#x60;ge, le&#x60; | Used quota is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;quotaUsed:ge:1024&#x60;&amp;#124;&#x60;quotaUsed:le:1073741824&#x60; | &#x60;positive Integer&#x60; | | **&#x60;userMax&#x60;** | User maximum filter | &#x60;ge, le&#x60; | User maxiumum is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;userMax:ge:10&#x60;&amp;#124;&#x60;userMax:le:100&#x60; | &#x60;positive Integer&#x60; | | **&#x60;userUsed&#x60;** | Number of registered users filter | &#x60;ge, le&#x60; | Number of registered users is is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;userUsed:ge:10&#x60;&amp;#124;&#x60;userUsed:le:100&#x60; | &#x60;positive Integer&#x60; | | **&#x60;isLocked&#x60;** | Lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;lastLoginAt&#x60;** | Last login date filter | &#x60;ge, le&#x60; | Last login date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;lastLoginAt:ge:2016-12-31&#x60;&amp;#124;&#x60;lastLoginAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;userLogin&#x60;** | User login filter | &#x60;eq&#x60; | User login name equals value.&lt;br&gt;Search user all logins e.g. &#x60;basic&#x60;, &#x60;active_directory&#x60;, &#x60;radius&#x60;. | &#x60;search String&#x60; | | **&#x60;attributeKey&#x60;** | Customer attribute key filter | &#x60;eq&#x60;, &#x60;nex&#x60; | Customer attribute key equals value / Customer attribute does **NOT** exist at customer | &#x60;search String&#x60; | | **&#x60;attributeValue&#x60;** | Customer attribute value filter | &#x60;eq&#x60; | Customer attribute value equals value. | &#x60;search String&#x60; | | **&#x60;activationCode&#x60;** | (**&#x60;DEPRECATED&#x60;**) Activation code filter | &#x60;cn, eq&#x60; | Activation code contains / equals value. | &#x60;search String&#x60; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) Lock status filter | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - unlocked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - locked&lt;/li&gt;&lt;/ul&gt; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;companyName:desc&#x60;   Sort by &#x60;companyName&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;companyName&#x60;** | Company name | | **&#x60;customerContractType&#x60;** | Customer contract type | | **&#x60;trialDaysLeft&#x60;** | Number of remaining trial days (demo customers) | | **&#x60;providerCustomerId&#x60;** | Provider Customer ID | | **&#x60;quotaMax&#x60;** | Maximum quota | | **&#x60;quotaUsed&#x60;** | Currently used quota | | **&#x60;userMax&#x60;** | Maximum user number | | **&#x60;userUsed&#x60;** | Number of registered users | | **&#x60;isLocked&#x60;** | Lock status of customer | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;lastLoginAt&#x60;** | Last login date of any user of this customer | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) Lock status of customer | 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param includeAttributes Include custom customer attributes. (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return CustomerList
   * @throws ApiException if fails to make API call
   */
  public CustomerList getCustomers(String xSdsServiceToken, String xSdsDateFormat, String filter, Boolean includeAttributes, Integer limit, Integer offset, String sort) throws ApiException {
    return getCustomersWithHttpInfo(xSdsServiceToken, xSdsDateFormat, filter, includeAttributes, limit, offset, sort).getData();
      }

  /**
   * Get list of customers
   * ### Functional Description:   Receive a list of customers.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: This list returns a maximum of **1000** entries.    ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;trialDaysLeft:le:10|userMax:le:100&#x60;   Get all customers with &#x60;10&#x60; trial days left **AND** user maximum **&lt;&#x3D;** &#x60;100&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;id&#x60;** | Customer ID filter | &#x60;eq&#x60; | Customer ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;companyName&#x60;** | Company name filter | &#x60;cn&#x60; | Company name contains value. | &#x60;search String&#x60; | | **&#x60;customerContractType&#x60;** | Customer contract type filter | &#x60;eq&#x60; | Customer contract type equals value. | &lt;ul&gt;&lt;li&gt;&#x60;demo&#x60;&lt;/li&gt;&lt;li&gt;&#x60;free&#x60;&lt;/li&gt;&lt;li&gt;&#x60;pay&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;trialDaysLeft&#x60;** | Left trial days filter | &#x60;ge, le&#x60; | Left trial days are greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;trialDaysLeft:ge:5&#x60;&amp;#124;&#x60;trialDaysLeft:le:10&#x60; | | **&#x60;providerCustomerId&#x60;** | Provider Customer ID filter | &#x60;cn, eq&#x60; | Provider Customer ID contains / equals value. | &#x60;search String&#x60; | | **&#x60;quotaMax&#x60;** | Maximum quota filter | &#x60;ge, le&#x60; | Maximum quota is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;quotaMax:ge:1024&#x60;&amp;#124;&#x60;quotaMax:le:1073741824&#x60; | &#x60;positive Integer&#x60; | | **&#x60;quotaUsed&#x60;** | Used quota filter | &#x60;ge, le&#x60; | Used quota is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;quotaUsed:ge:1024&#x60;&amp;#124;&#x60;quotaUsed:le:1073741824&#x60; | &#x60;positive Integer&#x60; | | **&#x60;userMax&#x60;** | User maximum filter | &#x60;ge, le&#x60; | User maxiumum is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;userMax:ge:10&#x60;&amp;#124;&#x60;userMax:le:100&#x60; | &#x60;positive Integer&#x60; | | **&#x60;userUsed&#x60;** | Number of registered users filter | &#x60;ge, le&#x60; | Number of registered users is is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;userUsed:ge:10&#x60;&amp;#124;&#x60;userUsed:le:100&#x60; | &#x60;positive Integer&#x60; | | **&#x60;isLocked&#x60;** | Lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;lastLoginAt&#x60;** | Last login date filter | &#x60;ge, le&#x60; | Last login date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;lastLoginAt:ge:2016-12-31&#x60;&amp;#124;&#x60;lastLoginAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;userLogin&#x60;** | User login filter | &#x60;eq&#x60; | User login name equals value.&lt;br&gt;Search user all logins e.g. &#x60;basic&#x60;, &#x60;active_directory&#x60;, &#x60;radius&#x60;. | &#x60;search String&#x60; | | **&#x60;attributeKey&#x60;** | Customer attribute key filter | &#x60;eq&#x60;, &#x60;nex&#x60; | Customer attribute key equals value / Customer attribute does **NOT** exist at customer | &#x60;search String&#x60; | | **&#x60;attributeValue&#x60;** | Customer attribute value filter | &#x60;eq&#x60; | Customer attribute value equals value. | &#x60;search String&#x60; | | **&#x60;activationCode&#x60;** | (**&#x60;DEPRECATED&#x60;**) Activation code filter | &#x60;cn, eq&#x60; | Activation code contains / equals value. | &#x60;search String&#x60; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) Lock status filter | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - unlocked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - locked&lt;/li&gt;&lt;/ul&gt; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;companyName:desc&#x60;   Sort by &#x60;companyName&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;companyName&#x60;** | Company name | | **&#x60;customerContractType&#x60;** | Customer contract type | | **&#x60;trialDaysLeft&#x60;** | Number of remaining trial days (demo customers) | | **&#x60;providerCustomerId&#x60;** | Provider Customer ID | | **&#x60;quotaMax&#x60;** | Maximum quota | | **&#x60;quotaUsed&#x60;** | Currently used quota | | **&#x60;userMax&#x60;** | Maximum user number | | **&#x60;userUsed&#x60;** | Number of registered users | | **&#x60;isLocked&#x60;** | Lock status of customer | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;lastLoginAt&#x60;** | Last login date of any user of this customer | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) Lock status of customer | 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param includeAttributes Include custom customer attributes. (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;CustomerList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<CustomerList> getCustomersWithHttpInfo(String xSdsServiceToken, String xSdsDateFormat, String filter, Boolean includeAttributes, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling getCustomers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_attributes", includeAttributes));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<CustomerList> localVarReturnType = new GenericType<CustomerList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of event types 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of available event types.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: List of available event types is returned.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return EventTypeList
   * @throws ApiException if fails to make API call
   */
  public EventTypeList getListOfEventTypesForTenant(String xSdsServiceToken) throws ApiException {
    return getListOfEventTypesForTenantWithHttpInfo(xSdsServiceToken).getData();
      }

  /**
   * Get list of event types 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of available event types.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: List of available event types is returned.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return ApiResponse&lt;EventTypeList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<EventTypeList> getListOfEventTypesForTenantWithHttpInfo(String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/webhooks/event_types";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<EventTypeList> localVarReturnType = new GenericType<EventTypeList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get list of tenant webhooks 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of webhooks for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: List of webhooks is returned.   ### &amp;#9432; Further Information:   Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  &#x60;EncryptionInfo&#x60; is **NOT** provided.   Wildcard character is the asterisk character: **&#x60;*&#x60;**  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;name:cn:goo|createdAt:ge:2015-01-01&#x60;   Get webhooks where name contains &#x60;goo&#x60; **AND** webhook creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;id&#x60;** | Webhook id filter | &#x60;eq&#x60; | Webhook id equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**). |&#x60;positive number&#x60;| | **&#x60;name&#x60;** | Webhook type name| &#x60;cn, eq&#x60; | Webhook name contains / equals value. | &#x60;search String&#x60; | | **&#x60;isEnabled&#x60;** | Webhook isEnabled filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;expiration&#x60;** | Expiration date filter | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expiration:ge:2016-12-31&#x60;&amp;#124;&#x60;expiration:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;lastFailStatus&#x60;** | Failure status filter | &#x60;eq&#x60; | Last HTTP status code. Set when a webhook is auto-disabled due to repeated delivery failures |&#x60;positive number&#x60;|   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;id&#x60;** | Webhook id | | **&#x60;name&#x60;** | Webhook name | | **&#x60;isEnabled&#x60;** | Webhook isEnabled | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;expiration&#x60;** | Expiration date | 
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return WebhookList
   * @throws ApiException if fails to make API call
   */
  public WebhookList getListOfTenantWebhooks(String xSdsDateFormat, String xSdsServiceToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    return getListOfTenantWebhooksWithHttpInfo(xSdsDateFormat, xSdsServiceToken, filter, limit, offset, sort).getData();
      }

  /**
   * Get list of tenant webhooks 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a list of webhooks for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: List of webhooks is returned.   ### &amp;#9432; Further Information:   Output is limited to **500** entries.   For more results please use filter criteria and paging (&#x60;offset&#x60; + &#x60;limit&#x60;).  &#x60;EncryptionInfo&#x60; is **NOT** provided.   Wildcard character is the asterisk character: **&#x60;*&#x60;**  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**)   Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;name:cn:goo|createdAt:ge:2015-01-01&#x60;   Get webhooks where name contains &#x60;goo&#x60; **AND** webhook creation date is **&gt;&#x3D;** &#x60;2015-01-01&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;id&#x60;** | Webhook id filter | &#x60;eq&#x60; | Webhook id equals value.&lt;br&gt;Multiple values are allowed and will be connected via logical disjunction (**OR**). |&#x60;positive number&#x60;| | **&#x60;name&#x60;** | Webhook type name| &#x60;cn, eq&#x60; | Webhook name contains / equals value. | &#x60;search String&#x60; | | **&#x60;isEnabled&#x60;** | Webhook isEnabled filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;expiration&#x60;** | Expiration date filter | &#x60;ge, le&#x60; | Expiration date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;expiration:ge:2016-12-31&#x60;&amp;#124;&#x60;expiration:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;lastFailStatus&#x60;** | Failure status filter | &#x60;eq&#x60; | Last HTTP status code. Set when a webhook is auto-disabled due to repeated delivery failures |&#x60;positive number&#x60;|   ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;name:desc&#x60;   Sort by &#x60;name&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;id&#x60;** | Webhook id | | **&#x60;name&#x60;** | Webhook name | | **&#x60;isEnabled&#x60;** | Webhook isEnabled | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;expiration&#x60;** | Expiration date | 
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @param filter Filter string (optional)
   * @param limit Range limit. Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param offset Range offset (optional)
   * @param sort Sort string (optional)
   * @return ApiResponse&lt;WebhookList&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookList> getListOfTenantWebhooksWithHttpInfo(String xSdsDateFormat, String xSdsServiceToken, String filter, Integer limit, Integer offset, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/webhooks";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<WebhookList> localVarReturnType = new GenericType<WebhookList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a specific webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is returned.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook getTenantWebhook(Long webhookId, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    return getTenantWebhookWithHttpInfo(webhookId, xSdsDateFormat, xSdsServiceToken).getData();
      }

  /**
   * Get tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Get a specific webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is returned.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> getTenantWebhookWithHttpInfo(Long webhookId, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling getTenantWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/webhooks/{webhook_id}"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create customer
   * ### Functional Description: Create a new customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.    ### Effects: A new customer is created.  ### &amp;#9432; Further Information: If no company name is set, first letter of the first name separated by dot following by last name of the first administrator is used (e.g. **&#x60;J.Doe&#x60;**).   Max quota has to be at least &#x60;1 MB&#x60; (&#x3D; &#x60;1.048.576 B&#x60;).  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;basic&#x60;** / **&#x60;sql&#x60;** | &#x60;username&#x60; | Unique user identifier | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; (optional) | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |  &amp;#9888; If &#x60;basic&#x60; authentication is enabled, the first administrator will get &#x60;basic&#x60; authentication by default.   To create a first administrator without &#x60;basic&#x60; authentication it **MUST** be disabled explicitly. 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return NewCustomerResponse
   * @throws ApiException if fails to make API call
   */
  public NewCustomerResponse newCustomerRequest(String xSdsServiceToken, NewCustomerRequest body, String xSdsDateFormat) throws ApiException {
    return newCustomerRequestWithHttpInfo(xSdsServiceToken, body, xSdsDateFormat).getData();
      }

  /**
   * Create customer
   * ### Functional Description: Create a new customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.    ### Effects: A new customer is created.  ### &amp;#9432; Further Information: If no company name is set, first letter of the first name separated by dot following by last name of the first administrator is used (e.g. **&#x60;J.Doe&#x60;**).   Max quota has to be at least &#x60;1 MB&#x60; (&#x3D; &#x60;1.048.576 B&#x60;).  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;basic&#x60;** / **&#x60;sql&#x60;** | &#x60;username&#x60; | Unique user identifier | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; (optional) | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |  &amp;#9888; If &#x60;basic&#x60; authentication is enabled, the first administrator will get &#x60;basic&#x60; authentication by default.   To create a first administrator without &#x60;basic&#x60; authentication it **MUST** be disabled explicitly. 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;NewCustomerResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NewCustomerResponse> newCustomerRequestWithHttpInfo(String xSdsServiceToken, NewCustomerRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling newCustomerRequest");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling newCustomerRequest");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<NewCustomerResponse> localVarReturnType = new GenericType<NewCustomerResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Reset tenant webhook lifetime 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Reset the lifetime of a webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Lifetime of the webhook is reset.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook resetTenantWebhookLifetime(Long webhookId, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    return resetTenantWebhookLifetimeWithHttpInfo(webhookId, xSdsDateFormat, xSdsServiceToken).getData();
      }

  /**
   * Reset tenant webhook lifetime 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Reset the lifetime of a webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Lifetime of the webhook is reset.  ### &amp;#9432; Further Information: None.
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> resetTenantWebhookLifetimeWithHttpInfo(Long webhookId, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling resetTenantWebhookLifetime");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/webhooks/{webhook_id}/reset_lifetime"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Set customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description:   Set custom customer attributes.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attributes gets set.  ### &amp;#9432; Further Information: Batch function.   All existing customer attributes will be deleted.    * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**. 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Customer
   * @throws ApiException if fails to make API call
   */
  public Customer setAllCustomerAttributes(String xSdsServiceToken, CustomerAttributes body, Long customerId, String xSdsDateFormat) throws ApiException {
    return setAllCustomerAttributesWithHttpInfo(xSdsServiceToken, body, customerId, xSdsDateFormat).getData();
      }

  /**
   * Set customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description:   Set custom customer attributes.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attributes gets set.  ### &amp;#9432; Further Information: Batch function.   All existing customer attributes will be deleted.    * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**. 
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Customer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Customer> setAllCustomerAttributesWithHttpInfo(String xSdsServiceToken, CustomerAttributes body, Long customerId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling setAllCustomerAttributes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setAllCustomerAttributes");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling setAllCustomerAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/customerAttributes"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Customer> localVarReturnType = new GenericType<Customer>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Add or edit customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description:   Add or edit custom customer attributes.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attributes get added or edited.  ### &amp;#9432; Further Information: Batch function.   If an entry exists before, it will be overwritten.    * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return Customer
   * @throws ApiException if fails to make API call
   */
  public Customer setCustomerAttributes(String xSdsServiceToken, CustomerAttributes body, Long customerId, String xSdsDateFormat) throws ApiException {
    return setCustomerAttributesWithHttpInfo(xSdsServiceToken, body, customerId, xSdsDateFormat).getData();
      }

  /**
   * Add or edit customer attributes
   * ### &amp;#128640; Since version 4.4.0  ### Functional Description:   Add or edit custom customer attributes.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attributes get added or edited.  ### &amp;#9432; Further Information: Batch function.   If an entry exists before, it will be overwritten.    * Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   * Characters are **case-insensitive**.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;Customer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Customer> setCustomerAttributesWithHttpInfo(String xSdsServiceToken, CustomerAttributes body, Long customerId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling setCustomerAttributes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setCustomerAttributes");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling setCustomerAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/customerAttributes"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Customer> localVarReturnType = new GenericType<Customer>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update customer
   * ### Functional Description:   Change selected attributes of a customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: Update of attributes.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return UpdateCustomerResponse
   * @throws ApiException if fails to make API call
   */
  public UpdateCustomerResponse updateCustomer(String xSdsServiceToken, UpdateCustomerRequest body, Long customerId, String xSdsDateFormat) throws ApiException {
    return updateCustomerWithHttpInfo(xSdsServiceToken, body, customerId, xSdsDateFormat).getData();
      }

  /**
   * Update customer
   * ### Functional Description:   Change selected attributes of a customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: Update of attributes.  ### &amp;#9432; Further Information: None.
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body body (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @return ApiResponse&lt;UpdateCustomerResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UpdateCustomerResponse> updateCustomerWithHttpInfo(String xSdsServiceToken, UpdateCustomerRequest body, Long customerId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling updateCustomer");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateCustomer");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling updateCustomer");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UpdateCustomerResponse> localVarReturnType = new GenericType<UpdateCustomerResponse>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Update an existing webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is updated.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;customer.created&#x60;** | Triggered when a new customer is created | Tenant Webhook | | **&#x60;customer.deleted&#x60;** | Triggered when a user is deleted | Tenant Webhook | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Tenant Webhook | 
   * @param body body (required)
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return Webhook
   * @throws ApiException if fails to make API call
   */
  public Webhook updateTenantWebhook(UpdateWebhookRequest body, Long webhookId, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    return updateTenantWebhookWithHttpInfo(body, webhookId, xSdsDateFormat, xSdsServiceToken).getData();
      }

  /**
   * Update tenant webhook 🞂 NEW 🞀
   * ### &amp;#128640; Since version 4.19.0  ### Functional Description:   Update an existing webhook for the tenant scope.  ### Precondition: Right _\&quot;manage webhook\&quot;_ required.  ### Effects: Webhook is updated.  ### &amp;#9432; Further Information: URL must begin with the &#x60;HTTPS&#x60; scheme. Webhook names are limited to 150 characters.  ### Available event types  | Name | Description | Scope | | :--- | :--- | :--- | | **&#x60;customer.created&#x60;** | Triggered when a new customer is created | Tenant Webhook | | **&#x60;customer.deleted&#x60;** | Triggered when a user is deleted | Tenant Webhook | | **&#x60;webhook.expiring&#x60;** | Triggered 30/20/10/1 days before a webhook expires |  Tenant Webhook | 
   * @param body body (required)
   * @param webhookId Unique identifier for a webhook (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsServiceToken Service Authentication token (optional)
   * @return ApiResponse&lt;Webhook&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Webhook> updateTenantWebhookWithHttpInfo(UpdateWebhookRequest body, Long webhookId, String xSdsDateFormat, String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateTenantWebhook");
    }
    
    // verify the required parameter 'webhookId' is set
    if (webhookId == null) {
      throw new ApiException(400, "Missing the required parameter 'webhookId' when calling updateTenantWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/webhooks/{webhook_id}"
      .replaceAll("\\{" + "webhook_id" + "\\}", apiClient.escapeString(webhookId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Webhook> localVarReturnType = new GenericType<Webhook>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
