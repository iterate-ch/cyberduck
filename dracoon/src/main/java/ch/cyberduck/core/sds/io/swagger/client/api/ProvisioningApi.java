package ch.cyberduck.core.sds.io.swagger.client.api;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.Customer;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerAttributes;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerList;
import ch.cyberduck.core.sds.io.swagger.client.model.NewCustomerRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.NewCustomerResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateCustomerRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateCustomerResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UserList;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
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
   * Delete customer
   * ### Functional Description: Delete a customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: Customer is deleted.  ### &amp;#9432; Further Information: None.
   * @param customerId Customer ID (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCustomer(Long customerId, String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling deleteCustomer");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling deleteCustomer");
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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete customer attributes
   * ### Functional Description: Delete custom customer attribute.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attribute gets deleted.  ### &amp;#9432; Further Information: Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   Characters are case-insensitive.
   * @param customerId Customer ID (required)
   * @param key Key (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCustomerAttributes(Long customerId, String key, String xSdsServiceToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling deleteCustomerAttributes");
    }
    
    // verify the required parameter 'key' is set
    if (key == null) {
      throw new ApiException(400, "Missing the required parameter 'key' when calling deleteCustomerAttributes");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling deleteCustomerAttributes");
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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get customer
   * ### Functional Description:   Receive details of a selected customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param customerId Customer ID (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @param includeAttributes Include custom customer attributes. (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Customer
   * @throws ApiException if fails to make API call
   */
  public Customer getCustomer(Long customerId, String xSdsServiceToken, Boolean includeAttributes, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling getCustomer");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling getCustomer");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_attributes", includeAttributes));

    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
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
   * Get customer users
   * ### Functional Description:   Receive a list of users associated with a certain customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) ### &amp;#9888; Except for **&#x60;login&#x60;**, **&#x60;firstName&#x60;** and  **&#x60;lastName&#x60;** - these are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    Example: &gt; &#x60;login:cn:searchString_1|firstName:cn:searchString_2&#x60;   Filter users by login containing &#x60;searchString_1&#x60; **OR** first name containing &#x60;searchString_2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;login&#x60;** | User login filter | &#x60;cn&#x60; | User login contains value. | &#x60;search String&#x60; | | **&#x60;firstName&#x60;** | User first name filter | &#x60;cn&#x60; | User first name contains value. | &#x60;search String&#x60; | | **&#x60;lastName&#x60;** | User last name filter | &#x60;cn&#x60; | User last name contains value. | &#x60;search String&#x60; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status filter | &#x60;eq&#x60; | User lock status equals value. | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - Locked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - Web access allowed&lt;/li&gt;&lt;li&gt;&#x60;2&#x60; - Web and mobile access allowed&lt;/li&gt;&lt;/ul&gt; | | **&#x60;isLocked&#x60;** | Lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;effectiveRoles&#x60;** | Filter users with DIRECT or DIRECT **AND** EFFECTIVE roles&lt;ul&gt;&lt;li&gt;&#x60;false&#x60;: DIRECT roles&lt;/li&gt;&lt;li&gt;&#x60;true&#x60;: DIRECT **AND** EFFECTIVE roles&lt;/li&gt;&lt;/ul&gt;DIRECT means: e.g. user gets role **directly** granted from someone with _grant permission_ right.&lt;br&gt;EFFECTIVE means: e.g. user gets role through **group membership**. | &#x60;eq&#x60; |  | &#x60;true or false&#x60;&lt;br&gt;default: &#x60;false&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.   Example: &gt; &#x60;firstName:asc|lastLoginSuccessAt:desc&#x60;   Sort by &#x60;firstName&#x60; ascending **AND** by &#x60;lastLoginSuccessAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;login&#x60;** | User login | | **&#x60;firstName&#x60;** | User first name | | **&#x60;lastName&#x60;** | User last name | | **&#x60;gender&#x60;** | Gender | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) User lock status | | **&#x60;lastLoginSuccessAt&#x60;** | Last successful login date | | **&#x60;expireAt&#x60;** | Expiration date |
   * @param customerId Customer ID (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return UserList
   * @throws ApiException if fails to make API call
   */
  public UserList getCustomerUsers(Long customerId, String xSdsServiceToken, Integer offset, Integer limit, String filter, String sort, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling getCustomerUsers");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling getCustomerUsers");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/users"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
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
   * Get customers
   * ### Functional Description:   Receive a list of customers.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: None.  ### &amp;#9432; Further Information: This list returns a maximum of **1000** entries.    ### Filtering ### &amp;#9888; All filter fields are connected via logical conjunction (**AND**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;trialDaysLeft:le:10|userMax:le:100&#x60;   Get all customers with &#x60;10&#x60; trial days left **AND** user maximum **&lt;&#x3D;** &#x60;100&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;id&#x60;** | Customer ID filter | &#x60;eq&#x60; | Customer ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;companyName&#x60;** | Company name filter | &#x60;cn&#x60; | Company name contains value. | &#x60;search String&#x60; | | **&#x60;customerContractType&#x60;** | Customer contract type filter | &#x60;eq&#x60; | Customer contract type equals value. | &lt;ul&gt;&lt;li&gt;&#x60;demo&#x60;&lt;/li&gt;&lt;li&gt;&#x60;free&#x60;&lt;/li&gt;&lt;li&gt;&#x60;pay&#x60;&lt;/li&gt;&lt;/ul&gt; | | **&#x60;activationCode&#x60;** | (**&#x60;DEPRECATED&#x60;**) Activation code filter | &#x60;cn, eq&#x60; | Activation code contains / equals value. | &#x60;search String&#x60; | | **&#x60;trialDaysLeft&#x60;** | Left trial days filter | &#x60;ge, le&#x60; | Left trial days are greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;trialDaysLeft:ge:5&#x60;&amp;#124;&#x60;trialDaysLeft:le:10&#x60; | | **&#x60;providerCustomerId&#x60;** | (**&#x60;DEPRECATED&#x60;**) Provider Customer ID filter | &#x60;cn, eq&#x60; | Provider Customer ID contains / equals value. | &#x60;search String&#x60; | | **&#x60;quotaMax&#x60;** | Maximum quota filter | &#x60;ge, le&#x60; | Maximum quota is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;quotaMax:ge:1024&#x60;&amp;#124;&#x60;quotaMax:le:1073741824&#x60; | &#x60;positive Integer&#x60; | | **&#x60;quotaUsed&#x60;** | Used quota filter | &#x60;ge, le&#x60; | Used quota is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;quotaUsed:ge:1024&#x60;&amp;#124;&#x60;quotaUsed:le:1073741824&#x60; | &#x60;positive Integer&#x60; | | **&#x60;userMax&#x60;** | User maximum filter | &#x60;ge, le&#x60; | User maxiumum is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;userMax:ge:10&#x60;&amp;#124;&#x60;userMax:le:100&#x60; | &#x60;positive Integer&#x60; | | **&#x60;userUsed&#x60;** | Number of registered users filter | &#x60;ge, le&#x60; | Number of registered users is is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;userUsed:ge:10&#x60;&amp;#124;&#x60;userUsed:le:100&#x60; | &#x60;positive Integer&#x60; | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) Lock status filter | &#x60;eq&#x60; |  | &lt;ul&gt;&lt;li&gt;&#x60;0&#x60; - unlocked&lt;/li&gt;&lt;li&gt;&#x60;1&#x60; - locked&lt;/li&gt;&lt;/ul&gt; | | **&#x60;isLocked&#x60;** | Lock status filter | &#x60;eq&#x60; |  | &#x60;true or false&#x60; | | **&#x60;createdAt&#x60;** | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;updatedAt&#x60;** | Last modification date filter | &#x60;ge, le&#x60; | Last modification date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;updatedAt:ge:2016-12-31&#x60;&amp;#124;&#x60;updatedAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;lastLoginAt&#x60;** | Last login date filter | &#x60;ge, le&#x60; | Last login date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;lastLoginAt:ge:2016-12-31&#x60;&amp;#124;&#x60;lastLoginAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | **&#x60;userLogin&#x60;** | User login filter | &#x60;eq&#x60; | User login name equals value.&lt;br&gt;Search user all logins e.g. &#x60;sql&#x60;, &#x60;active_directory&#x60;, &#x60;radius&#x60;. | &#x60;search String&#x60; | | **&#x60;attributeKey&#x60;** | Customer attribute key filter | &#x60;eq&#x60; | Customer attribute key equals value. | &#x60;search String&#x60; | | **&#x60;attributeValue&#x60;** | Customer attribute value filter | &#x60;eq&#x60; | Customer attribute value equals value. | &#x60;search String&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are **NOT** supported.   Example: &gt; &#x60;companyName:desc&#x60;   Sort by &#x60;companyName&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;companyName&#x60;** | Company name | | **&#x60;customerContractType&#x60;** | Customer contract type | | **&#x60;trialDaysLeft&#x60;** | Number of remaining trial days (demo customers) | | **&#x60;providerCustomerId&#x60;** | (**&#x60;DEPRECATED&#x60;**) Provider Customer ID | | **&#x60;quotaMax&#x60;** | Maximum quota | | **&#x60;quotaUsed&#x60;** | Currently used quota | | **&#x60;userMax&#x60;** | Maximum user number | | **&#x60;userUsed&#x60;** | Number of registered users | | **&#x60;lockStatus&#x60;** | (**&#x60;DEPRECATED&#x60;**) Lock status of customer | | **&#x60;isLocked&#x60;** | Lock status of customer | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;updatedAt&#x60;** | Last modification date | | **&#x60;lastLoginAt&#x60;** | Last login date of any user of this customer |
   * @param xSdsServiceToken Service Authentication token (required)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param includeAttributes Include custom customer attributes. (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return CustomerList
   * @throws ApiException if fails to make API call
   */
  public CustomerList getCustomers(String xSdsServiceToken, Integer offset, Integer limit, String filter, String sort, Boolean includeAttributes, String xSdsDateFormat) throws ApiException {
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

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "include_attributes", includeAttributes));

    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
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
   * Create customer
   * ### Functional Description: Create a new customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.    ### Effects: A new customer is created.  ### &amp;#9432; Further Information: If no company name is set, first name of the first administrator is used.   Max quota has to be at least 1 GB (&#x3D; 1 073 741 824 B).  ### Authentication Method Options  | Authentication Method | Option Key | Option Value | | :--- | :--- | :--- | | **&#x60;sql&#x60;** | none | none | | **&#x60;active_directory&#x60;** | &#x60;ad_config_id&#x60; (optional) | Active Directory configuration ID | |  | &#x60;username&#x60; | Active Directory username according to authentication setting &#x60;userFilter&#x60; | | **&#x60;radius&#x60;** | &#x60;username&#x60; | RADIUS username | | **&#x60;openid&#x60;** | &#x60;openid_config_id&#x60; | OpenID Connect configuration ID | |  | &#x60;username&#x60; | OpenID Connect username according to authentication setting &#x60;mappingClaim&#x60; |
   * @param body body (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return NewCustomerResponse
   * @throws ApiException if fails to make API call
   */
  public NewCustomerResponse newCustomerRequest(NewCustomerRequest body, String xSdsServiceToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling newCustomerRequest");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling newCustomerRequest");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<NewCustomerResponse> localVarReturnType = new GenericType<NewCustomerResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Set customer attributes
   * ### Functional Description:   Set custom customer attributes.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attributes gets set.  ### &amp;#9432; Further Information: Batch function.   All existing customer attributes will be deleted.   Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   Characters are case-insensitive. 
   * @param customerId Customer ID (required)
   * @param body body (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Customer
   * @throws ApiException if fails to make API call
   */
  public Customer setAllCustomerAttributes(Long customerId, CustomerAttributes body, String xSdsServiceToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling setAllCustomerAttributes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setAllCustomerAttributes");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling setAllCustomerAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/customerAttributes"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Customer> localVarReturnType = new GenericType<Customer>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Add or edit customer attributes
   * ### Functional Description:   Add or edit custom customer attributes.  ### Precondition: Right _\&quot;change global config\&quot;_ required.  ### Effects: Custom customer attributes get added or edited.  ### &amp;#9432; Further Information: Batch function.   If an entry exists before, it will be overwritten.   Allowed characters for keys are: &#x60;[a-zA-Z0-9_-]&#x60;   Characters are case-insensitive.
   * @param customerId Customer ID (required)
   * @param body body (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return Customer
   * @throws ApiException if fails to make API call
   */
  public Customer setCustomerAttributes(Long customerId, CustomerAttributes body, String xSdsServiceToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling setCustomerAttributes");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling setCustomerAttributes");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling setCustomerAttributes");
    }
    
    // create path and map variables
    String localVarPath = "/v4/provisioning/customers/{customer_id}/customerAttributes"
      .replaceAll("\\{" + "customer_id" + "\\}", apiClient.escapeString(customerId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Customer> localVarReturnType = new GenericType<Customer>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update customer
   * ### Functional Description:   Change selected attributes of a customer.  ### Precondition: Authentication with &#x60;X-Sds-Service-Token&#x60; required.  ### Effects: Update of attributes.  ### &amp;#9432; Further Information: None.
   * @param customerId Customer ID (required)
   * @param body body (required)
   * @param xSdsServiceToken Service Authentication token (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return UpdateCustomerResponse
   * @throws ApiException if fails to make API call
   */
  public UpdateCustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest body, String xSdsServiceToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling updateCustomer");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateCustomer");
    }
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling updateCustomer");
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
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UpdateCustomerResponse> localVarReturnType = new GenericType<UpdateCustomerResponse>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
