package io.thestencil.client.spi;

import java.util.Collection;

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

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer.Entity;
import io.thestencil.client.api.StencilComposer.EntityBody;
import io.thestencil.client.api.StencilComposer.EntityType;
import io.thestencil.client.spi.PersistenceConfig.EntityState;
import io.thestencil.client.spi.exceptions.DeleteException;
import io.thestencil.client.spi.exceptions.QueryException;
import io.thestencil.client.spi.exceptions.SaveException;

public class PersistenceCommands implements PersistenceConfig.Commands {
  protected final PersistenceConfig config;

  public PersistenceCommands(PersistenceConfig config) {
    super();
    this.config = config;
  }
  
  @Override
  public <T extends EntityBody> Uni<Entity<T>> delete(Entity<T> toBeDeleted) {
    return config.getClient().commit().head()
        .head(config.getRepoName(), config.getHeadName())
        .message("delete type: '" + toBeDeleted.getType() + "', with id: '" + toBeDeleted.getId() + "'")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor())
        .remove(toBeDeleted.getId())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitStatus.OK) {
            return toBeDeleted;
          }
          throw new DeleteException(toBeDeleted, commit);
        });
  }
  
  @Override
  public <T extends EntityBody> Uni<EntityState<T>> get(String blobId, EntityType type) {
    return config.getClient()
        .objects().blobState()
        .repo(config.getRepoName())
        .anyId(config.getHeadName())
        .blobName(blobId)
        .get().onItem()
        .transform(state -> {
          if(state.getStatus() != ObjectsStatus.OK) {
            throw new QueryException(blobId, type, state);  
          }
          Entity<T> start = config.getDeserializer()
              .fromString(type, state.getObjects().getBlob().getValue());
          
          return ImmutableEntityState.<T>builder()
              .src(state)
              .entity(start)
              .build();
        });
  }
  
  @Override
  public <T extends EntityBody> Uni<Entity<T>> save(Entity<T> toBeSaved) {
    return config.getClient().commit().head()
      .head(config.getRepoName(), config.getHeadName())
      .message("update type: '" + toBeSaved.getType() + "', with id: '" + toBeSaved.getId() + "'")
      .parentIsLatest()
      .author(config.getAuthorProvider().getAuthor())
      .append(toBeSaved.getId(), config.getSerializer().toString(toBeSaved))
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return toBeSaved;
        }
        throw new SaveException(toBeSaved, commit);
      });
  }

  @Override
  public Uni<Collection<Entity<?>>> save(Collection<Entity<?>> entities) {
    final var commitBuilder = config.getClient().commit().head().head(config.getRepoName(), config.getHeadName());
    final Entity<?> first = entities.iterator().next();
    
    for(final var target : entities) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
    }
    
    return commitBuilder
        .message("update type: '" + first.getType() + "', with id: '" + first.getId() + "'")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitStatus.OK) {
            return entities;
          }
          throw new SaveException(first, commit);
        });
  }
}
