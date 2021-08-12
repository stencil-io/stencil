package io.thestencil.persistence.spi.builders;

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

import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.thestencil.persistence.api.DeleteBuilder;
import io.thestencil.persistence.api.ImmutableEntity;
import io.thestencil.persistence.api.ImmutableLink;
import io.thestencil.persistence.api.ImmutableWorkflow;
import io.thestencil.persistence.api.ZoePersistence.Article;
import io.thestencil.persistence.api.ZoePersistence.Entity;
import io.thestencil.persistence.api.ZoePersistence.EntityType;
import io.thestencil.persistence.api.ZoePersistence.Link;
import io.thestencil.persistence.api.ZoePersistence.Locale;
import io.thestencil.persistence.api.ZoePersistence.Page;
import io.thestencil.persistence.api.ZoePersistence.Workflow;
import io.thestencil.persistence.spi.PersistenceCommands;
import io.thestencil.persistence.spi.PersistenceConfig;
import io.thestencil.persistence.spi.PersistenceConfig.EntityState;

public class DeleteBuilderImpl extends PersistenceCommands implements DeleteBuilder {
  
  public DeleteBuilderImpl(PersistenceConfig config) {
    super(config);
  }

  @Override
  public Uni<Entity<Article>> article(String articleId) {
    // Delete the article
    return new ArticleDeleteVisitor(config, articleId).visit();
  
  }
  @Override
  public Uni<Entity<Locale>> locale(String localeId) {
    // Get the locale
    final Uni<EntityState<Locale>> query = get(localeId, EntityType.LOCALE);
    
    // Delete the locale
    return query.onItem().transformToUni(state -> delete(state.getEntity()));
  }

  @Override
  public Uni<Entity<Page>> page(String pageId) {
    // Get the page
    final Uni<EntityState<Page>> query = get(pageId, EntityType.PAGE);
    
    // Delete the page
    return query.onItem().transformToUni(state -> delete(state.getEntity()));
  }

  @Override
  public Uni<Entity<Link>> link(String linkId) {
    // Get the link
    final Uni<EntityState<Link>> query = get(linkId, EntityType.LINK);
    
    // Delete the link
    return query.onItem().transformToUni(state -> delete(state.getEntity()));
  }
  
  @Override
  public Uni<Entity<Workflow>> workflow(String workflowId) {
    // Get the workflow
    final Uni<EntityState<Workflow>> query = get(workflowId, EntityType.WORKFLOW);
    
    // Delete the workflow
    return query.onItem().transformToUni(state -> delete(state.getEntity()));
  }

  @Override
  public Uni<Entity<Link>> linkArticlePage(LinkArticlePage linkArticlePage) {
    
    // Get the link
    final Uni<EntityState<Link>> query = get(linkArticlePage.getLinkId(), EntityType.LINK);
    
    return query.onItem().transformToUni(state -> {
      final Entity<Link> start = state.getEntity();
      
      var newArticles = start.getBody()
          .getArticles().stream().filter(a -> !a.equals(linkArticlePage.getArticleId()))
          .collect(Collectors.toList());
      
      if(newArticles.size() == start.getBody().getArticles().size()) {
        return Uni.createFrom().item(start);
      }
      
      Entity<Link> end = ImmutableEntity.<Link>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableLink.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build();
      
      return save(end);
    });
  }

  @Override
  public Uni<Entity<Workflow>> workflowArticlePage(WorkflowArticlePage workflowArticlePage) {
    // Get the workflow
    final Uni<EntityState<Workflow>> query = get(workflowArticlePage.getWorkflowId(), EntityType.WORKFLOW);
    
    return query.onItem().transformToUni(state -> {
      final Entity<Workflow> start = state.getEntity();
      
      var newArticles = start.getBody()
          .getArticles().stream().filter(a -> !a.equals(workflowArticlePage.getArticleId()))
          .collect(Collectors.toList());
      
      if(newArticles.size() == start.getBody().getArticles().size()) {
        return Uni.createFrom().item(start);
      }
      
      Entity<Workflow> end = ImmutableEntity.<Workflow>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableWorkflow.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build();
      return save(end);
    });
  }
}
