package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.Customer;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerList;
import ch.cyberduck.core.sds.io.swagger.client.model.NewCustomerRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.NewCustomerResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateCustomerRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateCustomerResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.UserList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-03-21T19:15:03.115+01:00")
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
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt;Delete a customer.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Customer is deleted.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Authentication with &lt;b&gt;X-Sds-Service-Token&lt;/b&gt; required.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteCustomer(String xSdsServiceToken, Long customerId) throws ApiException {
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
    String localVarPath = "/provisioning/customers/{customer_id}".replaceAll("\\{format\\}","json")
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


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get customer
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt;Receive details of a selected customer.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Existing customer.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; none.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Authentication with &lt;b&gt;X-Sds-Service-Token&lt;/b&gt; required.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return Customer
   * @throws ApiException if fails to make API call
   */
  public Customer getCustomer(String xSdsServiceToken, Long customerId, String xSdsDateFormat) throws ApiException {
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
    String localVarPath = "/provisioning/customers/{customer_id}".replaceAll("\\{format\\}","json")
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
   * Get customer users
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt; Receive a list of users associated with a certain customer.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Authentication with &lt;b&gt;X-Sds-Service-Token&lt;/b&gt; required.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Multiple fields are supported.&lt;/p&gt;&lt;h5&gt;Filter fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;login&lt;/dt&gt;&lt;dd&gt;Login name&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (User login name contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;firstName&lt;/dt&gt;&lt;dd&gt;First name&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (User first name contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;lastName&lt;/dt&gt;&lt;dd&gt;Last name&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (User last name contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;lockStatus&lt;/dt&gt;&lt;dd&gt;Lock status: 0 - Locked, 1 - Web access allowed, 2 - Web and mobile access allowed,&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (User lock status)&lt;br/&gt;VALUE: [0|1|2].&lt;/dd&gt;&lt;dt&gt;effectiveRoles&lt;/dt&gt;&lt;dd&gt;Filter users with roles, effective roles or direct roles (NO GROUPS)&lt;br/&gt;FALSE: Direct roles TRUE: effective roles&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false]. Default value is &lt;code&gt;false&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;&lt;b&gt;Logical grouping:&lt;/b&gt; filtering according first three fields (login, lastName, firstName)&lt;br&gt;is intrinsically processed by the API as logical &lt;i&gt;OR&lt;/i&gt;.  In opposite, filtering according to&lt;br/&gt;last three field (lockStatus)&lt;br/&gt;is processed intrinsically as logical &lt;i&gt;AND&lt;/i&gt;.&lt;/p&gt;&lt;p&gt;Example: &lt;samp&gt;login:cn:searchString_1|firstName:cn:searchString_2|lockStatus:eq:2 &lt;/samp&gt;&lt;br/&gt;- filter by login contains searchString_1 or firstName contains searchString_2 and user are not locked&lt;/p&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;&lt;br/&gt;Multiple fields are supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;expireAt&lt;/dt&gt;&lt;dd&gt;Expiration date&lt;/dd&gt;&lt;dt&gt;lastLoginSuccessAt&lt;/dt&gt;&lt;dd&gt;Last successful logon date&lt;/dd&gt;&lt;dt&gt;login&lt;/dt&gt;&lt;dd&gt;Login name&lt;/dd&gt;&lt;dt&gt;firstName&lt;/dt&gt;&lt;dd&gt;First name&lt;/dd&gt;&lt;dt&gt;gender&lt;/dt&gt;&lt;dd&gt;Gender&lt;/dd&gt;&lt;dt&gt;lastName&lt;/dt&gt;&lt;dd&gt;Last name&lt;/dd&gt;&lt;dt&gt;lockStatus&lt;/dt&gt;&lt;dd&gt;User lock status&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;lastLoginSuccessAt:asc|firstName:desc&lt;/samp&gt;&lt;br/&gt;- sort by lastLoginSuccessAt ascending and by firstName descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return UserList
   * @throws ApiException if fails to make API call
   */
  public UserList getCustomerUsers(String xSdsServiceToken, Long customerId, String xSdsDateFormat, Integer offset, Integer limit, String filter, String sort) throws ApiException {
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
    String localVarPath = "/provisioning/customers/{customer_id}/users".replaceAll("\\{format\\}","json")
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
   * Get customers
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Receive a list of customers.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt;&amp;nbsp;This list returns a maximum of 1000 entries. Please use filters or searches to specify what you are looking for.&lt;br/&gt;Authentication with &lt;b&gt;X-Sds-Service-Token&lt;/b&gt; required.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;[:&amp;lt;VALUE&amp;gt;...]&lt;/dfn&gt;&lt;/p&gt;&lt;h5&gt;Fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;id&lt;/dt&gt;&lt;dd&gt;Customer ID filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Positive Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;customerContractType&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;Customer contract type filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;demo|free|pay&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;providerCustomerId&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;Provider customer ID filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;companyName&lt;/dt&gt;&lt;dd&gt;Company name filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Company name contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;quotaMax&lt;/dt&gt;&lt;dd&gt;Maximum quota filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (Quota is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Positive Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;quotaUsed&lt;/dt&gt;&lt;dd&gt;Used quota filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (Quota is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Positive Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userMax&lt;/dt&gt;&lt;dd&gt;Maximum user filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (User maximum is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Positive Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userUsed&lt;/dt&gt;&lt;dd&gt;Used users filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (Number of registered users is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Positiv Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation date filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (Date is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Date&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;updatedAt&lt;/dt&gt;&lt;dd&gt;Update date filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (Date is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Date&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;lastLoginAt&lt;/dt&gt;&lt;dd&gt;Last login filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (Date is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Date&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;activationCode&lt;/dt&gt;&lt;dd&gt;Activation Code filter&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Activation code contains value, multiple values not allowed)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;trialDaysLeft&lt;/dt&gt;&lt;dd&gt;Left trial days filter&lt;br/&gt;OPERATOR: &lt;code&gt;ge|le&lt;/code&gt; (Number of trial days is greater or equal | less or equal)&lt;br/&gt;VALUE: &lt;code&gt;Positiv Integer&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;lockStatus&lt;/dt&gt;&lt;dd&gt;Lock status filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Integer (0 or 1)&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userLogin&lt;/dt&gt;&lt;dd&gt;User login&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (Customer user login name equal value, multiple values not allowed)&lt;br/&gt; Search user all logins.E.g. sql, active_directory, radius.&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;.&lt;br/&gt;Multiple fields not supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;customerContractType&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;Customer contract type&lt;/dd&gt;&lt;dt&gt;providerCustomerId&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;Provider customer ID (pay customers)&lt;/dd&gt;&lt;dt&gt;companyName&lt;/dt&gt;&lt;dd&gt;Company name&lt;/dd&gt;&lt;dt&gt;quotaMax&lt;/dt&gt;&lt;dd&gt;Maximum quota&lt;/dd&gt;&lt;dt&gt;quotaUsed&lt;/dt&gt;&lt;dd&gt;Currently used quota&lt;/dd&gt;&lt;dt&gt;userMax&lt;/dt&gt;&lt;dd&gt;Maximum user number&lt;/dd&gt;&lt;dt&gt;userUsed&lt;/dt&gt;&lt;dd&gt;Number of currently active users&lt;/dd&gt;&lt;dt&gt;lockStatus&lt;/dt&gt;&lt;dd&gt;Lock status of customer&lt;/dd&gt;&lt;dt&gt;trialDaysLeft&lt;/dt&gt;&lt;dd&gt;Number of remaining trial days (demo customers)&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation date&lt;/dd&gt;&lt;dt&gt;updatedAt&lt;/dt&gt;&lt;dd&gt;Date of last update&lt;/dd&gt;&lt;dt&gt;lastLoginAt&lt;/dt&gt;&lt;dd&gt;Date of last login of any user of this customer&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;companyName:desc&lt;/samp&gt;&lt;br/&gt;&amp;rarr; sort by company name descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsServiceToken Service Authentication token (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return CustomerList
   * @throws ApiException if fails to make API call
   */
  public CustomerList getCustomers(String xSdsServiceToken, String xSdsDateFormat, Integer offset, Integer limit, String filter, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling getCustomers");
    }
    
    // create path and map variables
    String localVarPath = "/provisioning/customers".replaceAll("\\{format\\}","json");

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
   * Create customer
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt;Create a new customer.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; A new customer is created.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Authentication with &lt;b&gt;X-Sds-Auth-Token&lt;/b&gt; required. If no company name is set, first name of the first DSA is used. Max quota has to be at least 1 GB (&#x3D; 1073741824).&lt;/p&gt;&lt;/div&gt;
   * @param xSdsServiceToken Service Authentication token (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return NewCustomerResponse
   * @throws ApiException if fails to make API call
   */
  public NewCustomerResponse newCustomerRequest(String xSdsServiceToken, NewCustomerRequest body, String xSdsDateFormat) throws ApiException {
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
    String localVarPath = "/provisioning/customers".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsServiceToken != null)
      localVarHeaderParams.put("X-Sds-Service-Token", apiClient.parameterToString(xSdsServiceToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<NewCustomerResponse> localVarReturnType = new GenericType<NewCustomerResponse>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update customer &lt;/dt&gt;
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt;Change selected attributes of a customer.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Existing customer.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Update of attributes.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Authentication with &lt;b&gt;X-Sds-Service-Token&lt;/b&gt; required.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsServiceToken Service Authentication token (required)
   * @param customerId Customer ID (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return UpdateCustomerResponse
   * @throws ApiException if fails to make API call
   */
  public UpdateCustomerResponse updateCustomer(String xSdsServiceToken, Long customerId, UpdateCustomerRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsServiceToken' is set
    if (xSdsServiceToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsServiceToken' when calling updateCustomer");
    }
    
    // verify the required parameter 'customerId' is set
    if (customerId == null) {
      throw new ApiException(400, "Missing the required parameter 'customerId' when calling updateCustomer");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateCustomer");
    }
    
    // create path and map variables
    String localVarPath = "/provisioning/customers/{customer_id}".replaceAll("\\{format\\}","json")
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
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UpdateCustomerResponse> localVarReturnType = new GenericType<UpdateCustomerResponse>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
