package io.thestencil.staticontent.spi.visitor;

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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.SiteState;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.staticontent.api.ImmutableLinkResource;
import io.thestencil.staticontent.api.ImmutableMarkdown;
import io.thestencil.staticontent.api.ImmutableMarkdowns;
import io.thestencil.staticontent.api.StaticContentClient.LinkResource;
import io.thestencil.staticontent.api.StaticContentClient.Markdown;
import io.thestencil.staticontent.api.StaticContentClient.Markdowns;

public class SiteStateVisitor {
  private static final Logger LOGGER = LoggerFactory.getLogger(SiteStateVisitor.class);
  public static String LINK_TYPE_WORKFLOW = "workflow";
  private final List<Entity<Locale>> locales = new ArrayList<>();
  private SiteState entity;
  
  public Markdowns visit(SiteState entity) {
    this.entity = entity;
    final var result = ImmutableMarkdowns.builder()
        .addAllLocales(visitLocales(entity).stream().map(e -> e.getBody().getValue()).collect(Collectors.toList()));
    
    for(final var article : entity.getArticles().values()) {
      result.addAllValues(visitArticle(article));
    }
    
    for(final var link : entity.getLinks().values()) {
      result.addAllLinks(visitLinks(link));
    }
    for(final var link : entity.getWorkflows().values()) {
      result.addAllLinks(visitWorkflows(link));
    }
    
    return result.build();
  }

  private List<LinkResource> visitWorkflows(Entity<Workflow> link) {
    final List<LinkResource> result = new ArrayList<>();
    final var locale = locales.stream().filter(l -> link.getBody().getLocale().equals(l.getId())).findFirst();
    if(locale.isEmpty()) {
      return result;
    }    
    
    for(final var articleId : link.getBody().getArticles()) {
      final var article = entity.getArticles().get(articleId);
      final var resource = ImmutableLinkResource.builder()
          .id(link.getId() + "-" + locale.get().getBody().getValue())
          .addLocale(locale.get().getBody().getValue())
          .desc(link.getBody().getName())
          .path(visitArticlePath(article))
          .value(link.getBody().getContent())
          .workflow(true).global(false)
          .type(LINK_TYPE_WORKFLOW)
          .build();
      result.add(resource);
    }

    if(link.getBody().getArticles().isEmpty()) {
      for(Entity<Article> article : entity.getArticles().values()) {
        final var resource = ImmutableLinkResource.builder()
            .id(link.getId() + "-" + locale.get().getBody().getValue())
            .addLocale(locale.get().getBody().getValue())
            .desc(link.getBody().getName())
            .path(visitArticlePath(article))
            .value(link.getBody().getContent())
            .workflow(true).global(true)
            .type(LINK_TYPE_WORKFLOW)
            .build();
        result.add(resource);
      }
    }
    
    return result;
  }
  
  private List<LinkResource> visitLinks(Entity<Link> link) {
    final List<LinkResource> result = new ArrayList<>();
    final var locale = locales.stream().filter(l -> link.getBody().getLocale().equals(l.getId())).findFirst();
    if(locale.isEmpty()) {
      return result;
    }
    
    for(final var articleId : link.getBody().getArticles()) {
      final var article = entity.getArticles().get(articleId);
      final var resource = ImmutableLinkResource.builder()
          .id(link.getId() + "-" + locale.get().getBody().getValue())
          .addLocale(locale.get().getBody().getValue())
          .desc(link.getBody().getDescription())
          .path(visitArticlePath(article))
          .value(link.getBody().getContent())
          .workflow(false).global(false)
          .type(link.getBody().getContentType())
          .build();
      result.add(resource);
    }
    
    if(link.getBody().getArticles().isEmpty()) {
      for(Entity<Article> article : entity.getArticles().values()) {
        final var resource = ImmutableLinkResource.builder()
            .id(link.getId() + "-" + locale.get().getBody().getValue())
            .addLocale(locale.get().getBody().getValue())
            .desc(link.getBody().getDescription())
            .path(visitArticlePath(article))
            .value(link.getBody().getContent())
            .workflow(false).global(true)
            .type(link.getBody().getContentType())
            .build();
        result.add(resource);
      }
    }
    
    return result;
  }
  
  private List<Markdown> visitArticle(Entity<Article> article) {
    final String path = visitArticlePath(article);
    final List<Markdown> result = new ArrayList<>();
    for(final var page : entity.getPages().values()) {
      if(!page.getBody().getArticle().equals(article.getId())) {
        continue;
      }
      final var locale = locales.stream().filter(l -> page.getBody().getLocale().equals(l.getId())).findFirst();
      if(locale.isEmpty()) {
        continue;
      }
      
      final var content = page.getBody().getContent();
      final var ast = new MarkdownVisitor().visit(content);
      if(ast.getHeadings().stream().filter(entity -> entity.getLevel() == 1).findFirst().isEmpty()) {
        //throw new MarkdownException();
        LOGGER.error("Failed to parse article '" + article.getBody().getName() + "', markdown must have atleast one h1(line starting with one # my super menu)");
      }
      
      result.add(ImmutableMarkdown.builder()
          .path(path)
          .locale(locale.get().getBody().getValue())
          .value(content)
          .addAllHeadings(ast.getHeadings())
          .build());
    }
    
    return result;
  }
  
  private String visitArticlePath(Entity<Article> src) {

    final StringBuilder path = new StringBuilder();
    Entity<Article> article = src;
    do {
      if(path.length() > 0) {
        path.insert(0, "/");
      }
      path.insert(0, String.format("%03d", article.getBody().getOrder()) + "_" + article.getBody().getName());
      final var parentId = article.getBody().getParentId();
      article = parentId == null ? null : entity.getArticles().get(parentId);
    } while(article != null);

    return path.toString();
  }
  
  
  private List<Entity<Locale>> visitLocales(SiteState site) {
    this.locales.addAll(site.getLocales().values().stream()
        .filter(l -> l.getBody().getEnabled())
        .collect(Collectors.toList()));
    return locales;
  }
}
