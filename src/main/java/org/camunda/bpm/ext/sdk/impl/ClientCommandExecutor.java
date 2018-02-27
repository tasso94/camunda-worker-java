/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.ext.sdk.impl;

import java.io.IOException;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.camunda.bpm.ext.sdk.impl.variables.ValueSerializers;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class ClientCommandExecutor {

  protected String endpointUrl;
  protected String clientId;
  protected long asyncResponseTimeout;
  protected int maxTasks;

  protected CloseableHttpClient httpClient;
  protected ObjectMapper objectMapper;
  protected ValueSerializers valueSerializers;

  public ClientCommandExecutor(String endpointUrl, String clientId, long asyncResponseTimeout, int maxTasks, CloseableHttpClient client, ObjectMapper objectMapper, ValueSerializers valueSerializers) {
    this.endpointUrl = endpointUrl;
    this.clientId = clientId;
    this.asyncResponseTimeout = asyncResponseTimeout;
    this.maxTasks = maxTasks;

    this.httpClient = client;
    this.objectMapper = objectMapper;
    this.valueSerializers = valueSerializers;
  }

  public <T> T executePost(String url, ClientPostComand<T> cmd) {
    HttpPost httpPost = new HttpPost(sanitizeUrl(endpointUrl, url));
    httpPost.addHeader("Content-Type", "application/json");
    ClientCommandContext clientCommandContext = new ClientCommandContext(clientId, asyncResponseTimeout, maxTasks, httpClient, objectMapper, valueSerializers);
    return cmd.execute(clientCommandContext, httpPost);
  }

  public <T> T executePostMultipart(String url, ClientPostMultipartComand<T> cmd) {
    HttpPost httpPost = new HttpPost(sanitizeUrl(endpointUrl, url));
    ClientCommandContext clientCommandContext = new ClientCommandContext(clientId, asyncResponseTimeout, maxTasks, httpClient, objectMapper, valueSerializers);
    return cmd.execute(clientCommandContext, httpPost);
  }

  public void executeDelete(String url) {
    HttpDelete delete = new HttpDelete(sanitizeUrl(endpointUrl, url));
    new ClientCommandContext(clientId, asyncResponseTimeout, 10, httpClient, objectMapper, valueSerializers)
      .execute(delete);
  }

  public <T> T executeGet(String url, ClientGetComand<T> cmd) {
    HttpGet httpGet = new HttpGet(sanitizeUrl(endpointUrl, url));
    ClientCommandContext clientCommandContext = new ClientCommandContext(clientId, asyncResponseTimeout, maxTasks, httpClient, objectMapper, valueSerializers);
    return cmd.execute(clientCommandContext, httpGet);
  }

  private String sanitizeUrl(String baseUrl, String relativeUrl) {
    if(baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() -1);
    }
    if(relativeUrl.startsWith("/")) {
      relativeUrl = relativeUrl.substring(1);
    }

    return baseUrl + "/" + relativeUrl;
  }

  public ValueSerializers getValueSerializers() {
    return valueSerializers;
  }

  /**
   * @return the objectMapper
   */
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void close() {
    try {
      httpClient.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
