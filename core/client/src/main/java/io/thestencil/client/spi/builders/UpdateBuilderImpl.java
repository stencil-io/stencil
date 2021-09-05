package io.thestencil.client.spi.builders;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.spi.PersistenceCommands;
import io.thestencil.client.spi.PersistenceConfig;
import io.thestencil.client.spi.PersistenceConfig.EntityState;
import io.thestencil.client.spi.exceptions.QueryException;
import io.thestencil.client.spi.exceptions.SaveException;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableLocale;
import io.thestencil.client.api.ImmutablePage;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.UpdateBuilder;

public class UpdateBuilderImpl extends PersistenceCommands implements UpdateBuilder {

  public UpdateBuilderImpl(PersistenceConfig config) {
    super(config);
  }

  @Override
  public Uni<Entity<Article>> article(ArticleMutator changes) {
    // Get the article
    final Uni<EntityState<Article>> query = get(changes.getArticleId(), EntityType.ARTICLE);
    
    // Change the article
    return query.onItem().transformToUni(state -> save(changeArticle(state, changes)));
  }
  
  private Entity<Article> changeArticle(EntityState<Article> state, ArticleMutator changes) {
    final var start = state.getEntity();
    return ImmutableEntity.<Article>builder()
        .from(start)
        .body(ImmutableArticle.builder().from(start.getBody())
            .name(changes.getName())
            .order(changes.getOrder())
            .parentId(changes.getParentId())
            .build())
        .build();
  }
  
  @Override
  public Uni<Entity<Locale>> locale(LocaleMutator changes) {
    // Get the locale
    final Uni<EntityState<Locale>> query = get(changes.getLocaleId(), EntityType.LOCALE);
    
    // Change the locale
    return query.onItem().transformToUni(state -> save(changeLocale(state, changes)));
  }
  
  private Entity<Locale> changeLocale(EntityState<Locale> state, LocaleMutator changes) {
    final var start = state.getEntity();
    return ImmutableEntity.<Locale>builder()
        .from(start)
        .body(ImmutableLocale.builder().from(start.getBody())
            .value(changes.getValue())
            .enabled(changes.getEnabled())
            .build())
        .build();
  }

  @Override
  public Uni<Entity<Page>> page(PageMutator changes) {
    // Get the page
    final Uni<EntityState<Page>> query = get(changes.getPageId(), EntityType.PAGE);
    
    // Change the page
    return query.onItem().transformToUni(state -> save(changePage(state, changes)));
  }

  @Override
  public Uni<List<Entity<Page>>> pages(List<PageMutator> mutators) {
    // Get the page
    
    final List<String> ids = new ArrayList<>();
    final Map<String, PageMutator> changes = new HashMap<>();
    for(var m : mutators) {
      changes.put(m.getPageId(), m);
      ids.add(m.getPageId());
    }

    return config.getClient()
        .objects().blobState()
        .repo(config.getRepoName())
        .anyId(config.getHeadName())
        .blobNames(ids)
        .list().onItem()
        .transformToUni(state -> {
          if(state.getStatus() != ObjectsStatus.OK) {
            throw new QueryException(String.join(",", ids), EntityType.PAGE, state);  
          }
          
          final List<Entity<Page>> toBeSaved = state.getObjects().getBlob().stream()
          .map(blob -> {
            final Entity<Page> start = config.getDeserializer().fromString(EntityType.PAGE, blob.getValue());
            final PageMutator mutator = changes.get(start.getId());
            final Entity<Page> end = ImmutableEntity.<Page>builder()
                .from(start)
                .body(ImmutablePage.builder().from(start.getBody())
                    .content(mutator.getContent())
                    .locale(mutator.getLocale())
                    .build())
                .build();
            return end;
          }).collect(Collectors.toList());
          
          final var command = config.getClient().commit().head().head(config.getRepoName(), config.getHeadName());
          toBeSaved.forEach(e -> command.append(e.getId(), config.getSerializer().toString(e)));

          return command
            .message("update type: '" + EntityType.PAGE + "', with ids: '" + String.join(",", ids) + "'")
            .parentIsLatest()
            .author(config.getAuthorProvider().getAuthor())
            .build().onItem().transform(commit -> {
              if(commit.getStatus() == CommitStatus.OK) {
                return toBeSaved;
              }
              throw new SaveException(new ArrayList<>(toBeSaved), commit);
              
            });
        });
  }
  
  private Entity<Page> changePage(EntityState<Page> state, PageMutator changes) {
    final var start = state.getEntity();
    return ImmutableEntity.<Page>builder()
        .from(start)
        .body(ImmutablePage.builder().from(start.getBody())
            .content(changes.getContent())
            .locale(changes.getLocale())
            .build())
        .build();
  }

  @Override
  public Uni<Entity<Link>> link(LinkMutator changes) {
    // Get the link
    final Uni<EntityState<Link>> query = get(changes.getLinkId(), EntityType.LINK);
    
    // Change the link
    return query.onItem().transformToUni(state -> save(changeLink(state, changes)));
  }
  
  private Entity<Link> changeLink(EntityState<Link> state, LinkMutator changes) {
    final var start = state.getEntity();
    return ImmutableEntity.<Link>builder()
        .from(start)
        .body(ImmutableLink.builder().from(start.getBody())
            .content(changes.getContent())
            .description(changes.getDescription())
            .locale(changes.getLocale())
            .contentType(changes.getType())
            .articles(changes.getArticles())
            .build())
        .build();
  }

  @Override
  public Uni<Entity<Workflow>> workflow(WorkflowMutator changes) {
    // Get the Workflow
    final Uni<EntityState<Workflow>> query = get(changes.getWorkflowId(), EntityType.WORKFLOW);
    
    // Change the Workflow
    return query.onItem().transformToUni(state -> save(changeWorkflow(state, changes)));
  }
  
  private Entity<Workflow> changeWorkflow(EntityState<Workflow> state, WorkflowMutator changes) {
    final var start = state.getEntity();
    return ImmutableEntity.<Workflow>builder()
        .from(start)
        .body(ImmutableWorkflow.builder().from(start.getBody())
            .content(changes.getContent())
            .locale(changes.getLocale())
            .name(changes.getName())
            .articles(changes.getArticles())
            .build())
        .build();
  }
}
