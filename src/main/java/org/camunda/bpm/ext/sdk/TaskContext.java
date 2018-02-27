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

import java.util.Map;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 *
 */
public interface TaskContext {

  String getTaskId();

  <T> T getVariable(String name);

  <T extends TypedValue> T getVariableTyped(String name);

  VariableMap getVariables();

  void setAllVariables(Map<String, Object> variables);

  void setVariable(String name, Object value);

  void complete(Map<String, Object> variables, Map<String, Object> localVariables);

  void complete();

  void handleFailure(String errorMessage, int retries, long retryTimeout);

  void handleFailure();

}
