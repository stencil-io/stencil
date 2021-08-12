package io.thestencil.staticontent.api;

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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.immutables.value.Value;

import io.thestencil.persistence.api.ZoePersistence.SiteState;
import io.thestencil.staticontent.api.MarkdownContent.Heading;
import io.thestencil.staticontent.api.MarkdownContent.ImageTag;

public interface StaticContentClient {
  StaticContentBuilder create();
  StaticContentBuilder from(SiteState site);
  StaticContentBuilder from(MarkdownContent site);
  
  
  SiteState parseSiteState(String json);
  MarkdownContent parseMd(SiteState site);
  FileParser parseFiles();
  
  interface FileParser {
    FileParser add(String path, byte[] value);
    MarkdownContent build();
  }
  
  interface StaticContentBuilder {
    StaticContentBuilder topic(Function<ImmutableTopicData.Builder, TopicData> newTopic);
    StaticContentBuilder topicName(Function<ImmutableTopicNameData.Builder, TopicNameData> newTopicNames);
    StaticContentBuilder link(Function<ImmutableLinkData.Builder, LinkData> newLink);
    StaticContentBuilder image(Function<ImmutableImageData.Builder, ImageData> newImage);
    StaticContentBuilder imageUrl(String imageUrl);
    StaticContentBuilder created(long created);
    SiteContent build();
  }
  
  @Value.Immutable
  interface TopicData {
    String getPath();
    String getLocale();
    String getValue();
    List<Heading> getHeadings();
    List<ImageTag> getImages();
  }

  @Value.Immutable
  interface TopicNameData {
    String getPath();
    Map<String, String> getLocale();
  }

  
  @Value.Immutable
  interface LinkData {
    String getId();
    String getPath();
    String getName();
    String getType();
    String getValue();
    String getLocale();
    Boolean getWorkflow();
  }
  
  @Value.Immutable
  interface ImageData {
    String getPath();
    byte[] getValue();
  }
}
