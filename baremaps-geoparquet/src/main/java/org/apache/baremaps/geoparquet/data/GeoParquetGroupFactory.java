/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.geoparquet.data;

import java.util.List;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Field;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName;

public class GeoParquetGroupFactory {

  private final GroupType schema;

  private final GeoParquetMetadata metadata;

  private final GeoParquetGroup.Schema geoParquetSchema;

  public GeoParquetGroupFactory(GroupType schema, GeoParquetMetadata metadata) {
    this.schema = schema;
    this.metadata = metadata;
    this.geoParquetSchema = createGeoParquetSchema(schema, metadata);
  }

  public static GeoParquetGroup.Schema createGeoParquetSchema(
      GroupType schema,
      GeoParquetMetadata metadata) {
    List<Field> fields = schema.getFields().stream().map(field -> {
      GeoParquetGroup.Cardinality cardinality = switch (field.getRepetition()) {
        case REQUIRED -> GeoParquetGroup.Cardinality.REQUIRED;
        case OPTIONAL -> GeoParquetGroup.Cardinality.OPTIONAL;
        case REPEATED -> GeoParquetGroup.Cardinality.REPEATED;
      };
      if (field.isPrimitive() && metadata.isGeometryColumn(field.getName())) {
        return new GeoParquetGroup.GeometryField(field.getName(), cardinality);
      } else if (field.isPrimitive()) {
        PrimitiveType primitiveType = field.asPrimitiveType();
        PrimitiveTypeName primitiveTypeName = primitiveType.getPrimitiveTypeName();
        String name = primitiveType.getName();
        return switch (primitiveTypeName) {
          case INT32 -> new GeoParquetGroup.IntegerField(name, cardinality);
          case INT64 -> new GeoParquetGroup.LongField(name, cardinality);
          case INT96 -> new GeoParquetGroup.Int96Field(name, cardinality);
          case FLOAT -> new GeoParquetGroup.FloatField(name, cardinality);
          case DOUBLE -> new GeoParquetGroup.DoubleField(name, cardinality);
          case BOOLEAN -> new GeoParquetGroup.BooleanField(name, cardinality);
          case BINARY -> new GeoParquetGroup.BinaryField(name, cardinality);
          case FIXED_LEN_BYTE_ARRAY -> new GeoParquetGroup.BinaryField(name, cardinality);
        };
      } else {
        GroupType groupType = field.asGroupType();
        return (Field) new GeoParquetGroup.GroupField(groupType.getName(),
            GeoParquetGroup.Cardinality.REQUIRED, createGeoParquetSchema(groupType, metadata));
      }
    }).toList();
    return new GeoParquetGroup.Schema(schema.getName(), fields);
  }

  public GeoParquetGroupImpl newGroup() {
    return new GeoParquetGroupImpl(schema, metadata, geoParquetSchema);
  }

}
