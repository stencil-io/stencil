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

import java.util.Map;

import io.thestencil.staticontent.api.SiteContent.Blob;
import io.thestencil.staticontent.api.SiteContent.Site;
import io.thestencil.staticontent.api.SiteContent.Topic;
import io.thestencil.staticontent.api.SiteContent.TopicLink;

public class MutableSite implements Site {
  private String id;
  private String images;
  private String locale;
  private Map<String, Topic> topics;
  private Map<String, Blob> blobs;
  private Map<String, TopicLink> links;
  
  public MutableSite() {
  }
  
  public MutableSite(
      String id, String images, String locale, 
      Map<String, Topic> topics, 
      Map<String, Blob> blobs,
      Map<String, TopicLink> links) {
    super();
    this.id = id;
    this.images = images;
    this.locale = locale;
    this.topics = topics;
    this.blobs = blobs;
    this.links = links;
  }

  public String getId() {
    return id;
  }
  public String getImages() {
    return images;
  }
  public String getLocale() {
    return locale;
  }
  public Map<String, Topic> getTopics() {
    return topics;
  }
  public Map<String, Blob> getBlobs() {
    return blobs;
  }
  public Map<String, TopicLink> getLinks() {
    return links;
  }
  @Override
  public String toString() {
    return "ImmutableSite [id=" + id + ", images=" + images + ", locale=" + locale + ", topics=" + topics + ", blobs="
        + blobs + ", links=" + links + "]";
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setImages(String images) {
    this.images = images;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setTopics(Map<String, Topic> topics) {
    this.topics = topics;
  }

  public void setBlobs(Map<String, Blob> blobs) {
    this.blobs = blobs;
  }

  public void setLinks(Map<String, TopicLink> links) {
    this.links = links;
  }
}
