package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import org.joda.time.DateTime;
import ch.cyberduck.core.box.io.swagger.client.model.Events;
import ch.cyberduck.core.box.io.swagger.client.model.RealtimeServers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class EventsApi {
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
   * List user and enterprise events
   * Returns up to a year of past events for a given user or for the entire enterprise.  By default this returns events for the authenticated user. To retrieve events for the entire enterprise, set the &#x60;stream_type&#x60; to &#x60;admin_logs&#x60;. The user making the API call will need to have admin privileges, and the application will need to have the permission to access the event feed to get the enterprise event feed.
   * @param streamType Defines the type of events that are returned  * &#x60;all&#x60; returns everything for a user and is the default * &#x60;changes&#x60; returns events that may cause file tree changes   such as file updates or collaborations. * &#x60;sync&#x60; is similar to &#x60;changes&#x60; but only applies to synced folders * &#x60;admin_logs&#x60; returns all events for an entire enterprise and   requires the user making the API call to have admin permissions. (optional, default to all)
   * @param streamPosition The location in the event stream to start receiving events from.  * &#x60;now&#x60; will return an empty list events and the latest stream position for initialization. * &#x60;0&#x60; or &#x60;null&#x60; will return all events. (optional)
   * @param limit Limits the number of events returned (optional, default to 100)
   * @param eventType A comma-separated list of events to filter by. This can only be used when requesting the events with a &#x60;stream_type&#x60; of &#x60;admin_logs&#x60;. For any other &#x60;stream_type&#x60; this value will be ignored. (optional)
   * @param createdAfter The lower bound date and time to return events for. This can only be used when requesting the events with a &#x60;stream_type&#x60; of &#x60;admin_logs&#x60;. For any other &#x60;stream_type&#x60; this value will be ignored. (optional)
   * @param createdBefore The upper bound date and time to return events for. This can only be used when requesting the events with a &#x60;stream_type&#x60; of &#x60;admin_logs&#x60;. For any other &#x60;stream_type&#x60; this value will be ignored. (optional)
   * @return Events
   * @throws ApiException if fails to make API call
   */
  public Events getEvents(String streamType, String streamPosition, Long limit, List<String> eventType, DateTime createdAfter, DateTime createdBefore) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/events";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream_type", streamType));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stream_position", streamPosition));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "event_type", eventType));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "created_after", createdAfter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "created_before", createdBefore));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Events> localVarReturnType = new GenericType<Events>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get events long poll endpoint
   * Returns a list of real-time servers that can be used for long-polling updates to the [event stream](#get-events).  Long polling is the concept where a HTTP request is kept open until the server sends a response, then repeating the process over and over to receive updated responses.  Long polling the event stream can only be used for user events, not for enterprise events.  To use long polling, first use this endpoint to retrieve a list of long poll URLs. Next, make a long poll request to any of the provided URLs.  When an event occurs in monitored account a response with the value &#x60;new_change&#x60; will be sent. The response contains no other details as it only serves as a prompt to take further action such as sending a request to the [events endpoint](#get-events) with the last known &#x60;stream_position&#x60;.  After the server sends this response it closes the connection. You must now repeat the long poll process to begin listening for events again.  If no events occur for a while and the connection times out you will receive a response with the value &#x60;reconnect&#x60;. When you receive this response you’ll make another call to this endpoint to restart the process.  If you receive no events in &#x60;retry_timeout&#x60; seconds then you will need to make another request to the real-time server (one of the URLs in the response for this endpoint). This might be necessary due to network errors.  Finally, if you receive a &#x60;max_retries&#x60; error when making a request to the real-time server, you should start over by making a call to this endpoint first.
   * @return RealtimeServers
   * @throws ApiException if fails to make API call
   */
  public RealtimeServers optionsEvents() throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/events";

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

    GenericType<RealtimeServers> localVarReturnType = new GenericType<RealtimeServers>() {};
    return apiClient.invokeAPI(localVarPath, "OPTIONS", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
