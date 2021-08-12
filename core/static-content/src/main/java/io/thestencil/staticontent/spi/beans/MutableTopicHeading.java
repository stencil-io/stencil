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

import io.thestencil.staticontent.api.SiteContent.TopicHeading;

public class MutableTopicHeading implements TopicHeading {
  private String id;
  private String name;
  private Integer order;
  private Integer level;
  public MutableTopicHeading() {
  }
  public MutableTopicHeading(String id, String name, Integer order, Integer level) {
    super();
    this.id = id;
    this.name = name;
    this.order = order;
    this.level = level;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Integer getOrder() {
    return order;
  }
  public void setOrder(Integer order) {
    this.order = order;
  }
  public Integer getLevel() {
    return level;
  }
  public void setLevel(Integer level) {
    this.level = level;
  }
  @Override
  public String toString() {
    return "MutableTopicHeading [id=" + id + ", name=" + name + ", order=" + order + ", level=" + level + "]";
  }
}
