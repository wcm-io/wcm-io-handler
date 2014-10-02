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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DataPropertyUtilTest {

  @Test(expected = IllegalArgumentException.class)
  public void testToHtml5DataNameNull() {
    DataPropertyUtil.toHtml5DataName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToHtml5DataNameEmptyString() {
    DataPropertyUtil.toHtml5DataName("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToHtml5DataNameEmptyStringNonHeadlessCamelCaseName() {
    DataPropertyUtil.toHtml5DataName("Abc");
  }

  @Test
  public void testToHtml5DataName() {
    assertEquals("data-xyz", DataPropertyUtil.toHtml5DataName("xyz"));
    assertEquals("data-xyz-abc", DataPropertyUtil.toHtml5DataName("xyzAbc"));
    assertEquals("data-xyz2-abc-def", DataPropertyUtil.toHtml5DataName("xyz2AbcDef"));
    assertEquals("data-x-a-bz", DataPropertyUtil.toHtml5DataName("xABz"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToHeadlessCamelCaseNameNull() {
    DataPropertyUtil.toHeadlessCamelCaseName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToHeadlessCamelCaseNameEmptyString() {
    DataPropertyUtil.toHeadlessCamelCaseName("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToHeadlessCamelCaseNameNonHtml5DataName() {
    DataPropertyUtil.toHeadlessCamelCaseName("xyz");
  }

  @Test
  public void testToHeadlessCamelCaseName() {
    assertEquals("xyz", DataPropertyUtil.toHeadlessCamelCaseName("data-xyz"));
    assertEquals("xyzAbc", DataPropertyUtil.toHeadlessCamelCaseName("data-xyz-abc"));
    assertEquals("xyz2AbcDef", DataPropertyUtil.toHeadlessCamelCaseName("data-xyz2-abc-def"));
    assertEquals("xABz", DataPropertyUtil.toHeadlessCamelCaseName("data-x-a-bz"));
  }

  @Test
  public void testIsHeadlessCamelCaseName() {
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
  public void testIsHtml5DataName() {
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
