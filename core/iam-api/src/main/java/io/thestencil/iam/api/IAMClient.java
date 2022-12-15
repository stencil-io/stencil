package io.thestencil.iam.api;

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

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;

public interface IAMClient {

  LivenessQuery livenessQuery();
  UserQuery userQuery();
  
  interface LivenessQuery {
    Uni<UserLiveness> get();
  }
  interface UserQuery {
    Uni<UserQueryResult> get();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableUserLiveness.class) @JsonDeserialize(as = ImmutableUserLiveness.class)
  interface UserLiveness {
    // Issuance in seconds
    long getIssuedAtTime();
    
    // Expiration in seconds
    long getExpiresIn();
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableUser.class) @JsonDeserialize(as = ImmutableUser.class)
  interface User {
    String getId();
    String getSsn();
    String getFirstName();
    String getLastName();
    Contact getContact();
    Boolean getProtectionOrder();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableContact.class) @JsonDeserialize(as = ImmutableContact.class)
  interface Contact {
    String getEmail();
    @Nullable
    Address getAddress();
    @Nullable
    String getAddressValue();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableAddress.class) @JsonDeserialize(as = ImmutableAddress.class)
  interface Address {
    String getLocality();
    String getStreet();
    String getPostalCode();
    String getCountry();
  }
  
  enum ResultType { OK, ANONYMOUS }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUserQueryResult.class) @JsonDeserialize(as = ImmutableUserQueryResult.class)
  interface UserQueryResult {
    ResultType getType();
    @Nullable
    User getUser();
  }
}
