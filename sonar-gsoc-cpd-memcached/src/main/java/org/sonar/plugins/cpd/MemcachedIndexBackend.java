/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cpd;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.Project;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.plugins.cpd.backends.CpdIndexBackend;

import java.io.IOException;

public class MemcachedIndexBackend implements CpdIndexBackend {

  private static final Logger LOG = LoggerFactory.getLogger(MemcachedIndexBackend.class);

  public static final String BACKEND_KEY = "memcached";

  public String getBackendKey() {
    return BACKEND_KEY;
  }

  public CloneIndex getCloneIndex(Project project) {
    Configuration conf = project.getConfiguration();
    String key = conf.getString("sonar.newcpd.cloneGroup", CpdPlugin.CPD_DEFAULT_CLONE_GROUP);
    String addr = conf.getString("sonar.newcpd.memcachedAddress", MemcachedPlugin.MEMCACHED_DEFAULT_CONNECT_STRING);
    MemcachedClient client = null;
    try {
      client = new MemcachedClient(AddrUtil.getAddresses(addr));
    } catch (IOException ex) {
      LOG.error("Error during memcached connect", ex);
    }
    return new MemcachedCloneIndex(key, client);
  }


}
