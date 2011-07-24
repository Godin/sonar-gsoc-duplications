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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

@Properties({
    @Property(
        key = "sonar.newcpd.memcachedAddress",
        defaultValue = MemcachedPlugin.MEMCACHED_DEFAULT_CONNECT_STRING,
        name = "Memcached connect address in format \"host:port\"",
        description = "Memcached connect address in format \"host:port\"",
        project = true,
        module = true,
        global = true
    )
})
public class MemcachedPlugin extends SonarPlugin {

  public final static String MEMCACHED_DEFAULT_CONNECT_STRING = "localhost:11211";

  public List getExtensions() {
    return Arrays.asList(MemcachedIndexBackend.class);
  }

}
