package io.thestencil.client.spi.builders;

import java.time.LocalDateTime;

/*-
 * #%L
 * stencil-persistence
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

import java.util.Optional;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.CreateBuilder;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableLocale;
import io.thestencil.client.api.ImmutableLocaleLabel;
import io.thestencil.client.api.ImmutablePage;
import io.thestencil.client.api.ImmutableRelease;
import io.thestencil.client.api.ImmutableTemplate;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.Article;
import io.thestencil.client.api.StencilComposer.Entity;
import io.thestencil.client.api.StencilComposer.EntityType;
import io.thestencil.client.api.StencilComposer.Link;
import io.thestencil.client.api.StencilComposer.Locale;
import io.thestencil.client.api.StencilComposer.Page;
import io.thestencil.client.api.StencilComposer.Release;
import io.thestencil.client.api.StencilComposer.SiteContentType;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilComposer.Template;
import io.thestencil.client.api.StencilComposer.Workflow;
import io.thestencil.client.spi.StencilAssert;
import io.thestencil.client.spi.exceptions.ConstraintException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateBuilderImpl implements CreateBuilder {
  
  private final StencilClient client;
  
  @Override
  public Uni<Entity<Article>> article(CreateArticle init) {
    final Uni<SiteState> query = client.getStore().query().head();
    return query.onItem().transformToUni(state -> {
    
      final var gid = gid(EntityType.ARTICLE);
      final var article = ImmutableArticle.builder()
          .name(init.getName())
          .parentId(init.getParentId())
          .order(Optional.ofNullable(init.getOrder()).orElse(0))
          .build();
      final Entity<Article> entity = ImmutableEntity.<Article>builder()
          .id(gid)
          .type(EntityType.ARTICLE)
          .body(article)
          .build();
      
      final var duplicate = state.getArticles().values().stream()
          .filter(p -> p.getBody().getName().equals(init.getName()))
          .findFirst();
      
      if(duplicate.isPresent()) {
        throw new ConstraintException(entity, "Article: '" + init.getName() + "' already exists!");
      }

      if(init.getParentId() != null && !state.getArticles().containsKey(init.getParentId())) {
        throw new ConstraintException(entity, "Article: '" + init.getName() + "', parent: '" + init.getParentId() + "' does not exist!");
      }
      
      return client.getStore().create(entity);
    });
  }
  
  @Override
  public Uni<Entity<Template>> template(CreateTemplate init) {
    final Uni<SiteState> query = client.getStore().query().head();
    return query.onItem().transformToUni(state -> {
    
      final var gid = gid(EntityType.TEMPLATE);
      final var template = ImmutableTemplate.builder()
          .name(init.getName())
          .description(init.getDescription())
          .type(init.getType())
          .content(init.getContent())
          .build();
      final Entity<Template> entity = ImmutableEntity.<Template>builder()
          .id(gid)
          .type(EntityType.TEMPLATE)
          .body(template)
          .build();
      
      final var duplicate = state.getTemplates().values().stream()
          .filter(p -> p.getBody().getName().equals(init.getName()))
          .findFirst();
      
      if(duplicate.isPresent()) {
        throw new ConstraintException(entity, "Template: '" + init.getName() + "' already exists!");
      }
      return client.getStore().create(entity);
    });
  }

  @Override
  public Uni<Entity<Release>> release(CreateRelease init) {
    return client.getStore().query().head().onItem().transformToUni(state -> {
        
      StencilAssert.isTrue(state.getContentType() != SiteContentType.NOT_CREATED, () -> "Can't create release because ref state query failed!");
    
      final var gid = gid(EntityType.RELEASE);
      
      final var release = new CreateReleaseVisitor(state)
          .visit(ImmutableRelease.builder()
            .name(init.getName())
            .created(LocalDateTime.now())
            .note(Optional.ofNullable(init.getNote()).orElse(""))
            .parentCommit(state.getCommit())
          ).build();

      final Entity<Release> entity = ImmutableEntity.<Release>builder()
          .id(gid)
          .type(EntityType.RELEASE)
          .body(release)
          .build();
      
      return client.getStore().create(entity);
    });
  }

  @Override
  public Uni<Entity<Locale>> locale(CreateLocale init) {
    final Uni<SiteState> query = client.getStore().query().head();
    return query.onItem().transformToUni(state -> {
      
      final var gid = gid(EntityType.LOCALE);
      final var locale = ImmutableLocale.builder()
          .value(init.getLocale())
          .enabled(true)
          .build();
      
      final Entity<Locale> entity = ImmutableEntity.<Locale>builder()
          .id(gid)
          .type(EntityType.LOCALE)
          .body(locale)
          .build();
      
      final var duplicate = state.getLocales().values().stream()
          .filter(p -> p.getBody().getValue().equals(init.getLocale()))
          .findFirst();
      
      if(duplicate.isPresent()) {
        throw new ConstraintException(entity, "Locale: '" + init.getLocale() + "' already exists!");
      }
      
      return client.getStore().create(entity);
      
    });
  }

  @Override
  public Uni<Entity<Page>> page(CreatePage init) {
    final Uni<SiteState> query = client.getStore().query().head();
    return query.onItem().transformToUni(state -> {
      final var localeId = init.getLocale();
      final var gid = gid(EntityType.PAGE);
      final var page = ImmutablePage.builder()
          .article(init.getArticleId())
          .locale(localeId)
          .content(Optional.ofNullable(init.getContent()).orElse(""))
          .build();
      
      final Entity<Page> entity = ImmutableEntity.<Page>builder()
          .id(gid)
          .type(EntityType.PAGE)
          .body(page)
          .build();
      

      if(!state.getLocales().containsKey(localeId)) {
        throw new ConstraintException(entity, "Locale with id: '" + localeId + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
      }
      
      final var duplicate = state.getPages().values().stream()
          .filter(p -> p.getBody().getArticle().equals(init.getArticleId()))
          .filter(p -> p.getBody().getLocale().equals(init.getLocale()))
          .findFirst();
      
      if(duplicate.isPresent()) {
        throw new ConstraintException(entity, "Page locale with id: '" + localeId + "' already exists!");
      }
      
      return client.getStore().create(entity);
    });
  }

  @Override
  public Uni<Entity<Link>> link(CreateLink init) {
    final Uni<SiteState> query = client.getStore().query().head();
    return query.onItem().transformToUni(state -> {
      final var gid = gid(EntityType.LINK);
      final var link = ImmutableLink.builder()
        .contentType(init.getType())
        .value(init.getValue())
        .articles(init.getArticles());
      
      for(final var label : init.getLabels()) {
        link.addLabels(ImmutableLocaleLabel.builder()
            .locale(label.getLocale())
            .labelValue(label.getLabelValue())
            .build());
        
        if(!state.getLocales().containsKey(label.getLocale())) {
          throw new ConstraintException(
              ImmutableEntity.<Link>builder().id(gid).type(EntityType.LINK).body(link.build()).build(), 
              "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
        }
      }
      
      final var entity = ImmutableEntity.<Link>builder().id(gid).type(EntityType.LINK).body(link.build()).build();
      
      return client.getStore().create(entity);
    });
  }

  @Override
  public Uni<Entity<Workflow>> workflow(CreateWorkflow init) {
    final Uni<SiteState> query = client.getStore().query().head();
    return query.onItem().transformToUni(state -> {
      
      final var gid = gid(EntityType.WORKFLOW);
      final var workflow = ImmutableWorkflow.builder().devMode(init.getDevMode()).value(init.getValue()).articles(init.getArticles());
            
      for(final var label : init.getLabels()) {        
        workflow.addLabels(ImmutableLocaleLabel.builder()
            .locale(label.getLocale())
            .labelValue(label.getLabelValue())
            .build());

        if(!state.getLocales().containsKey(label.getLocale())) {
          throw new ConstraintException(
              ImmutableEntity.<Workflow>builder().id(gid).type(EntityType.WORKFLOW).body(workflow.build()).build(), 
              "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
        }
      }

      final var entity = ImmutableEntity.<Workflow>builder().id(gid).type(EntityType.WORKFLOW).body(workflow.build()).build();
      
      return client.getStore().create(entity);
        
      });
  }
  
  private String gid(EntityType type) {
    return client.getStore().gid(type);
  }
  @Override
  public Uni<SiteState> repo() {
    return client.getStore().repo().create().onItem().transformToUni(e -> e.query().head());
  }
}
