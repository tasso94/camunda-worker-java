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
package org.camunda.bpm.ext.sdk.impl.dto;

import java.util.Map;

import org.camunda.bpm.ext.sdk.impl.variables.TypedValueDto;

/**
 * @author Daniel Meyer
 *
 */
public class CompleteExternalTaskRequestDto {

    protected String workerId;
    protected Map<String, TypedValueDto> variables;
    protected Map<String, TypedValueDto> localVariables;

    public String getWorkerId() {
      return workerId;
    }

    public void setWorkerId(String workerId) {
      this.workerId = workerId;
    }

    public Map<String, TypedValueDto> getVariables() {
      return variables;
    }

    public void setVariables(Map<String, TypedValueDto> variables) {
      this.variables = variables;
    }

    public Map<String, TypedValueDto> getLocalVariables() {
      return localVariables;
    }

    public void setLocalVariables(Map<String, TypedValueDto> localVariables) {
      this.localVariables = localVariables;
    }

  }
