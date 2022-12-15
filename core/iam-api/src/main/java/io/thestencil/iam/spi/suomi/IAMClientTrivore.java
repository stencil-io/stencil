package io.thestencil.iam.spi.suomi;

/*-
 * #%L
 * iam-api
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.ImmutableUserLiveness;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IAMClientTrivore implements IAMClient {
  private final JsonWebToken idToken;

  @Override
  public UserQuery userQuery() {
    return new UserQueryDefault(idToken);
  }
  
  @Override
  public LivenessQuery livenessQuery() {
    return new LivenessQuery() {
      
      @Override
      public Uni<UserLiveness> get() {
        return Uni.createFrom().item(() -> createUserLiveness());
      }
    };
  }
  
  private UserLiveness createUserLiveness() {
    final var now = LocalDateTime.now();
    final var then = LocalDateTime.ofInstant(Instant.ofEpochSecond(idToken.getExpirationTime()), ZoneId.systemDefault());
    return ImmutableUserLiveness.builder()
        .issuedAtTime(idToken.getIssuedAtTime())
        .expiresIn(Duration.between(now, then).toSeconds())
        .build();
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private JsonWebToken idToken;
    
    public Builder idToken(JsonWebToken idToken) {
      this.idToken = idToken;
      return this;
    }
    public IAMClientTrivore builder() {
      return new IAMClientTrivore(idToken);
    }
  }
}
