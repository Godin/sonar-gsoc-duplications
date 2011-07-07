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
        key = "sonar.newcpd.blockSize",
        defaultValue = "" + CpdPlugin.CPD_BLOCK_SIZE_DEFAULT_VALUE,
        name = "Block size",
        description = "The number of duplicate statements above which a block is considered as a duplication.",
        project = true,
        module = true,
        global = true),
    @Property(
        key = "sonar.newcpd.skip",
        defaultValue = "false",
        name = "Skip detection of duplicated code",
        description = "Skip detection of duplicated code",
        project = true,
        module = true,
        global = true
    )
})
public class CpdPlugin extends SonarPlugin {

  public static final int CPD_BLOCK_SIZE_DEFAULT_VALUE = 5;

  public List getExtensions() {
//    return Arrays.asList(CpdSensor.class, SumDuplicationsDecorator.class, DuplicationDensityDecorator.class);
    return Arrays.asList(CpdSensor.class);
  }

}
