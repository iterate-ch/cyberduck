package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import org.joda.time.DateTime;
import ch.cyberduck.core.storegate.io.swagger.client.model.EventContents;
import ch.cyberduck.core.storegate.io.swagger.client.model.EventExportRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class EventsApi {
  private ApiClient apiClient;

  public EventsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public EventsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Download events via token.
   * 
   * @param downloadid  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String eventsGet(String downloadid) throws ApiException {
    return eventsGetWithHttpInfo(downloadid).getData();
      }

  /**
   * Download events via token.
   * 
   * @param downloadid  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> eventsGetWithHttpInfo(String downloadid) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'downloadid' is set
    if (downloadid == null) {
      throw new ApiException(400, "Missing the required parameter 'downloadid' when calling eventsGet");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/events/export/{downloadid}"
      .replaceAll("\\{" + "downloadid" + "\\}", apiClient.escapeString(downloadid.toString()));

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Download events.
   * 
   * @param fromDate  (required)
   * @param toDate  (required)
   * @param user  (required)
   * @param fileId  (required)
   * @param filename  (required)
   * @param timeZone  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String eventsGet_0(DateTime fromDate, DateTime toDate, String user, String fileId, String filename, String timeZone) throws ApiException {
    return eventsGet_0WithHttpInfo(fromDate, toDate, user, fileId, filename, timeZone).getData();
      }

  /**
   * Download events.
   * 
   * @param fromDate  (required)
   * @param toDate  (required)
   * @param user  (required)
   * @param fileId  (required)
   * @param filename  (required)
   * @param timeZone  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> eventsGet_0WithHttpInfo(DateTime fromDate, DateTime toDate, String user, String fileId, String filename, String timeZone) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'fromDate' is set
    if (fromDate == null) {
      throw new ApiException(400, "Missing the required parameter 'fromDate' when calling eventsGet_0");
    }
    
    // verify the required parameter 'toDate' is set
    if (toDate == null) {
      throw new ApiException(400, "Missing the required parameter 'toDate' when calling eventsGet_0");
    }
    
    // verify the required parameter 'user' is set
    if (user == null) {
      throw new ApiException(400, "Missing the required parameter 'user' when calling eventsGet_0");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling eventsGet_0");
    }
    
    // verify the required parameter 'filename' is set
    if (filename == null) {
      throw new ApiException(400, "Missing the required parameter 'filename' when calling eventsGet_0");
    }
    
    // verify the required parameter 'timeZone' is set
    if (timeZone == null) {
      throw new ApiException(400, "Missing the required parameter 'timeZone' when calling eventsGet_0");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/events/export";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fromDate", fromDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toDate", toDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user", user));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fileId", fileId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filename", filename));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "timeZone", timeZone));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get events.
   * 
   * @param pageIndex  (required)
   * @param pageSize  (required)
   * @param fromDate  (required)
   * @param toDate  (required)
   * @param user  (required)
   * @param fileId  (required)
   * @param filename  (required)
   * @return EventContents
   * @throws ApiException if fails to make API call
   */
  public EventContents eventsGet_1(Integer pageIndex, Integer pageSize, DateTime fromDate, DateTime toDate, String user, String fileId, String filename) throws ApiException {
    return eventsGet_1WithHttpInfo(pageIndex, pageSize, fromDate, toDate, user, fileId, filename).getData();
      }

  /**
   * Get events.
   * 
   * @param pageIndex  (required)
   * @param pageSize  (required)
   * @param fromDate  (required)
   * @param toDate  (required)
   * @param user  (required)
   * @param fileId  (required)
   * @param filename  (required)
   * @return ApiResponse&lt;EventContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<EventContents> eventsGet_1WithHttpInfo(Integer pageIndex, Integer pageSize, DateTime fromDate, DateTime toDate, String user, String fileId, String filename) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling eventsGet_1");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling eventsGet_1");
    }
    
    // verify the required parameter 'fromDate' is set
    if (fromDate == null) {
      throw new ApiException(400, "Missing the required parameter 'fromDate' when calling eventsGet_1");
    }
    
    // verify the required parameter 'toDate' is set
    if (toDate == null) {
      throw new ApiException(400, "Missing the required parameter 'toDate' when calling eventsGet_1");
    }
    
    // verify the required parameter 'user' is set
    if (user == null) {
      throw new ApiException(400, "Missing the required parameter 'user' when calling eventsGet_1");
    }
    
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling eventsGet_1");
    }
    
    // verify the required parameter 'filename' is set
    if (filename == null) {
      throw new ApiException(400, "Missing the required parameter 'filename' when calling eventsGet_1");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/events";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fromDate", fromDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "toDate", toDate));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user", user));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "fileId", fileId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filename", filename));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<EventContents> localVarReturnType = new GenericType<EventContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get download events token.
   * 
   * @param request  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String eventsPost(EventExportRequest request) throws ApiException {
    return eventsPostWithHttpInfo(request).getData();
      }

  /**
   * Get download events token.
   * 
   * @param request  (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> eventsPostWithHttpInfo(EventExportRequest request) throws ApiException {
    Object localVarPostBody = request;
    
    // verify the required parameter 'request' is set
    if (request == null) {
      throw new ApiException(400, "Missing the required parameter 'request' when calling eventsPost");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/events/export";

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

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
