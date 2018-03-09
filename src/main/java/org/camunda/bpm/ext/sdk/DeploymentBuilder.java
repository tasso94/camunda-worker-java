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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.camunda.bpm.ext.sdk.dto.DeploymentDto;
import org.camunda.bpm.ext.sdk.impl.ClientCommandContext;
import org.camunda.bpm.ext.sdk.impl.ClientCommandExecutor;
import org.camunda.bpm.ext.sdk.impl.ClientPostMultipartComand;

/**
 * @author Daniel Meyer
 *
 */
public class DeploymentBuilder {

  protected ClientCommandExecutor commandExecutor;
  protected MultipartEntityBuilder requestBuilder;
  protected int dataPartConter = 0;

  public DeploymentBuilder(ClientCommandExecutor commandExecutor) {
    requestBuilder = MultipartEntityBuilder.create();
    this.commandExecutor = commandExecutor;
  }

  public DeploymentBuilder name(String deploymentName) {
    requestBuilder.addTextBody("deployment-name", deploymentName);
    return this;
  }
  
  public DeploymentBuilder source(String deploymentSource) {
	    requestBuilder.addTextBody("deployment-source", deploymentSource);
	    return this;
  }

  public DeploymentBuilder classPathResource(String classpathResource) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if(cl == null) {
      cl = CamundaClient.class.getClassLoader();
    }
    InputStream inputStream = cl.getResourceAsStream(classpathResource);

    String filename = classpathResource;
    requestBuilder.addBinaryBody("resource-" + (++dataPartConter), inputStream, ContentType.APPLICATION_OCTET_STREAM, filename);
    return this;
  }

  public DeploymentBuilder stringResource(String filename, String content) {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
    requestBuilder.addBinaryBody("resource-" + (++dataPartConter), inputStream, ContentType.APPLICATION_OCTET_STREAM, filename);
    return this;
  };

  public DeploymentBuilder enableDuplicateFiltering() {
    requestBuilder.addTextBody("enable-duplicate-filtering", "true");
    return this;
  }

  public DeploymentDto deploy() {
    return commandExecutor.executePostMultipart("/deployment/create", new ClientPostMultipartComand<DeploymentDto>() {

      public DeploymentDto execute(ClientCommandContext ctc, HttpPost post) {
        post.setEntity(requestBuilder.build());
        HttpResponse response = ctc.execute(post);
        return ctc.readObject(response.getEntity(), DeploymentDto.class);
      }
    });
  }

}
