package io.thestencil.staticontent.spi.beans;

/*-
 * #%L
 * stencil-static-content
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

import io.thestencil.staticontent.api.SiteContent.Blob;

public class MutableBlob implements Blob {
  private String id;
  private String value;
  
  public MutableBlob() {
  }
  
  public MutableBlob(String id, String value) {
    super();
    this.id = id;
    this.value = value;
  }
  public String getId() {
    return id;
  }
  public String getValue() {
    return value;
  }
  @Override
  public String toString() {
    return "ImmutableBlob [id=" + id + ", value=" + value + "]";
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setValue(String value) {
    this.value = value;
  }
}
