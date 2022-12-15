package io.thestencil.quarkus.iam;

/*-
 * #%L
 * quarkus-stencil-iam
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.arc.DefaultBean;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.spi.suomi.IAMClientTrivore;

@ApplicationScoped
public class IAMBeanFactory {
  
  /**
   * Injection point for the ID Token issued by the OpenID Connect Provider
   */
  @Inject
  JsonWebToken idToken;
  
  @Produces
  @Singleton
  @DefaultBean
  public IAMClient iamClient() {
    return IAMClientTrivore.builder().idToken(idToken).builder();
  }
}
