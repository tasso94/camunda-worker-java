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
package org.camunda.bpm.ext.worker.test;

import org.camunda.bpm.ext.sdk.CamundaClient;
import org.camunda.bpm.ext.sdk.CamundaClientException;
import org.camunda.bpm.ext.sdk.WorkerRegistration;
import org.camunda.bpm.ext.sdk.dto.DeploymentDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests assume that the server is running
 *
 * @author Daniel Meyer
 *
 */
public class WorkerIntegrationTests {

  protected CamundaClient client;

  @Before
  public void setup() {
    client = CamundaClient.create()
      .endpointUrl("http://localhost:8080/engine-rest")
      .build();
  }

  @After
  public void close() {
    client.close();
  }

  @Test
  public void testCreateDeployment() {

    DeploymentDto deployment = client.createDeployment()
      .name("testDeployment")
      .enableDuplicateFiltering()
      .classPathResource("org/camunda/bpm/ext/worker/test/oneTaskProcess.bpmn")
      .deploy();

    client.deleteDeployment(deployment.getId(), true);

  }

  @Test
  public void testFailingDeployment() {

    try {
      client.createDeployment()
        .name("testDeployment")
        .enableDuplicateFiltering()
        .stringResource("file.bpmn", "this is not xml")
        .deploy();
      Assert.fail("Exception expected");
    }
    catch(CamundaClientException e) {
      //expected
    }
  }

  @Test
  public void testStartProcessInstance() {

    DeploymentDto deployment = client.createDeployment()
      .name("testDeployment")
      .enableDuplicateFiltering()
      .classPathResource("org/camunda/bpm/ext/worker/test/oneTaskProcess.bpmn")
      .deploy();

    try {
      client.startProcessInstanceByKey("testProcess", null);
    }
    finally {
      client.deleteDeployment(deployment.getId(), true);
    }

  }

  @Test
  public void testWorkerRegisterUnregister() {

    DeploymentDto deployment = client.createDeployment()
      .name("testDeployment")
      .enableDuplicateFiltering()
      .classPathResource("org/camunda/bpm/ext/worker/test/oneTaskProcess.bpmn")
      .deploy();

    WorkerRegistration registration = client.registerWorker()
      .topicName("exampleTopicName")
      .lockDuration(5000)
      .worker(new CompletingWorker())
      .build();

    client.startProcessInstanceByKey("testProcess", null);

    registration.remove();

    client.deleteDeployment(deployment.getId(), true);

  }

  @Test
  public void testWorkerVariables() {

    DeploymentDto deployment = client.createDeployment()
      .name("testDeployment")
      .enableDuplicateFiltering()
      .classPathResource("org/camunda/bpm/ext/worker/test/oneTaskProcess.bpmn")
      .deploy();

    WorkerRegistration registration = client.registerWorker()
      .topicName("exampleTopicName")
      .lockDuration(5000)
      .worker(new CompletingWorker())
      .build();

    client.startProcessInstanceByKey("testProcess", null);

    registration.remove();

    client.deleteDeployment(deployment.getId(), true);

  }

}
