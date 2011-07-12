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

import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.database.DatabaseSession;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.*;

import java.io.File;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class CpdAnalyserTest {
  @Test
  public void testFlat3Map() {

    File file = new File("src/test/files/Flat3Map.java");
    InputFile inputFile = InputFileUtils.create(new File("src/test/files/"), file);

    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);

    when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("src/test/files/")));
    when(fileSystem.mainFiles(Java.KEY)).thenReturn(Arrays.asList(inputFile));

    Resource resource1 = JavaFile.fromIOFile(file, fileSystem.getSourceDirs(), false);

    Project project = new Project("key").setFileSystem(fileSystem);
    project.setLanguageKey(Java.KEY);
    BaseConfiguration conf = new BaseConfiguration();
    conf.setProperty("sonar.newcpd.blockSize", "15");
    project.setConfiguration(conf);

    SensorContext context = mock(SensorContext.class);


    when(context.saveResource(resource1)).thenReturn("key1");

    DatabaseSession session = mock(DatabaseSession.class);
    CpdSensor sensor = new CpdSensor(session);
    sensor.analyse(project, context);
    verify(context).saveMeasure(resource1, CoreMetrics.DUPLICATED_FILES, 1d);
    verify(context, atLeastOnce()).saveResource(resource1);
  }
}
