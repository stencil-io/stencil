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

import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer.Article;
import io.thestencil.client.api.StencilComposer.Entity;
import io.thestencil.client.api.StencilComposer.EntityType;
import io.thestencil.client.api.StencilComposer.Link;
import io.thestencil.client.api.StencilComposer.Page;
import io.thestencil.client.api.StencilComposer.Workflow;
import io.thestencil.client.spi.PersistenceConfig;
import io.thestencil.client.spi.exceptions.QueryException;
import io.thestencil.client.spi.exceptions.SaveException;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableWorkflow;

public class ArticleDeleteVisitor {
  private final PersistenceConfig config;
  private final String articleId;
  
  public ArticleDeleteVisitor(PersistenceConfig config, String articleId) {
    super();
    this.articleId = articleId;
    this.config = config;
  }
  
  public Uni<Entity<Article>> visit() {
    return config.getClient()
        .objects().refState()
        .repo(config.getRepoName())
        .ref(config.getHeadName())
        .blobs()
        .build().onItem()
        .transformToUni(state -> visitObjects(state));
  }
  
  @SuppressWarnings("unchecked")
  private Uni<Entity<Article>> visitObjects(ObjectsResult<RefObjects> state) {
    if(state.getStatus() != ObjectsStatus.OK) {
      throw new QueryException(articleId, EntityType.ARTICLE, state);
    }
  
    final var start = visitArticleId(state, articleId);
    final var updateCommand = config.getClient().commit().head();
    final var message = new StringBuilder("delete: " + articleId);
    
    for(TreeValue treeValue : state.getObjects().getTree().getValues().values()) {
      final var blob = state.getObjects().getBlobs().get(treeValue.getBlob());
      final var entity = this.config.getDeserializer().fromString(blob.getValue());

      if(entity.getId().equals(articleId)) {
        continue;
      }

      if(entity.getType() == EntityType.PAGE) {
        visitPage((Entity<Page>) entity).ifPresent(changeEntity -> {
          updateCommand.remove(changeEntity.getId());
          //message.append(System.lineSeparator()).append("deleting type: '" + changeEntity.getType() + "' with id:'" + changeEntity.getId() + "'");
        });
      } else if(entity.getType() == EntityType.WORKFLOW) {
        visitWorkflow((Entity<Workflow>) entity).ifPresent(changeEntity -> {
          updateCommand.append(changeEntity.getId(), config.getSerializer().toString(changeEntity));
          //message.append(System.lineSeparator()).append("change type: '" + changeEntity.getType() + "' with id:'" + changeEntity.getId() + "'");
        });
      } else if(entity.getType() == EntityType.LINK) {
        visitLink((Entity<Link>) entity).ifPresent(changeEntity -> {
          updateCommand.append(changeEntity.getId(), config.getSerializer().toString(changeEntity));
          //message.append(System.lineSeparator()).append("changes type: '" + changeEntity.getType() + "' with id:'" + changeEntity.getId() + "'");
        });
      } else if(entity.getType() == EntityType.ARTICLE) {
        visitArticle((Entity<Article>) entity).ifPresent(changeEntity -> {
          updateCommand.append(changeEntity.getId(), config.getSerializer().toString(changeEntity));
          //message.append(System.lineSeparator()).append("changes type: '" + changeEntity.getType() + "' with id:'" + changeEntity.getId() + "'");
        });
      }
    }
    
    return updateCommand
      .head(config.getRepoName(), config.getHeadName())
      .message(message.toString())
      .parentIsLatest()
      .remove(start.getId())
      .author(config.getAuthorProvider().getAuthor())
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return start;
        }
        throw new SaveException(start, commit);
      });
  }
  
  public Optional<Entity<?>> visitArticle(Entity<Article> start) {
    
    if(start.getBody().getParentId() != null  && 
        start.getBody().getParentId().equals(articleId)) {
      return Optional.of(ImmutableEntity.<Article>builder()
          .id(start.getId()).type(start.getType())
          .body(ImmutableArticle.builder().from(start.getBody())
              .parentId(null)
              .build())
          .build());
    }
    
    return Optional.empty();
  }

  public Optional<Entity<?>> visitLink(Entity<Link> start) {
    var newArticles = start.getBody()
        .getArticles().stream().filter(a -> !a.equals(articleId))
        .collect(Collectors.toList());
    
    if(newArticles.size() == start.getBody().getArticles().size()) {
      return Optional.empty();
    }
    
    return Optional.of(ImmutableEntity.<Link>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableLink.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build());
  }
  
  public Optional<Entity<?>> visitPage(Entity<Page> page) {
    if(page.getBody().getArticle().equals(articleId)) {
      return Optional.of(page);
    }
    return Optional.empty();
  }
  public Optional<Entity<?>> visitWorkflow(Entity<Workflow> start) {
    var newArticles = start.getBody()
        .getArticles().stream().filter(a -> !a.equals(articleId))
        .collect(Collectors.toList());
    
    if(newArticles.size() == start.getBody().getArticles().size()) {
      return Optional.empty();
    }
    
    return Optional.of(ImmutableEntity.<Workflow>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableWorkflow.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build());
  }
  
  @SuppressWarnings("unchecked")
  private Entity<Article> visitArticleId(ObjectsResult<RefObjects> state, String articleId) {
    final Optional<TreeValue> treeValue = state.getObjects().getTree().getValues().values().stream().filter(b -> b.getName().equals(articleId)).findFirst();
    if(treeValue.isEmpty()) {
      throw new QueryException(articleId, EntityType.ARTICLE, state);
    }
    final var blob = state.getObjects().getBlobs().get(treeValue.get().getBlob());
    final Entity<?> blobEntity = this.config.getDeserializer().fromString(blob.getValue());
    if(blobEntity.getType() != EntityType.ARTICLE) {
      throw new QueryException(articleId, EntityType.ARTICLE, state);
    }
    return (Entity<Article>) blobEntity;
  }
}
