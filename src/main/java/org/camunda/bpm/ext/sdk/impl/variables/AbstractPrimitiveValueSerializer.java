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

import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.ext.sdk.CamundaClientException;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.camunda.bpm.ext.sdk.impl.variables.ValueSerializers.toRestApiTypeName;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractPrimitiveValueSerializer<T extends PrimitiveValue<?>> implements ValueSerializer<T> {

  protected PrimitiveValueType type;

  public AbstractPrimitiveValueSerializer(PrimitiveValueType type) {
    this.type = type;
  }

  public String getTypeName() {
    return type.getName();
  }

  public boolean canSerialize(UntypedValueImpl value) {
    if(value.getValue() == null) {
      return false;
    }
    else {
      return type.getJavaType().isAssignableFrom(value.getValue().getClass());
    }
  }

  public TypedValue convertToTypedValue(UntypedValueImpl untyped) {
    return type.createValue(untyped.getValue(), null);
  }

  @SuppressWarnings("unchecked")
  public T deserializeValue(TypedValueDto object, ObjectMapper objectMapper) {
    Object value = object.getValue();

    Class<?> javaType = type.getJavaType();
    Object mappedValue = null;
    try {
      if (value != null) {
        if (javaType.isAssignableFrom(value.getClass())) {
          mappedValue = value;
        } else {
          // use jackson to map the value to the requested java type
          mappedValue = objectMapper.readValue("\"" + value + "\"", javaType);
        }
      }
      return (T) type.createValue(mappedValue, object.getValueInfo());
    }
    catch(Exception e) {
      throw new CamundaClientException("Could not deserialize value of type "+object.getType()+ ", value: "+value, e);
    }
  }

  public TypedValueDto serializeValue(T value, ObjectMapper objectMapper) {
    TypedValueDto typedValueDto = new TypedValueDto();
    typedValueDto.setType(toRestApiTypeName(type.getName()));
    typedValueDto.setValue(value.getValue());
    typedValueDto.setValueInfo(type.getValueInfo(value));
    return typedValueDto;
  }


}
