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

import java.util.Arrays;

import org.camunda.bpm.ext.sdk.impl.WorkerRegistrationImpl;
import org.camunda.bpm.ext.sdk.impl.workers.WorkerManager;

/**
 * @author Daniel Meyer
 *
 */
public class WorkerRegistrationBuilder {

  protected WorkerManager workerManager;
  protected WorkerRegistrationImpl registration;

  public WorkerRegistrationBuilder(WorkerManager workerManager) {
    registration = new WorkerRegistrationImpl(workerManager);
    this.workerManager = workerManager;
  }

  public WorkerRegistrationBuilder topicName(String topicName) {
    registration.setTopicName(topicName);
    return this;
  }

  public WorkerRegistrationBuilder lockDuration(long lockDuration) {
    registration.setLockDuration(lockDuration);
    return this;
  }

  public WorkerRegistrationBuilder worker(Worker worker) {
    registration.setWorker(worker);
    return this;
  }

  public WorkerRegistrationBuilder variableNames(String... variableNames) {
    registration.getVariableNames().addAll(Arrays.asList(variableNames));
    return this;
  }

  public WorkerRegistration build() {
    workerManager.register(registration);
    return registration;
  }

}
