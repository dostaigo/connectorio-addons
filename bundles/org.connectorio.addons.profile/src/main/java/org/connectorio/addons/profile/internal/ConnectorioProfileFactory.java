/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.profile.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(service = {ProfileFactory.class, ProfileTypeProvider.class})
public class ConnectorioProfileFactory implements ProfileFactory, ProfileTypeProvider {

  private Set<ProfileFactory> profileFactories = ConcurrentHashMap.newKeySet();

  @Override
  public Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback, ProfileContext profileContext) {
    if (ConnectorioProfiles.PROFILE.equals(profileTypeUID)) {
      return new ConnectorioProfile(callback, profileContext, profileFactories);
    }
    return null;
  }

  @Override
  public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
    return Arrays.asList(ConnectorioProfiles.PROFILE);
  }

  @Override
  public Collection<ProfileType> getProfileTypes(Locale locale) {
    return Arrays.asList(ConnectorioProfiles.PROFILE_TYPE);
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  protected void addProfileFactory(ProfileFactory profileFactory) {
    profileFactories.add(profileFactory);
  }

  @SuppressWarnings("null")
  protected void removeProfileFactory(ProfileFactory profileFactory) {
    boolean links = profileFactories.remove(profileFactory);
  }

}