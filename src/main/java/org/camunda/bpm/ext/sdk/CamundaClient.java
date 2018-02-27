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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.ext.sdk.dto.ProcessInstanceResponseDto;
import org.camunda.bpm.ext.sdk.dto.StartProcessInstanceDto;
import org.camunda.bpm.ext.sdk.dto.StartProcessInstanceResponseDto;
import org.camunda.bpm.ext.sdk.impl.ClientCommandContext;
import org.camunda.bpm.ext.sdk.impl.ClientCommandExecutor;
import org.camunda.bpm.ext.sdk.impl.ClientPostComand;
import org.camunda.bpm.ext.sdk.impl.variables.TypedValueDto;
import org.camunda.bpm.ext.sdk.impl.workers.LockedExternalTaskDto;
import org.camunda.bpm.ext.sdk.impl.workers.WorkerManager;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Meyer
 *
 */
public class CamundaClient {

  private final static ClientLogger LOG = ClientLogger.LOGGER;

  protected ClientCommandExecutor commandExecutor;
  protected WorkerManager workerManager;

  CamundaClient(CamundaClientBuilder camundaClientBuilder) {
    this.commandExecutor = camundaClientBuilder.clientCommandExecutor;
    this.workerManager = camundaClientBuilder.workerManager;
  }

  public static CamundaClientBuilder create() {
    return new CamundaClientBuilder();
  }

  // instance methods ////////////////////////////////

  public WorkerRegistrationBuilder registerWorker() {
    return new WorkerRegistrationBuilder(workerManager);
  }

  public DeploymentBuilder createDeployment() {
    return new DeploymentBuilder(commandExecutor);
  }

  public StartProcessInstanceResponseDto startProcessInstanceByKey(String key) {
    return startProcessInstanceByKey(key, null);
  }

  public StartProcessInstanceResponseDto startProcessInstanceByKey(String key, final Map<String, Object> variables) {
    return startProcessInstanceByKey(key, null, variables);
  }

  public StartProcessInstanceResponseDto startProcessInstanceByKeyRaw(String key, final String rawPayload) {
    return commandExecutor.executePost("/process-definition/key/"+key+"/start", new ClientPostComand<StartProcessInstanceResponseDto>() {
      public StartProcessInstanceResponseDto execute(ClientCommandContext ctc, HttpPost post) {
        post.setEntity(new StringEntity(rawPayload, Charset.defaultCharset()));
        HttpResponse response = ctc.execute(post);
        return ctc.readObject(response.getEntity(), StartProcessInstanceResponseDto.class);
      }
    });
  }

  public StartProcessInstanceResponseDto startProcessInstanceByKey(String key, final String businessKey, final Map<String, Object> variables) {
    return commandExecutor.executePost("/process-definition/key/"+key+"/start", new ClientPostComand<StartProcessInstanceResponseDto>() {
      public StartProcessInstanceResponseDto execute(ClientCommandContext ctc, HttpPost post) {

        StartProcessInstanceDto startProcessInstanceDto = new StartProcessInstanceDto();
        startProcessInstanceDto.setVariables(ctc.writeVariables(Variables.fromMap(variables)));
        startProcessInstanceDto.setBusinessKey(businessKey);

        post.setEntity(ctc.writeObject(startProcessInstanceDto));
        HttpResponse response = ctc.execute(post);
        return ctc.readObject(response.getEntity(), StartProcessInstanceResponseDto.class);
      }
    });
  }

  public void deleteDeployment(String deploymentId, boolean cascade) {
    commandExecutor.executeDelete("/deployment/"+deploymentId+(cascade ? "?cascade=true": ""));
  }

  public ProcessInstanceResponseDto queryProcessInstanceById(String processInstanceId) {
    return commandExecutor.executeGet("/process-instance/" +  processInstanceId, (ctc, get) -> {
      HttpResponse response = ctc.execute(get);
      return ctc.readObject(response.getEntity(), ProcessInstanceResponseDto.class);
    });
  }

  public List<ProcessInstanceResponseDto> queryProcessInstanceByKey(String processDefinitionKey) {
    return commandExecutor.executeGet("/process-instance/?processDefinitionKey=" +  processDefinitionKey, (ctc, get) -> {
      HttpResponse response = ctc.execute(get);
      return Arrays.asList(ctc.readObject(response.getEntity(), ProcessInstanceResponseDto[].class));
    });
  }

  public List<LockedExternalTaskDto> queryExternalTasksByTopicName(String topicName) {
    return commandExecutor.executeGet("/external-task/?topicName=" +  topicName, (ctc, get) -> {
      HttpResponse response = ctc.execute(get);
      return Arrays.asList(ctc.readObject(response.getEntity(), LockedExternalTaskDto[].class));
    });
  }

  public List<LockedExternalTaskDto> queryExternalTaskLogsByTopicName(String topicName) {
    return commandExecutor.executeGet("/history/external-task-log?topicName=" +  topicName, (ctc, get) -> {
      HttpResponse response = ctc.execute(get);
      return Arrays.asList(ctc.readObject(response.getEntity(), LockedExternalTaskDto[].class));
    });
  }

  public List<TypedValueDto> queryHistoricVariableInstancesByProcessDefinitionKey(String processDefinitionKey) {
    return commandExecutor.executeGet("/history/variable-instance?processDefinitionKey=" + processDefinitionKey, (ctc, get) -> {
      HttpResponse response = ctc.execute(get);
      return Arrays.asList(ctc.readObject(response.getEntity(), TypedValueDto[].class));
    });
  }


  public void close() {
    LOG.closing();
    workerManager.close();
    commandExecutor.close();
  }

}
