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

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.TimeProfiler;
import org.sonar.duplications.CloneFinder;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.java.JavaCloneFinder;
import org.sonar.plugins.cpd.backends.CpdIndexBackend;
import org.sonar.plugins.cpd.backends.MemoryIndexBackend;

import java.util.List;

public class CpdSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(CpdSensor.class);

  private final CpdIndexBackend[] backends;

  public CpdSensor(CpdIndexBackend[] backends) {
    this.backends = backends;
  }

  public boolean shouldExecuteOnProject(Project project) {
    if (!Java.INSTANCE.equals(project.getLanguage())) {
      LOG.info("Detection of duplication code is not supported for {}.", project.getLanguage());
      return false;
    }
    if (isSkipped(project)) {
      LOG.info("Detection of duplicated code is skipped");
      return false;
    }
    return true;
  }

  boolean isSkipped(Project project) {
    Configuration conf = project.getConfiguration();
    return conf.getBoolean("sonar.newcpd." + project.getLanguageKey() + ".skip",
        conf.getBoolean("sonar.newcpd.skip", false));
  }

  String getBackendKey(Project project) {
    Configuration conf = project.getConfiguration();
    String key = conf.getString("sonar.newcpd." + project.getLanguageKey() + ".backend",
        conf.getString("sonar.newcpd.backend", MemoryIndexBackend.BACKEND_KEY));
    return key;
  }

  CloneIndex getCloneIndex(String key) {
    for (CpdIndexBackend backend : backends) {
      if (key.equals(backend.getBackendKey())) {
        return backend.getCloneIndex();
      }
    }
    return null;
  }

  public void analyse(Project project, SensorContext context) {
    CloneIndex index = getCloneIndex(getBackendKey(project));

    List<InputFile> inputFiles = project.getFileSystem().mainFiles(project.getLanguageKey());
    if (inputFiles.size() == 0) {
      return;
    }

    CloneFinder cf = JavaCloneFinder.build(index, getBlockSize(project));
    CpdAnalyser analyser = new CpdAnalyser(project, context);

    LOG.info("CPD :: project {}, total files {}", project.getName(), inputFiles.size());

    TimeProfiler profiler = new TimeProfiler(LOG);
    profiler.start("CPD :: tokenize and update index");
    for (InputFile inputFile : inputFiles) {
      index.remove(inputFile.getFile().getAbsolutePath());
      cf.register(inputFile.getFile());
    }
    profiler.stop();

    profiler.start("CPD :: find and report duplicates");
    long totalTimeFindClones = 0;
    for (InputFile inputFile : inputFiles) {
      cf.clearSourceFilesForDetection();
      cf.addSourceFileForDetection(inputFile.getFile().getAbsolutePath());

      long start = System.currentTimeMillis();
      List<Clone> clones = cf.findClones();
      totalTimeFindClones += System.currentTimeMillis() - start;

      analyser.analyse(clones);
    }
    profiler.stop();
    LOG.info("CPD :: time for findClones(): {} ms", totalTimeFindClones);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  int getBlockSize(Project project) {
    Configuration conf = project.getConfiguration();
    return conf.getInt("sonar.newcpd." + project.getLanguageKey() + ".blockSize",
        conf.getInt("sonar.newcpd.blockSize", CpdPlugin.CPD_BLOCK_SIZE_DEFAULT_VALUE));
  }

}
