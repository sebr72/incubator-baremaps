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

import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;


public class GeometryValue extends Primitive {

  private final Binary binary;

  public GeometryValue(Binary binary) {
    this.binary = binary;
  }

  public GeometryValue(Geometry geometry) {
    this.binary = Binary.fromConstantByteArray(new WKBWriter().write(geometry));
  }

  @Override
  public Geometry getGeometry() {
    try {
      return new WKBReader().read(binary.getBytes());
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getString() {
    try {
      return new WKBReader().read(binary.getBytes()).toString();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeValue(RecordConsumer recordConsumer) {
    recordConsumer.addBinary(binary);
  }

  @Override
  public String toString() {
    return getString();
  }
}
