package io.thestencil.client.spi.builders;

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

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.VersionBuilder;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@NoArgsConstructor
public class VersionBuilderImpl implements VersionBuilder {

    @Override
    public Uni<String> version() {
      final String[] version = {"unknown"};
      final String[] timestamp = {"unknown"};
      ClassLoader classLoader = getClass().getClassLoader();
      File file = new File(classLoader.getResource("version.txt").getFile());
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        br.lines().forEach(line -> {
          if (line.startsWith("version=")) {
            version[0] = line.substring(8);
          }
          if (line.startsWith("timestamp=")) {
            timestamp[0] = line.substring(10);
          }
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
      String finalVersion = version[0];
      String finalTimestamp = timestamp[0];
      return Uni.createFrom().item(() -> finalVersion.concat(";").concat(finalTimestamp));
    }
}
