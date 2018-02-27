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
package org.camunda.bpm.ext.sdk.impl.workers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class FetchAndLockRequestDto {

  protected int maxTasks;

  protected String workerId;

  protected long asyncResponseTimeout;

  protected List<FetchExternalTaskTopicDto> topics = new ArrayList<FetchExternalTaskTopicDto>();

  public int getMaxTasks() {
    return maxTasks;
  }

  public void setMaxTasks(int maxTasks) {
    this.maxTasks = maxTasks;
  }

  public String getWorkerId() {
    return workerId;
  }

  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  public List<FetchExternalTaskTopicDto> getTopics() {
    return topics;
  }

  public void setTopics(List<FetchExternalTaskTopicDto> topics) {
    this.topics = topics;
  }

  public long getAsyncResponseTimeout() {
    return asyncResponseTimeout;
  }

  public void setAsyncResponseTimeout(long asyncResponseTimeout) {
    this.asyncResponseTimeout = asyncResponseTimeout;
  }

  public void clear() {
    topics.clear();
  }

}
