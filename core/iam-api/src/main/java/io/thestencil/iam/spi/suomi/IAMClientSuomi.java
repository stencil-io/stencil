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
import io.thestencil.iam.api.ImmutableIAMClientConfig;
import io.thestencil.iam.api.ImmutableUserLiveness;
import io.thestencil.iam.api.RemoteIntegration;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.ext.web.client.WebClient;

public class IAMClientSuomi implements IAMClient {

  private final IAMClientConfig config;
  private final RequestOptions roles;
  public IAMClientSuomi(IAMClientConfig config) {
    super();
    this.config = config;
    this.roles = new RequestOptions()
        .setURI(config.getSecurityProxy().getPath())
        .setHost(config.getSecurityProxy().getHost());
  }

  @Override
  public UserRolesQuery userRolesQuery() {
    return new UserRolesQueryImpl(config, roles);
  }

  @Override
  public IAMClientConfig getConfig() {
    return config;
  }
  
  @Override
  public UserQuery userQuery() {
    return new UserQuerySuomi(config.getToken());
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
    final var idToken = config.getToken();
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
    private WebClient webClient;
    private String servicePath;
    private RemoteIntegration securityProxy;
    
    public Builder webClient(WebClient webClient) { this.webClient = webClient; return this; }
    public Builder servicePath(String servicePath) { this.servicePath = servicePath; return this; }
    public Builder securityProxy(RemoteIntegration securityProxy) { this.securityProxy = securityProxy; return this; }
    public Builder idToken(JsonWebToken idToken) { this.idToken = idToken; return this; }
    
    public IAMClientSuomi builder() {
      return new IAMClientSuomi(ImmutableIAMClientConfig.builder()
          .token(idToken)
          .servicePath(servicePath)
          .securityProxy(securityProxy)
          .webClient(webClient)
          .build()); 
    }
  }
}
