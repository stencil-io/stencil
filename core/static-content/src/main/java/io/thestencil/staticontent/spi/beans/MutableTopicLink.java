package io.thestencil.staticontent.spi.beans;

/*-
 * #%L
 * stencil-static-content
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.thestencil.staticontent.api.SiteContent.TopicLink;

public class MutableTopicLink implements TopicLink {
  private String id;
  private String type;
  private String name;
  private String value;
  private Boolean secured;
  public MutableTopicLink() {
  }
  public MutableTopicLink(String id, String type, String name, String value, Boolean secured) {
    super();
    this.id = id;
    this.type = type;
    this.name = name;
    this.value = value;
    this.secured = secured;
  }
  public String getId() {
    return id;
  }
  public String getType() {
    return type;
  }
  public String getName() {
    return name;
  }
  public String getValue() {
    return value;
  }
  public Boolean getSecured() {
    return secured;
  }
  @Override
  public String toString() {
    return "TopicLink [id=" + id + ", type=" + type + ", name=" + name + ", value=" + value + ", secured="
        + secured + "]";
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setType(String type) {
    this.type = type;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public void setSecured(Boolean secured) {
    this.secured = secured;
  }
}
