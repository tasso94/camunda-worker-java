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
package org.camunda.bpm.ext.sdk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.ext.sdk.impl.ClientCommandExecutor;
import org.camunda.bpm.ext.sdk.impl.variables.ValueSerializers;
import org.camunda.bpm.ext.sdk.impl.workers.WorkerManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * @author Daniel Meyer
 *
 */
public class CamundaClientBuilder {

  private final static ClientLogger LOG = ClientLogger.LOGGER;

  protected String endpointUrl;
  protected String clientId;

  protected long asyncResponseTimeout;
  protected int maxTasks;

  protected int numOfWorkerThreads = 4;
  protected int queueSize = 25;

  protected CloseableHttpClient httpClient;
  protected ClientCommandExecutor clientCommandExecutor;
  protected WorkerManager workerManager;
  protected ObjectMapper objectMapper;
  protected ValueSerializers valueSerializers;

  public CamundaClientBuilder() {

  }

  public CamundaClientBuilder endpointUrl(String endpointUrl) {
    this.endpointUrl = endpointUrl;
    return this;
  }

  public CamundaClientBuilder clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public CamundaClientBuilder asyncResponseTimeout(long asyncResponseTimeout) {
    this.asyncResponseTimeout = asyncResponseTimeout;
    return this;
  }

  public CamundaClientBuilder maxTasks(int maxTasks) {
    this.maxTasks = maxTasks;
    return this;
  }

  public CamundaClientBuilder workerThreads(int numOfWorkerThreads) {
    this.numOfWorkerThreads = numOfWorkerThreads;
    return this;
  }

  public CamundaClientBuilder queueSize(int queueSize) {
    this.queueSize = queueSize;
    return this;
  }


   // building ///////////////////////////////

  public CamundaClient build() {
    LOG.initializingCamundaClient(endpointUrl);
    init();
    return new CamundaClient(this);
  }

  protected void init() {
    initClientId();
    initObjectMapper();
    initValueSerializers();
    initHttpClient();
    initClientCommandExecutor();
    initWorkerManager();
  }

  protected void initValueSerializers() {
    if(valueSerializers == null) {
      valueSerializers = new ValueSerializers();
    }
  }

  protected void initClientId() {
    if(clientId == null) {
      String hostName;
      try {
        hostName = InetAddress.getLocalHost().getHostName() + " - " + UUID.randomUUID().toString();
        clientId = hostName;
      } catch (UnknownHostException e) {
        throw new CamundaClientException("Cannot get hostname", e);
      }
    }
  }

  protected void initObjectMapper() {
    if(objectMapper == null) {
      objectMapper = new ObjectMapper();
      objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
  }

  protected void initWorkerManager() {
    if(this.workerManager == null) {
      this.workerManager = new WorkerManager(clientCommandExecutor, numOfWorkerThreads, queueSize);
    }
  }

  protected void initClientCommandExecutor() {
    if(clientCommandExecutor == null) {
      clientCommandExecutor = new ClientCommandExecutor(endpointUrl, clientId, 0, 10, httpClient, objectMapper, valueSerializers);
    }
  }

  protected void initHttpClient() {
    if(httpClient == null) {
      httpClient = HttpClients.createDefault();
    }
  }
}
