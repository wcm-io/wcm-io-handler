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
package io.wcm.handler.commons.editcontext;

import java.util.Map;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.components.DropTarget;
import com.google.common.collect.ImmutableMap;

/**
 * {@link DropTarget} implementation.
 */
@ProviderType
public final class DropTargetImpl implements DropTarget {

  private final String name;
  private final String id;
  private final String propertyName;
  private String[] groups = new String[0];
  private String[] accept = new String[] {
      "*"
  };
  private Map<String, String> parameters = ImmutableMap.of();

  /**
   * @param name drop target name
   * @param propertyName property name
   */
  public DropTargetImpl(String name, String propertyName) {
    this.name = name;
    this.id = CSS_CLASS_PREFIX + this.name;
    this.propertyName = propertyName;
  }

  /**
   * Returns the name of this drop target
   * @return drop target name
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Returns the id for the drop target element. this is currently the same
   * as the name, prefixed with the {@link #CSS_CLASS_PREFIX}
   * @return drop target id
   */
  @Override
  public String getId() {
    return this.id;
  }

  /**
   * Returns the property name to use for this drop target.
   * @return the property name.
   */
  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  /**
   * Returns the DD groups for this target
   * @return the DD groups.
   */
  @Override
  public String[] getGroups() {
    return this.groups;
  }

  /**
   * @param values the DD groups.
   * @return this
   */
  public DropTargetImpl setGroups(String[] values) {
    this.groups = values;
    return this;
  }

  /**
   * Returns the mime-type accept patterns
   * @return the accept patterns
   */
  @Override
  public String[] getAccept() {
    return this.accept;
  }

  /**
   * @param values the accept patterns
   * @return this
   */
  public DropTargetImpl setAccept(String[] values) {
    this.accept = values;
    return this;
  }

  /**
   * Returns a map of additional parameters to be set on the target node
   * @return a map of additional parameters.
   */
  @Override
  public Map<String, String> getParameters() {
    return this.parameters;
  }

  /**
   * @param value a map of additional parameters.
   * @return this
   */
  public DropTargetImpl setParameters(Map<String, String> value) {
    this.parameters = value;
    return this;
  }

  @Override
  public void write(JSONWriter out) throws JSONException {
    out.object();
    out.key("id").value(this.name);
    out.key("name").value(this.propertyName);
    out.key("accept").array();
    for (String a : this.accept) {
      out.value(a);
    }
    out.endArray();
    out.key("groups").array();
    for (String group : this.groups) {
      out.value(group);
    }
    out.endArray();
    if (!this.parameters.isEmpty()) {
      out.key("params");
      out.object();
      for (Map.Entry<String, String> e : this.parameters.entrySet()) {
        out.key(e.getKey()).value(e.getValue());
      }
      out.endObject();
    }
    out.endObject();
  }

}
