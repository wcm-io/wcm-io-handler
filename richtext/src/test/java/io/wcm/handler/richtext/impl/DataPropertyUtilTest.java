/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * #L%
 */
package io.wcm.handler.richtext.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DataPropertyUtilTest {

  @Test
  void testToHtml5DataNameNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      DataPropertyUtil.toHtml5DataName(null);
    });
  }

  @Test
  void testToHtml5DataNameEmptyString() {
    assertThrows(IllegalArgumentException.class, () -> {
      DataPropertyUtil.toHtml5DataName("");
    });
  }

  @Test
  void testToHtml5DataNameEmptyStringNonHeadlessCamelCaseName() {
    assertThrows(IllegalArgumentException.class, () -> {
      DataPropertyUtil.toHtml5DataName("Abc");
    });
  }

  @Test
  void testToHtml5DataName() {
    assertEquals("data-xyz", DataPropertyUtil.toHtml5DataName("xyz"));
    assertEquals("data-xyz-abc", DataPropertyUtil.toHtml5DataName("xyzAbc"));
    assertEquals("data-xyz2-abc-def", DataPropertyUtil.toHtml5DataName("xyz2AbcDef"));
    assertEquals("data-x-a-bz", DataPropertyUtil.toHtml5DataName("xABz"));
  }

  @Test
  void testToHeadlessCamelCaseNameNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      DataPropertyUtil.toHeadlessCamelCaseName(null);
    });
  }

  @Test
  void testToHeadlessCamelCaseNameEmptyString() {
    assertThrows(IllegalArgumentException.class, () -> {
      DataPropertyUtil.toHeadlessCamelCaseName("");
    });
  }

  @Test
  void testToHeadlessCamelCaseNameNonHtml5DataName() {
    assertThrows(IllegalArgumentException.class, () -> {
      DataPropertyUtil.toHeadlessCamelCaseName("xyz");
    });
  }

  @Test
  void testToHeadlessCamelCaseName() {
    assertEquals("xyz", DataPropertyUtil.toHeadlessCamelCaseName("data-xyz"));
    assertEquals("xyzAbc", DataPropertyUtil.toHeadlessCamelCaseName("data-xyz-abc"));
    assertEquals("xyz2AbcDef", DataPropertyUtil.toHeadlessCamelCaseName("data-xyz2-abc-def"));
    assertEquals("xABz", DataPropertyUtil.toHeadlessCamelCaseName("data-x-a-bz"));
  }

  @Test
  void testIsHeadlessCamelCaseName() {
    assertFalse(DataPropertyUtil.isHeadlessCamelCaseName(null));
    assertFalse(DataPropertyUtil.isHeadlessCamelCaseName(""));
    assertFalse(DataPropertyUtil.isHeadlessCamelCaseName("Xyz"));
    assertFalse(DataPropertyUtil.isHeadlessCamelCaseName("XYZ"));
    assertFalse(DataPropertyUtil.isHeadlessCamelCaseName("xyz-abc"));
    assertFalse(DataPropertyUtil.isHeadlessCamelCaseName("0xyz"));

    assertTrue(DataPropertyUtil.isHeadlessCamelCaseName("xyz"));
    assertTrue(DataPropertyUtil.isHeadlessCamelCaseName("xyzAbc"));
    assertTrue(DataPropertyUtil.isHeadlessCamelCaseName("xyz2Abc"));
  }

  @Test
  void testIsHtml5DataName() {
    assertFalse(DataPropertyUtil.isHtml5DataName(null));
    assertFalse(DataPropertyUtil.isHtml5DataName(""));
    assertFalse(DataPropertyUtil.isHtml5DataName("xyz"));
    assertFalse(DataPropertyUtil.isHtml5DataName("data"));
    assertFalse(DataPropertyUtil.isHtml5DataName("dataxyz"));
    assertFalse(DataPropertyUtil.isHtml5DataName("data-"));
    assertFalse(DataPropertyUtil.isHtml5DataName("data-Xyz"));
    assertFalse(DataPropertyUtil.isHtml5DataName("data-xyZ"));
    assertFalse(DataPropertyUtil.isHtml5DataName("data-XYZ"));
    assertFalse(DataPropertyUtil.isHtml5DataName("data-0xyz"));

    assertTrue(DataPropertyUtil.isHtml5DataName("data-xyz"));
    assertTrue(DataPropertyUtil.isHtml5DataName("data-xxx-yyy-zzz"));
  }

}
