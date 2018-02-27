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

import java.io.StringWriter;

import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.ext.sdk.CamundaClientException;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class JsonValueSerializer implements ValueSerializer<JsonValue> {

  public String getTypeName() {
    return SpinValueType.JSON.getName();
  }

  public boolean canSerialize(UntypedValueImpl value) {
    return false;
  }

  public JsonValue deserializeValue(TypedValueDto object, ObjectMapper objectMapper) {
    Object value = object.getValue();
    if(value == null) {
      return SpinValues.jsonValue((String)null).create();
    }
    if(value instanceof String) {
      return SpinValues.jsonValue((String) value).create();
    }
    throw new CamundaClientException("Value of type 'json' must be a serialized as string. Got :"+value);
  }

  public TypedValueDto serializeValue(JsonValue value, ObjectMapper objectMapper) {
    String serializedValue = null;
    if(value.getValue() != null) {
      StringWriter writer = new StringWriter();
      value.getValue().writeToWriter(writer);
      serializedValue = writer.toString();
    }
    TypedValueDto typedValueDto = new TypedValueDto();
    typedValueDto.setType("Json");
    typedValueDto.setValue(serializedValue);
    return typedValueDto;
  }

  public TypedValue convertToTypedValue(UntypedValueImpl valueTyped) {
    return null;
  }

}
