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

import org.camunda.bpm.ext.sdk.ClientLogger;

/**
 * @author Daniel Meyer
 *
 */
public class SimpleBackoffStrategy implements BackoffStrategy {

  private static ClientLogger LOG = ClientLogger.LOGGER;

  // config
  int waitIncrease = 1000;
  int maxWait =  5 * 1000;
  int minWait = 1000;

  // state
  int wait = minWait;

  public SimpleBackoffStrategy() {

  }

  public SimpleBackoffStrategy(int waitIncrease, int maxWait, int minWait) {
    this.waitIncrease = waitIncrease;
    this.maxWait = maxWait;
    this.minWait = minWait;
  }

  public void run() throws InterruptedException {
    if(wait != minWait) {
      LOG.backOff(wait);
    }
    synchronized (this) {
      this.wait(wait);
    }
    wait = Math.min(maxWait, wait + waitIncrease);
  }

  public void reset() {
    wait = minWait;
  }

  public void stopWait() {
    synchronized (this) {
      this.notifyAll();
    }
  }

}