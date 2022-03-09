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

import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilClient.SiteState;
import io.thestencil.client.api.beans.SitesBean;
import io.thestencil.staticontent.api.ImmutableImageResource;
import io.thestencil.staticontent.api.ImmutableMarkdown;
import io.thestencil.staticontent.api.ImmutableMarkdowns;
import io.thestencil.staticontent.api.StaticContentClient;
import io.thestencil.staticontent.spi.support.ParserAssert;
import io.thestencil.staticontent.spi.visitor.CSVLinksVisitor;
import io.thestencil.staticontent.spi.visitor.ImmutableLinkData;
import io.thestencil.staticontent.spi.visitor.ImmutableTopicData;
import io.thestencil.staticontent.spi.visitor.MarkdownException;
import io.thestencil.staticontent.spi.visitor.MarkdownVisitor;
import io.thestencil.staticontent.spi.visitor.SiteStateVisitor;
import io.thestencil.staticontent.spi.visitor.SiteVisitor;
import io.thestencil.staticontent.spi.visitor.SiteVisitor.LinkData;
import io.thestencil.staticontent.spi.visitor.SiteVisitor.TopicData;
import io.thestencil.staticontent.spi.visitor.SiteVisitorDefault;

public class StaticContentClientDefault implements StaticContentClient {

  private final ObjectMapper objectMapper;
  public StaticContentClientDefault(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }
  
  @Override
  public MarkdownBuilder markdown() {
    return new MarkdownBuilder() {
      private Markdowns jsonOfSiteState;
      private ImmutableMarkdowns.Builder fromFiles;
      private boolean dev;
      
      @Override
      public MarkdownBuilder md(String path, byte[] value) {
        if(fromFiles == null) {
          fromFiles = ImmutableMarkdowns.builder();
        }
        if (!path.toLowerCase().endsWith(".md")) {
          final var cleanName = path.toLowerCase();
          if(cleanName.equals("links.csv")) {
            fromFiles.addAllLinks(new CSVLinksVisitor(path).visit(value));
          } else if(cleanName.startsWith("images/")) {
            fromFiles.addImages(ImmutableImageResource.builder().path(path).value(value).build());
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
          
          fromFiles.addValues(ImmutableMarkdown.builder()
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
      public MarkdownBuilder json(String jsonOfSiteState) {
        try {
          final var site = objectMapper.readValue(jsonOfSiteState, SiteState.class);
          this.jsonOfSiteState = new SiteStateVisitor(dev).visit(site);
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        return this;
      }
      
      @Override
      public MarkdownBuilder json(SiteState jsonOfSiteState) {
        this.jsonOfSiteState = new SiteStateVisitor(dev).visit(jsonOfSiteState);
        return this;
      }
      
      @Override
      public Markdowns build() {
        ParserAssert.isTrue(jsonOfSiteState != null || fromFiles != null, () -> "json or md files must be provided!");
        ParserAssert.isTrue(jsonOfSiteState == null || fromFiles == null, () -> "json or md files both can't be provided!");
        
        if(fromFiles != null) {
          return fromFiles.build();
        }
        return this.jsonOfSiteState;
      }

      @Override
      public MarkdownBuilder dev() {
        this.dev = true;
        return this;
      }
    };
  }
  @Override
  public SitesBuilder sites() {
    return new SitesBuilder() {
      private final SiteVisitor visitor = new SiteVisitorDefault(obj -> writeAsString(obj, objectMapper));
      private String imageUrl;
      private Long created;
      private Markdowns markdowns;
      @Override
      public SitesBuilder source(Markdowns markdowns) {
        this.markdowns = markdowns;
        return this;
      }
      @Override
      public SitesBuilder imagePath(String imagePath) {
        this.imageUrl = imagePath;
        return this;
      }
      @Override
      public SitesBuilder created(long created) {
        this.created = created;
        return this;
      }
      private SitesBuilder topic(
          Function<ImmutableTopicData.Builder, TopicData> newTopic) {
        visitor.visitTopicData(newTopic.apply(ImmutableTopicData.builder()));
        return this;
      }
      private SitesBuilder link(
          Function<ImmutableLinkData.Builder, LinkData> newLink) {
        visitor.visitLinkData(newLink.apply(ImmutableLinkData.builder()));
        return this;
      }
      @Override
      public Sites build() {
        ParserAssert.notEmpty(imageUrl, () -> "imageUrl can't be empty!");
        ParserAssert.notNull(created, () -> "created can't be empty!");
        ParserAssert.notNull(markdowns, () -> "markdowns can't be empty!");

        markdowns.getValues()
        .forEach(value -> topic(builder -> builder
        .path(value.getPath())
        .locale(value.getLocale())
        .headings(value.getHeadings())
        .images(value.getImages())
        .value(value.getValue())
        .build()));
      
        markdowns.getLinks()
        .forEach(link -> link.getLocale().forEach(locale -> link(builder -> builder
          .id(link.getId())
          .path(link.getPath())
          .locale(locale)
          .type(link.getType())
          .name(link.getDesc())
          .global(link.getGlobal())
          .value(link.getValue())
          .workflow(link.getType().equals(SiteStateVisitor.LINK_TYPE_WORKFLOW))
          .build()
        )));
        
        final var visited = visitor.visit(imageUrl);
        final var content = visited.getSites().stream().collect(
          Collectors.toMap(e -> e.getLocale(), e -> e)
        );
        return SitesBean.builder()
            .created(created)
            .sites(content)
            .build();
      }
    };
  }
  
  
  private static ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
    return objectMapper;
  } 
  
  private static String writeAsString(Object anyObject, ObjectMapper objectMapper) {
    try {
      return objectMapper.writeValueAsString(anyObject);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public StaticContentClientDefault build() {
      return new StaticContentClientDefault(objectMapper());
    }
    public StaticContentClientDefault build(ObjectMapper objectMapper) {
      return new StaticContentClientDefault(objectMapper);
    }
  }

}
