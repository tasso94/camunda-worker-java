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

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.ObjectTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.ext.sdk.CamundaClientException;
import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.spi.DataFormatReader;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class ObjectValueSerializer implements ValueSerializer<ObjectValue> {

  public String getTypeName() {
    return ValueType.OBJECT.getName();
  }

  public boolean canSerialize(UntypedValueImpl value) {
    return true;
  }

  public ObjectValue deserializeValue(TypedValueDto object, ObjectMapper objectMapper) {
    Map<String, Object> valueInfo = object.getValueInfo();
    String objectTypeName = (String) valueInfo.get(ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME);
    String serializationDataFormat = (String) valueInfo.get(ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT);

    DataFormat<? extends Spin<?>> dataFormat = DataFormats.getDataFormat(serializationDataFormat);
    if(serializationDataFormat == null) {
      throw new CamundaClientException("Cannot find dataformat "+serializationDataFormat);
    }

    Object value = null;
    if(object.getValue() != null) {

      DataFormatMapper mapper = dataFormat.getMapper();
      DataFormatReader reader = dataFormat.getReader();


      BufferedReader bufferedReader = new BufferedReader(new StringReader((String) object.getValue()));

      try {
        Object mappedObject = reader.readInput(bufferedReader);
        value = mapper.mapInternalToJava(mappedObject, objectTypeName);
      }
      finally{
        IoUtil.closeSilently(bufferedReader);
      }
    }

    return Variables.objectValue(value)
        .serializationDataFormat(serializationDataFormat)
        .create();
  }

  public TypedValueDto serializeValue(ObjectValue value, ObjectMapper objectMapper) {
    Object objectValue = value.getValue();

    Map<String, Object> valueInfo = new HashMap<String, Object>();
    String serializedValue = null;
    if(objectValue != null) {
      DataFormat<?> dataFormat = null;
      String requestedSerializationDataformatName = value.getSerializationDataFormat();
      if(requestedSerializationDataformatName == null) {
        for (DataFormat<? extends Spin<?>> df : DataFormats.getAvailableDataFormats()) {
          if(df.getMapper().canMap(objectValue)) {
            dataFormat = df;
          }
        }
      }
      else {
        dataFormat = DataFormats.getDataFormat(requestedSerializationDataformatName);
      }

      Object internal = dataFormat.getMapper().mapJavaToInternal(objectValue);

      StringWriter valueWriter = new StringWriter();
      dataFormat.getWriter().writeToWriter(valueWriter, internal);

      serializedValue = valueWriter.toString();

      valueInfo.put(ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME,
          dataFormat.getMapper().getCanonicalTypeName(objectValue));
      valueInfo.put(ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, dataFormat.getName());
    }


    TypedValueDto typedValueDto = new TypedValueDto();
    typedValueDto.setType(ValueSerializers.toRestApiTypeName(ValueType.OBJECT.getName()));
    typedValueDto.setValue(serializedValue);
    typedValueDto.setValueInfo(valueInfo);

    return typedValueDto;
  }

  public TypedValue convertToTypedValue(UntypedValueImpl untyped) {
    return Variables.objectValue(untyped.getValue()).create();
  }

}
