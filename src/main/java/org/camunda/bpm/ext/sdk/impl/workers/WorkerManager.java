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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.ext.sdk.ClientLogger;
import org.camunda.bpm.ext.sdk.impl.ClientCommandExecutor;
import org.camunda.bpm.ext.sdk.impl.WorkerRegistrationImpl;

/**
 * @author Daniel Meyer
 *
 */
public class WorkerManager {

  private final static ClientLogger LOG = ClientLogger.LOGGER;

  protected Thread fetcherThread;

  protected FetchAndLockTasksRunnable fetcherRunnable;

  protected List<WorkerRegistrationImpl> registrations = new ArrayList<>();

  protected ThreadPoolExecutor workerThreadPool;

  public WorkerManager(ClientCommandExecutor commandExecutor, int numOfWorkerThreads, int queueSize) {
    this.fetcherRunnable = new FetchAndLockTasksRunnable(this, commandExecutor);

    ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueSize);

    fetcherThread = new Thread(fetcherRunnable);
    fetcherThread.start();

    workerThreadPool = new ThreadPoolExecutor(1, numOfWorkerThreads, 10, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public void close() {
    fetcherRunnable.exit();
    try {
      fetcherThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    workerThreadPool.shutdown();
    try {
      workerThreadPool.awaitTermination(5, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void execute(Runnable task) {
    workerThreadPool.execute(task);
  }

  public List<WorkerRegistrationImpl> getRegistrations() {
    return registrations;
  }

  public void register(WorkerRegistrationImpl registration) {
    synchronized (registrations) {
      registrations.add(registration);
      registrations.notifyAll();
    }
    LOG.registeredNewWorker(registration);
  }

  public void remove(WorkerRegistrationImpl registration) {
    LOG.registeredNewWorker(registration);
    synchronized (registrations) {
      registrations.remove(registration);
    }
  }

}
