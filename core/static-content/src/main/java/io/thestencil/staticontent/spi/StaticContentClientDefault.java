package io.thestencil.staticontent.spi;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.thestencil.persistence.api.ZoePersistence.SiteState;
import io.thestencil.staticontent.api.ImmutableImageData;
import io.thestencil.staticontent.api.ImmutableImageResource;
import io.thestencil.staticontent.api.ImmutableLinkData;
import io.thestencil.staticontent.api.ImmutableMarkdown;
import io.thestencil.staticontent.api.ImmutableMarkdownContent;
import io.thestencil.staticontent.api.ImmutableTopicData;
import io.thestencil.staticontent.api.ImmutableTopicNameData;
import io.thestencil.staticontent.api.MarkdownContent;
import io.thestencil.staticontent.api.SiteContent;
import io.thestencil.staticontent.api.StaticContentClient;
import io.thestencil.staticontent.spi.beans.MutableStaticContent;
import io.thestencil.staticontent.spi.support.ParserAssert;
import io.thestencil.staticontent.spi.visitor.CSVLinksVisitor;
import io.thestencil.staticontent.spi.visitor.MarkdownException;
import io.thestencil.staticontent.spi.visitor.MarkdownVisitor;
import io.thestencil.staticontent.spi.visitor.SiteStateVisitor;
import io.thestencil.staticontent.spi.visitor.SiteVisitor;
import io.thestencil.staticontent.spi.visitor.SiteVisitorDefault;

public class StaticContentClientDefault implements StaticContentClient {

  private static ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
  }
  
  
  @Override
  public StaticContentBuilder create() {
    
    return new StaticContentBuilder() {
      private final SiteVisitor visitor = new SiteVisitorDefault((src) -> {
        try {
          return objectMapper.writeValueAsString(src);
        } catch(IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      });
      private String imageUrl;
      private Long created;
      
      @Override
      public StaticContentBuilder topic(
          Function<ImmutableTopicData.Builder, TopicData> newTopic) {
        visitor.visitTopicData(newTopic.apply(ImmutableTopicData.builder()));
        return this;
      }
      @Override
      public StaticContentBuilder link(
          Function<ImmutableLinkData.Builder, LinkData> newLink) {
        visitor.visitLinkData(newLink.apply(ImmutableLinkData.builder()));
        return this;
      }
      @Override
      public StaticContentBuilder image(
          Function<ImmutableImageData.Builder, ImageData> newImage) {
        visitor.visitImageData(newImage.apply(ImmutableImageData.builder()));
        return this;
      }
      @Override
      public StaticContentBuilder topicName(
          Function<ImmutableTopicNameData.Builder, TopicNameData> newTopicNames) {
        visitor.visitTopicNameData(newTopicNames.apply(ImmutableTopicNameData.builder()));
        return this;
      }
      @Override
      public StaticContentBuilder imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
      }
      @Override
      public StaticContentBuilder created(long created) {
        this.created = created;
        return this;
      }
      
      @Override
      public SiteContent build() {
        ParserAssert.notEmpty(imageUrl, () -> "imageUrl can't be empty!");
        ParserAssert.notNull(created, () -> "created can't be empty!");
      
        final var visited = visitor.visit(imageUrl);
        final var content = visited.getSites().stream().collect(
            Collectors.toMap(e -> e.getLocale(), e -> e)
        );
        return new MutableStaticContent(created, content);
      }
      
    };
  }

  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public StaticContentClientDefault build() {
      return new StaticContentClientDefault();
    }
  }

  @Override
  public StaticContentBuilder from(SiteState site) {
    final var content = new SiteStateVisitor().visit(site);
    return from(content);
  }
  
  @Override
  public StaticContentBuilder from(MarkdownContent content) {
    final var client = StaticContentClientDefault.builder().build().create();
    content.getValues()
    .forEach(value -> client.topic(builder -> builder
        .path(value.getPath())
        .locale(value.getLocale())
        .headings(value.getHeadings())
        .images(value.getImages())
        .value(value.getValue())
        .build()));
  
    content.getLinks()
    .forEach(link -> link.getLocale().forEach(locale -> client.link(builder -> builder
          .id(link.getId())
          .path(link.getPath())
          .locale(locale)
          .type(link.getType())
          .name(link.getDesc())
          .value(link.getValue())
          .workflow(link.getType().equals(SiteStateVisitor.LINK_TYPE_WORKFLOW))
          .build()
        )));
    return client;
  }


  @Override
  public SiteState parseSiteState(String json) {
    try {
      return objectMapper.readValue(json, SiteState.class);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  
  }
  @Override
  public MarkdownContent parseMd(SiteState site) {
    final var content = new SiteStateVisitor().visit(site);
    return content;
  }

  @Override
  public FileParser parseFiles() {
    return new FileParser() {
      private final ImmutableMarkdownContent.Builder result = ImmutableMarkdownContent.builder();
      @Override
      public FileParser add(String path, byte[] value) {
        if (!path.toLowerCase().endsWith(".md")) {
          final var cleanName = path.toLowerCase();
          if(cleanName.equals("links.csv")) {
            result.addAllLinks(new CSVLinksVisitor(path).visit(value));
          } else if(cleanName.startsWith("images/")) {
            result.addImages(ImmutableImageResource.builder().path(path).value(value).build());
          }
          return this;
        }

        final var fragments = path.split("\\/");
        if (!(fragments.length == 2 || fragments.length == 3)) {
          throw new MarkdownException("Markdown: '" + path + "' must have [2..3] (level2/level2/en.md) levels but was: '"
              + fragments.length + "'!");
        }
        final var fileName = fragments[fragments.length - 1];
        if (fileName.length() != 5) {
          throw new MarkdownException(
              "Markdown: '" + path + "' must be name as <path>/<locale>.md but was: '" + path + "'!");
        }
        final var locale = fileName.substring(0, 2);
        
        try {
          final var content = new String(value, StandardCharsets.UTF_8);
          final String cleanPath;
          if (fragments.length == 2) {
            cleanPath = fragments[0];
          } else {
            cleanPath = fragments[0] + "/" + fragments[1];
          }

          final var ast = new MarkdownVisitor().visit(content);
          if(ast.getHeadings().stream().filter(entity -> entity.getLevel() == 1).findFirst().isEmpty()) {
            throw new MarkdownException("markdown must have atleast one h1(line starting with one # my super menu)");
          }
          
          result.addValues(ImmutableMarkdown.builder()
              .path(cleanPath)
              .locale(locale)
              .value(content)
              .addAllHeadings(ast.getHeadings())
              .addAllImages(ast.getImages())
              .build());
          
          return this;
        } catch (Exception e) {
          throw new MarkdownException("Failed to parse markdown: '" + path + "', error: " + e.getMessage(), e);
        }
      }

      @Override
      public MarkdownContent build() {
        return result.build();
      }
    };
  }
}
