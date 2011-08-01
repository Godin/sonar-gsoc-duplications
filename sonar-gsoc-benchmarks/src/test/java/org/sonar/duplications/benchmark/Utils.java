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
package org.sonar.duplications.benchmark;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public final class Utils {

  private Utils() {
  }

  private static final String[] PROJECTS = {
      "activemq-core-5.5.0",
      "openejb-jee-3.1.4",
      "struts-1.3.9",
      "struts-el-1.2.9",
      // "jboss-as-server-6.0.0.Final",
      // "commons-collections-3.2",
      // "easybeans-core-1.2.1",
      // "neo4j-kernel-1.4",
      // "jackrabbit-jcr-tests-2.2.7",
      // "struts2-embeddedjsp-plugin-2.2.3",
      // "tomcat-jasper-7.0.19",
      // "empire-db-2.1.0-incubating"
  };

  public static List<File> filesFromJdk16() {
    List<File> files = Utils.listJavaFiles(new File("/tmp/jdk-src"));
    System.out.println(files.size() + " files to analyse");
    return files;
  }

  public static List<File> filesFromDifferentProjects() {
    List<File> files = Lists.newArrayList();
    for (String project : PROJECTS) {
      List<File> projectFiles = Utils.getProjectFiles(project);
      System.out.println(projectFiles.size() + " in " + project);
      files.addAll(projectFiles);
    }
    System.out.println(files.size() + " files to analyse");
    return files;
  }

  public static List<File> getProjectFiles(String project) {
    return listJavaFiles(new File("target/test-projects/" + project));
  }

  public static List<File> listJavaFiles(File dir) {
    if (dir.exists() && dir.isDirectory()) {
      return Lists.newArrayList(FileUtils.listFiles(dir, new String[] { "java" }, true));
    } else {
      return Collections.emptyList();
    }
  }

}
