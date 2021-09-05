package io.thestencil.client.spi;

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

import java.util.function.Consumer;

import io.thestencil.client.api.CreateBuilder;
import io.thestencil.client.api.DeleteBuilder;
import io.thestencil.client.api.MigrationBuilder;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.UpdateBuilder;
import io.thestencil.client.spi.builders.CreateBuilderImpl;
import io.thestencil.client.spi.builders.DeleteBuilderImpl;
import io.thestencil.client.spi.builders.MigrationBuilderImpl;
import io.thestencil.client.spi.builders.QueryBuilderImpl;
import io.thestencil.client.spi.builders.UpdateBuilderImpl;

public class StencilClientImpl implements StencilClient {

  private final PersistenceConfig config;
  
  public StencilClientImpl(PersistenceConfig config) {
    super();
    this.config = config;
  }

  @Override
  public CreateBuilder create() {
    return new CreateBuilderImpl(config);
  }

  @Override
  public UpdateBuilder update() {
    return new UpdateBuilderImpl(config);
  }

  @Override
  public DeleteBuilder delete() {
    return new DeleteBuilderImpl(config);
  }
  
  @Override
  public QueryBuilder query() {
    return new QueryBuilderImpl(config);
  }

  @Override
  public MigrationBuilder migration() {
    return new MigrationBuilderImpl(config);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private ImmutablePersistenceConfig.Builder config = ImmutablePersistenceConfig.builder();
    
    public Builder config(Consumer<ImmutablePersistenceConfig.Builder> config) {
      config.accept(this.config);
      return this;
    }
    public StencilClientImpl build() {
      return new StencilClientImpl(config.build());
    }
  }
}
