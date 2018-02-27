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
package org.camunda.bpm.ext.sdk.impl.variables;

import org.camunda.bpm.engine.variable.impl.value.NullValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class NullValueSerializer implements ValueSerializer<NullValueImpl> {

  public String getTypeName() {
    return ValueType.NULL.getName();
  }

  public boolean canSerialize(UntypedValueImpl value) {
    return value == null;
  }

  public NullValueImpl deserializeValue(TypedValueDto object, ObjectMapper objectMapper) {
    return NullValueImpl.INSTANCE;
  }

  public TypedValueDto serializeValue(NullValueImpl value, ObjectMapper objectMapper) {
    TypedValueDto typedValueDto = new TypedValueDto();
    typedValueDto.type = "Null";
    return typedValueDto;
  }

  public TypedValue convertToTypedValue(UntypedValueImpl valueTyped) {
    return NullValueImpl.INSTANCE;
  }

}
