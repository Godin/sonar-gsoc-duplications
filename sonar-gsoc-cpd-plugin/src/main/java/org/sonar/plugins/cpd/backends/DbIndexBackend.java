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
package org.sonar.plugins.cpd.backends;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.database.DatabaseSession;
import org.sonar.api.resources.Project;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.plugins.cpd.CpdPlugin;

public class DbIndexBackend implements CpdIndexBackend {

  public static final String BACKEND_KEY = "db";

  private final DatabaseSession session;

  public DbIndexBackend(DatabaseSession session) {
    this.session = session;
  }

  public String getBackendKey() {
    return BACKEND_KEY;
  }

  public CloneIndex getCloneIndex(Project project) {
    Configuration conf = project.getConfiguration();
    String key = conf.getString("sonar.newcpd.cloneGroup", CpdPlugin.CPD_DEFAULT_CLONE_GROUP);
    return new DbCloneIndex(session, key);
  }

}
