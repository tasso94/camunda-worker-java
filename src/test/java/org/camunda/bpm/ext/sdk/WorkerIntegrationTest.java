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

import org.camunda.bpm.ext.sdk.dto.DeploymentDto;
import org.camunda.bpm.ext.sdk.impl.variables.TypedValueDto;
import org.camunda.bpm.ext.sdk.impl.workers.LockedExternalTaskDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tassilo Weidner
 */
public class WorkerIntegrationTest {

  private CamundaClient client;
  private DeploymentDto deployment;

  @Before
  public void setup() {
    client = CamundaClient.create()
      .endpointUrl("http://localhost:8080/engine-rest")
      .maxTasks(10)
      .asyncResponseTimeout(5000)
      .build();

    deployment = client.createDeployment()
      .name("testDeployment")
      .enableDuplicateFiltering()
      .classPathResource("org/camunda/bpm/ext/worker/test/oneTaskProcess.bpmn")
      .deploy();
  }

  @After
  public void close() {
    client.deleteDeployment(deployment.getId(), true);
    client.close();
  }

  @Test
  public void shouldCompleteTasks() throws InterruptedException {
    // given
    for (int i = 0; i < 10; i++) {
      client.startProcessInstanceByKey("testProcess");
    }

    // when
    client.registerWorker()
      .lockDuration(5000)
      .topicName("exampleTopicName")
      .worker(TaskContext::complete)
      .build();

    client.workerManager.close();

    // then
    List<LockedExternalTaskDto> tasks = client.queryExternalTasksByTopicName("exampleTopicName");
    assertThat(tasks.size(), is(0));
  }

  @Test
  public void shouldHandleFailure() throws InterruptedException {
    // given
    for (int i = 0; i < 10; i++) {
      client.startProcessInstanceByKey("testProcess");
    }

    // when
    client.registerWorker()
      .lockDuration(5000)
      .topicName("exampleTopicName")
      .worker(taskContext -> {
        taskContext.handleFailure("error message", 5, 5);
      }).build();

    client.workerManager.close();

    // then
    List<LockedExternalTaskDto> tasks = client.queryExternalTasksByTopicName("exampleTopicName");
    assertThat(tasks.size(), is(10));

    for (LockedExternalTaskDto task : tasks) {
      assertThat(task.getErrorMessage(), is("error message"));
      assertThat(task.getRetries(), is(5));
    }
  }

  @Test
  public void shouldSetLocalVariables() throws InterruptedException {
    // given
    for (int i = 0; i < 10; i++) {
      client.startProcessInstanceByKey("testProcess");
    }

    // when
    client.registerWorker()
      .lockDuration(5000)
      .topicName("exampleTopicName")
      .worker(taskContext -> {
        Map<String, Object> localVariables = new HashMap<>();
        localVariables.put("aVariableName", "aVariableValue");
        taskContext.complete(null, localVariables);
      }).build();

    client.workerManager.close();

    // then
    List<TypedValueDto> variables = client.queryHistoricVariableInstancesByProcessDefinitionKey("testProcess");
    assertThat(variables.size(), is(10));

    for (TypedValueDto variable : variables) {
      assertThat(variable.getValue(), is("aVariableValue"));
    }
  }

}
