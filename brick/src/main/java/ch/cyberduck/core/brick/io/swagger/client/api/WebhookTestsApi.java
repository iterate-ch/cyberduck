package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.WebhookTestEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class WebhookTestsApi {
  private ApiClient apiClient;

  public WebhookTestsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public WebhookTestsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create Webhook Test
   * Create Webhook Test
   * @param url URL for testing the webhook. (required)
   * @param method HTTP method(GET or POST). (optional)
   * @param encoding HTTP encoding method.  Can be JSON, XML, or RAW (form data). (optional)
   * @param headers Additional request headers. (optional)
   * @param body Additional body parameters. (optional)
   * @param action action for test body (optional)
   * @return WebhookTestEntity
   * @throws ApiException if fails to make API call
   */
  public WebhookTestEntity postWebhookTests(String url, String method, String encoding, Map<String, String> headers, Map<String, String> body, String action) throws ApiException {
    return postWebhookTestsWithHttpInfo(url, method, encoding, headers, body, action).getData();
      }

  /**
   * Create Webhook Test
   * Create Webhook Test
   * @param url URL for testing the webhook. (required)
   * @param method HTTP method(GET or POST). (optional)
   * @param encoding HTTP encoding method.  Can be JSON, XML, or RAW (form data). (optional)
   * @param headers Additional request headers. (optional)
   * @param body Additional body parameters. (optional)
   * @param action action for test body (optional)
   * @return ApiResponse&lt;WebhookTestEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<WebhookTestEntity> postWebhookTestsWithHttpInfo(String url, String method, String encoding, Map<String, String> headers, Map<String, String> body, String action) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'url' is set
    if (url == null) {
      throw new ApiException(400, "Missing the required parameter 'url' when calling postWebhookTests");
    }
    
    // create path and map variables
    String localVarPath = "/webhook_tests";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (url != null)
      localVarFormParams.put("url", url);
if (method != null)
      localVarFormParams.put("method", method);
if (encoding != null)
      localVarFormParams.put("encoding", encoding);
if (headers != null)
      localVarFormParams.put("headers", headers);
if (body != null)
      localVarFormParams.put("body", body);
if (action != null)
      localVarFormParams.put("action", action);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<WebhookTestEntity> localVarReturnType = new GenericType<WebhookTestEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
