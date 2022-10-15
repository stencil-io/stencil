package io.thestencil.client.api;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import java.util.Collection;
import java.util.List;

import org.immutables.value.Value;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer.Entity;
import io.thestencil.client.api.StencilComposer.EntityBody;
import io.thestencil.client.api.StencilComposer.EntityType;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilStoreConfig.EntityState;

public interface StencilStore {
  <T extends EntityBody> Uni<Entity<T>> delete(Entity<T> toBeDeleted);
  <T extends EntityBody> Uni<EntityState<T>> get(String blobId, EntityType type);
  <T extends EntityBody> Uni<Entity<T>> save(Entity<T> toBeSaved);
  <T extends EntityBody> Uni<Entity<T>> create(Entity<T> toBeSaved);
  Uni<Collection<Entity<?>>> saveAll(Collection<Entity<?>> toBeSaved);
  
  
  Uni<Collection<Entity<?>>> batch(BatchCommand batch);
  
  
  @Value.Immutable
  @SuppressWarnings("rawtypes")
  interface BatchCommand {
    List<Entity> getToBeCreated();
    List<Entity> getToBeSaved();
    List<Entity> getToBeDeleted();
  }
  
  
  QueryBuilder query();
  StoreRepoBuilder repo();
  
  String getRepoName();
  String getHeadName();

  String gid(EntityType type);
  
  interface QueryBuilder {
    Uni<SiteState> head();
    Uni<SiteState> release(String releaseId);
    <T extends EntityBody> Uni<List<Entity<T>>> head(List<String> ids, EntityType type);
  }
  
  interface StoreRepoBuilder {
    StoreRepoBuilder repoName(String repoName);
    StoreRepoBuilder headName(String headName);
    Uni<StencilStore> create();    
    StencilStore build();
    Uni<Boolean> createIfNot();
  }
  
}
