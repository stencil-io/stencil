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

import java.util.ArrayList;
import java.util.List;

import io.thestencil.staticontent.api.SiteContent.Topic;
import io.thestencil.staticontent.api.SiteContent.TopicHeading;

public class MutableTopic implements Topic {
  private String id;
  private String name;
  private List<String> links;
  private String parent;
  private String blob;
  private List<TopicHeading> headings = new ArrayList<>();
  public MutableTopic() {
  }
  public MutableTopic(String id, String name, List<String> links, String parent, String blob, List<TopicHeading> headings) {
    super();
    this.id = id;
    this.name = name;
    this.links = links;
    this.parent = parent;
    this.blob = blob;
    this.headings = headings;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public List<String> getLinks() {
    return links;
  }
  public String getParent() {
    return parent;
  }
  public String getBlob() {
    return blob;
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setLinks(List<String> links) {
    this.links = links;
  }
  public void setParent(String parent) {
    this.parent = parent;
  }
  public void setBlob(String blob) {
    this.blob = blob;
  }
  public List<TopicHeading> getHeadings() {
    return headings;
  }
  @Override
  public String toString() {
    return "ImmutableTopic [id=" + id + ", name=" + name + ", links=" + links + ", parent=" + parent + ", blob=" + blob +
        ", headings=" + headings
        + "]";
  }
}
