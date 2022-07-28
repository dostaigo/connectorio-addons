/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.itest.s7;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.connectorio.addons.itest.base.OfflineKarInstallationTest;
import org.connectorio.addons.itests.exam.openhab.OpenHABDistributionKitOption;
import org.connectorio.addons.itests.exam.openhab.container.CustomOptions;
import org.connectorio.addons.itests.exam.openhab.container.OpenHABTestContainerFactory;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamFactory(OpenHABTestContainerFactory.class)
@ExamReactorStrategy(PerClass.class)
public class S7OfflineKarInstallationTest extends OfflineKarInstallationTest {

  public S7OfflineKarInstallationTest() {
    super("org.connectorio.addons", "org.connectorio.addons.kar.s7", "openhab-binding-s7");
  }

  @Override
  protected Set<Option> customize() {
    return new LinkedHashSet<>(Arrays.asList(
      mavenBundle(maven("org.connectorio.addons", "org.connectorio.addons.itest.base").versionAsInProject()),
      new OpenHABDistributionKitOption().unpackDirectory(new File("target/distro")),
      new CustomOptions()
    ));
  }

}