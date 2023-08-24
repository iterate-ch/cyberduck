package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.Invoice;
import ch.cyberduck.core.storegate.io.swagger.client.model.InvoiceInfo;
import ch.cyberduck.core.storegate.io.swagger.client.model.PaymentInfo;
import ch.cyberduck.core.storegate.io.swagger.client.model.SetPaymentStatusRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.UpdatePaymentRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class BillingApi {
  private ApiClient apiClient;

  public BillingApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BillingApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get invoices
   * 
   * @return List&lt;InvoiceInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public List<InvoiceInfo> billingGetInvoiceHeaders() throws ApiException {
    return billingGetInvoiceHeadersWithHttpInfo().getData();
      }

  /**
   * Get invoices
   * 
   * @return ApiResponse&lt;List&lt;InvoiceInfo&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<InvoiceInfo>> billingGetInvoiceHeadersWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/billing/invoices";

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

    GenericType<List<InvoiceInfo>> localVarReturnType = new GenericType<List<InvoiceInfo>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get invoice
   * 
   * @param id  (required)
   * @return Invoice
   * @throws ApiException if fails to make API call
   */
  public Invoice billingGetInvoiceHeaders_0(String id) throws ApiException {
    return billingGetInvoiceHeaders_0WithHttpInfo(id).getData();
      }

  /**
   * Get invoice
   * 
   * @param id  (required)
   * @return ApiResponse&lt;Invoice&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Invoice> billingGetInvoiceHeaders_0WithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling billingGetInvoiceHeaders_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/billing/invoices/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<Invoice> localVarReturnType = new GenericType<Invoice>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get payment information
   * 
   * @return PaymentInfo
   * @throws ApiException if fails to make API call
   */
  public PaymentInfo billingGetPayment() throws ApiException {
    return billingGetPaymentWithHttpInfo().getData();
      }

  /**
   * Get payment information
   * 
   * @return ApiResponse&lt;PaymentInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PaymentInfo> billingGetPaymentWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/billing/info";

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

    GenericType<PaymentInfo> localVarReturnType = new GenericType<PaymentInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update payment status
   * 
   * @param updatePayment  (required)
   * @throws ApiException if fails to make API call
   */
  public void billingSetPaymentStatus(SetPaymentStatusRequest updatePayment) throws ApiException {

    billingSetPaymentStatusWithHttpInfo(updatePayment);
  }

  /**
   * Update payment status
   * 
   * @param updatePayment  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> billingSetPaymentStatusWithHttpInfo(SetPaymentStatusRequest updatePayment) throws ApiException {
    Object localVarPostBody = updatePayment;
    
    // verify the required parameter 'updatePayment' is set
    if (updatePayment == null) {
      throw new ApiException(400, "Missing the required parameter 'updatePayment' when calling billingSetPaymentStatus");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/billing/status";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update payment information
   * 
   * @param updatePayment  (required)
   * @return PaymentInfo
   * @throws ApiException if fails to make API call
   */
  public PaymentInfo billingUpdatePayment(UpdatePaymentRequest updatePayment) throws ApiException {
    return billingUpdatePaymentWithHttpInfo(updatePayment).getData();
      }

  /**
   * Update payment information
   * 
   * @param updatePayment  (required)
   * @return ApiResponse&lt;PaymentInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PaymentInfo> billingUpdatePaymentWithHttpInfo(UpdatePaymentRequest updatePayment) throws ApiException {
    Object localVarPostBody = updatePayment;
    
    // verify the required parameter 'updatePayment' is set
    if (updatePayment == null) {
      throw new ApiException(400, "Missing the required parameter 'updatePayment' when calling billingUpdatePayment");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/billing/info";

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

    GenericType<PaymentInfo> localVarReturnType = new GenericType<PaymentInfo>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
