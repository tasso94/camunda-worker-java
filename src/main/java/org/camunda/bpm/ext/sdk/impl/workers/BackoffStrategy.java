package org.camunda.bpm.ext.sdk.impl.workers;

/**
 * @author Daniel Meyer
 *
 */
public interface BackoffStrategy {

  void run() throws InterruptedException;

  void reset();

  void stopWait();

}