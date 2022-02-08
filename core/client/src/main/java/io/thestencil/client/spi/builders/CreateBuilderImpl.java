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

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.CreateBuilder;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableLocale;
import io.thestencil.client.api.ImmutableLocaleLabel;
import io.thestencil.client.api.ImmutablePage;
import io.thestencil.client.api.ImmutableRelease;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.ImmutableTemplate;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.SiteContentType;
import io.thestencil.client.api.StencilClient.SiteState;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.spi.PersistenceConfig;
import io.thestencil.client.spi.exceptions.ConstraintException;
import io.thestencil.client.spi.exceptions.RefException;
import io.thestencil.client.spi.exceptions.RepoException;
import io.thestencil.client.spi.exceptions.SaveException;


public class CreateBuilderImpl implements CreateBuilder {
  
  private final PersistenceConfig config;
  
  public CreateBuilderImpl(PersistenceConfig config) {
    super();
    this.config = config;
  }
  
  @Override
  public Uni<Entity<Article>> article(CreateArticle init) {
    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
    
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
      
      return config.getClient().commit().head()
        .head(config.getRepoName(), config.getHeadName())
        .message("creating-article")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor())
        .append(gid, config.getSerializer().toString(entity))
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitStatus.OK) {
            return entity;
          }
          throw new SaveException(entity, commit);
        });
    });
  }
  
  @Override
  public Uni<Entity<Template>> template(CreateTemplate init) {
    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
    
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


      return config.getClient().commit().head()
        .head(config.getRepoName(), config.getHeadName())
        .message("creating-template")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor())
        .append(gid, config.getSerializer().toString(entity))
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitStatus.OK) {
            return entity;
          }
          throw new SaveException(entity, commit);
        });
    });
  }

  @Override
  public Uni<Entity<Release>> release(CreateRelease init) {
  
    
    return config.getClient().objects().refState()
      .repo(config.getRepoName())
      .ref(config.getHeadName())
      .blobs(true)
      .build().onItem().transformToUni(state -> {
        if(state.getStatus() == ObjectsStatus.OK) {
          final var gid = gid(EntityType.RELEASE);
          
          final var release = new CreateReleaseVisitor(state, config)
              .visit(ImmutableRelease.builder()
                .name(init.getName())
                .created(LocalDateTime.now())
                .note(Optional.ofNullable(init.getNote()).orElse(""))
                .parentCommit(state.getObjects().getRef().getCommit())
              ).build();

          final Entity<Release> entity = ImmutableEntity.<Release>builder()
              .id(gid)
              .type(EntityType.RELEASE)
              .body(release)
              .build();
          return config.getClient().commit().head()
            .head(config.getRepoName(), config.getHeadName())
            .message("creating-release")
            .parentIsLatest()
            .author(config.getAuthorProvider().getAuthor())
            .append(gid, config.getSerializer().toString(entity))
            .build().onItem().transform(commit -> {
              if(commit.getStatus() == CommitStatus.OK) {
                return entity;
              }
              throw new SaveException(entity, commit);
            });      
        }
        
        throw new RefException("Can't create release because ref state query failed!", state);
      });
  }

  @Override
  public Uni<Entity<Locale>> locale(CreateLocale init) {
    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
    
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
      
      
      return config.getClient().commit().head()
          .head(config.getRepoName(), config.getHeadName())
          .message("creating-locale")
          .parentIsLatest()
          .author(config.getAuthorProvider().getAuthor())
          .append(gid, config.getSerializer().toString(entity))
          .build().onItem().transform(commit -> {
            if(commit.getStatus() == CommitStatus.OK) {
              return entity;
            }
            throw new SaveException(entity, commit);
          });
      
    });
  }

  @Override
  public Uni<Entity<Page>> page(CreatePage init) {

    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
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
      
      return config.getClient().commit().head()
          .head(config.getRepoName(), config.getHeadName())
          .message("creating-page")
          .parentIsLatest()
          .author(config.getAuthorProvider().getAuthor())
          .append(gid, config.getSerializer().toString(entity))
          .build().onItem().transform(commit -> {
            if(commit.getStatus() == CommitStatus.OK) {
              return entity;
            }
            throw new SaveException(entity, commit);
          });
    });
  }

  @Override
  public Uni<Entity<Link>> link(CreateLink init) {
    
    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
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
      
      return config.getClient().commit().head()
        .head(config.getRepoName(), config.getHeadName())
        .message("creating-link")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor())
        .append(gid, config.getSerializer().toString(entity))
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitStatus.OK) {
            return entity;
          }
          throw new SaveException(entity, commit);
        });
      
    });
  }

  @Override
  public Uni<Entity<Workflow>> workflow(CreateWorkflow init) {
    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
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
      
      return config.getClient().commit().head()
          .head(config.getRepoName(), config.getHeadName())
          .message("creating-workflow")
          .parentIsLatest()
          .author(config.getAuthorProvider().getAuthor())
          .append(gid, config.getSerializer().toString(entity))
          .build().onItem().transform(commit -> {
            if(commit.getStatus() == CommitStatus.OK) {
              return entity;
            }
            throw new SaveException(entity, commit);
          });
        
      });
  }
  
  private String gid(EntityType type) {
    return config.getGidProvider().getNextId(type);
  }

  @Override
  public Uni<SiteState> repo() {
    return config.getClient().repo().create()
        .name(config.getRepoName())
        .build().onItem().transform(repoResult -> {
          if(repoResult.getStatus() == RepoStatus.OK) {
            return ImmutableSiteState.builder().contentType(SiteContentType.OK).name(repoResult.getRepo().getName()).build();
          }
          throw new RepoException("Can't create repository with name: '"  + config.getRepoName() + "'!", repoResult);
        });
  }
}
