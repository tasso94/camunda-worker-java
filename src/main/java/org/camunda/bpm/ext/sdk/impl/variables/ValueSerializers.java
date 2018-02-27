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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.ext.sdk.CamundaClientException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class ValueSerializers {

  protected Map<String, ValueSerializer<?>> serializers = new HashMap<String, ValueSerializer<?>>();
  protected List<ValueSerializer<?>> prioritizedSerializers = new ArrayList<ValueSerializer<?>>();

  public ValueSerializers() {
    // primitives
    putSerializer(new StringValueSerializer());
    putSerializer(new BooleanValueSerializer());
    putSerializer(new DateValueSerializer());
    putSerializer(new DoubleValueSerializer());
    putSerializer(new IntegerValueSerializer());
    putSerializer(new ShortValueSerializer());
    putSerializer(new NullValueSerializer());
    putSerializer(new BytesValueSerializer());

    // objects
    putSerializer(new JsonValueSerializer());
    putSerializer(new ObjectValueSerializer());
  }

  public ValueSerializer<?> getSerializerForValue(TypedValue value) {
    if(value instanceof UntypedValueImpl) {
      for (ValueSerializer<?> serializer : prioritizedSerializers) {
        if(serializer.canSerialize((UntypedValueImpl) value)) {
          return serializer;
        }
      }
    }
    return serializers.get(value.getType().getName());
  }

  public ValueSerializer<?> getSerializerForType(String typeName) {
    return serializers.get(typeName);
  }

  public void putSerializer(ValueSerializer<?> serializer) {
    serializers.put(serializer.getTypeName(), serializer);
    prioritizedSerializers.add(serializer);
  }

  public VariableMap readValues(Map<String, TypedValueDto> dtoMap, ObjectMapper objectMapper) {
    VariableMapImpl variables = new VariableMapImpl();
    if(dtoMap != null) {
      for (Entry<String, TypedValueDto> dto : dtoMap.entrySet()) {
        String type = dto.getValue().getType();
        ValueSerializer<?> serializer = getSerializerForType(fromRestApiTypeName(type));
        if(serializer == null) {
          throw new CamundaClientException("Cannot find serializer for type " +type);
        }
        TypedValue typedValue = serializer.deserializeValue(dto.getValue(), objectMapper);
        variables.put(dto.getKey(), typedValue);
      }
    }

    return variables;
  }

  public Map<String, TypedValueDto> writeValues(VariableMap variableMap, ObjectMapper mapper) {
    Map<String, TypedValueDto> result = new HashMap<String, TypedValueDto>();
    if(variableMap != null) {
      for (String variableName : variableMap.keySet()) {
        TypedValue valueTyped = variableMap.getValueTyped(variableName);
        ValueSerializer serializer = getSerializerForValue(valueTyped);
        if(serializer == null) {
          throw new CamundaClientException("Cannot find serializer for value "+valueTyped);
        }
        if(valueTyped instanceof UntypedValueImpl) {
          valueTyped = serializer.convertToTypedValue((UntypedValueImpl) valueTyped);
        }
        TypedValueDto valueDto = serializer.serializeValue(valueTyped, mapper);
        result.put(variableName, valueDto);
      }
    }
    return result;
  }

  public static String toRestApiTypeName(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  public static String fromRestApiTypeName(String name) {
    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }

}
