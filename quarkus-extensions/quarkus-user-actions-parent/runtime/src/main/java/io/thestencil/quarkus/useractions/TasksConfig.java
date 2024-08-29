package io.thestencil.quarkus.useractions;

/*-
 * #%L
 * quarkus-stencil-user-actions
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

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class TasksConfig {
  
  /**
   * backend host
   */
  @ConfigItem
  String host;
  
  /**
   * backend path
   */
  @ConfigItem
  String path;
  
  /**
   * protocol: http (default) or https
   */
  @ConfigItem(defaultValue = "http")
  String protocol;

  /**
   * port: default is 80
   */
  @ConfigItem(defaultValue = "80")
  Integer port;
}
