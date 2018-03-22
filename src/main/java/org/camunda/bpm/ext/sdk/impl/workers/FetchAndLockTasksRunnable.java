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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.camunda.bpm.ext.sdk.CamundaClientException;
import org.camunda.bpm.ext.sdk.ClientLogger;
import org.camunda.bpm.ext.sdk.Worker;
import org.camunda.bpm.ext.sdk.impl.ClientCommandContext;
import org.camunda.bpm.ext.sdk.impl.ClientCommandExecutor;
import org.camunda.bpm.ext.sdk.impl.ClientPostComand;
import org.camunda.bpm.ext.sdk.impl.WorkerRegistrationImpl;

/**
 * @author Daniel Meyer
 *
 */
public class FetchAndLockTasksRunnable implements Runnable {

  private final static ClientLogger LOG = ClientLogger.LOGGER;

  protected transient boolean exit = false;

  protected WorkerManager workerManager;
  protected ClientCommandExecutor commandExecutor;
  protected BackoffStrategy backoffStrategy;
  
  public FetchAndLockTasksRunnable(WorkerManager workerManager, ClientCommandExecutor commandExecutor, BackoffStrategy backoffStrategy) {
    this.workerManager = workerManager;
    this.commandExecutor = commandExecutor;
    this.backoffStrategy = backoffStrategy;
  }

  public void run() {
    while(!exit) {
      acquire();
    }
  }

  protected void acquire() {
    final List<WorkerRegistrationImpl> registrations = workerManager.getRegistrations();
    final FetchAndLockRequestDto request = new FetchAndLockRequestDto();
    final Map<String, Worker> workerMap = new HashMap<>();

    request.clear();
    workerMap.clear();


    synchronized (registrations) {
      int numOfRegistrations = registrations.size();

      if(numOfRegistrations == 0) {
        try {
          registrations.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
          // TODO
        }
      }

      for (WorkerRegistrationImpl registration : registrations) {
        request.topics.add(new FetchExternalTaskTopicDto(registration.getTopicName(), registration.getLockDuration(), registration.getVariableNames()));
        workerMap.put(registration.getTopicName(), registration.getWorker());
      }

    }
    
    int tasksAcquired = 0;

    try {
      fetchAndLock(request, workerMap);
    } catch(Exception e) {
      LOG.exceptionDuringPoll(e);
    }
    
    if(tasksAcquired == 0) {
        try {
          // back-off
          backoffStrategy.run();
        } catch(InterruptedException e) {
          e.printStackTrace();
        }
    } else {
        backoffStrategy.reset();
    }
  }

  private int fetchAndLock(final FetchAndLockRequestDto request, final Map<String, Worker> workerMap) {
	if (workerMap.size() == 0){
		return 0;
  	}
	
    return commandExecutor.executePost("/external-task/fetchAndLock", (ClientPostComand<Integer>) (ClientCommandContext ctc, HttpPost post) -> {
      request.setWorkerId(ctc.getClientId());
      request.setMaxTasks(ctc.getMaxTasks());
      request.setAsyncResponseTimeout(ctc.getAsyncResponseTimeout());

      post.setEntity(ctc.writeObject(request));

      int tasksAcquired = 0;
      try {
        HttpResponse response = ctc.execute(post);
        LockedExternalTaskDto[] lockedTasksResponseDto = ctc.readObject(response.getEntity(), LockedExternalTaskDto[].class);


        for (LockedExternalTaskDto lockedExternalTaskDto : lockedTasksResponseDto) {

          WorkerTask task = WorkerTask.from(lockedExternalTaskDto, commandExecutor, workerMap.get(lockedExternalTaskDto.getTopicName()));
          workerManager.execute(task);
          tasksAcquired++;
        }
      } catch (CamundaClientException e) {
        LOG.unableToPoll(e);
      }

      return tasksAcquired;
    });
  }

  public void exit() {
    exit = true;
    // thread may be either waiting for a registration to open
    synchronized (workerManager.getRegistrations()) {
      workerManager.getRegistrations().notifyAll();
    }
    // or doing backoff
    backoffStrategy.stopWait();
  }

}
