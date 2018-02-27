import org.camunda.bpm.ext.sdk.CamundaClient;
import org.camunda.bpm.ext.sdk.TaskContext;
import org.camunda.bpm.ext.sdk.Worker;
import org.camunda.bpm.ext.sdk.WorkerRegistration;

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

/**
 * @author Daniel Meyer
 *
 */
public class Consumer {

  public static void main(String[] args) {

    CamundaClient client = CamundaClient.create()
      .endpointUrl("http://192.168.88.216:8080/engine-rest/")
      .build();

    WorkerRegistration registration = client.registerWorker()
      .topicName("orderProcess:payment")
      .lockDuration(5000)
      .worker(new Worker() {

        public void doWork(TaskContext taskContext) {

//          if(new Date().getTime() % 13 == 0) {
//            taskContext.handleFailure("reserveOrderItems failed");
//            System.out.println("Task failed");
//          }
//          else {
////            System.out.println("reserveOrderItems complete");
            taskContext.complete();
//          }

        }
      })
      .build();

  }

}
