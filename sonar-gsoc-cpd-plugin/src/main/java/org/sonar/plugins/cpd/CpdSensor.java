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

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.duplications.CloneFinder;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.sonar.duplications.statement.TokenMatcherFactory.*;

public class CpdSensor implements Sensor {

  public CpdSensor() {
  }

  public boolean shouldExecuteOnProject(Project project) {
    return Java.INSTANCE.equals(project.getLanguage());
  }

  public void analyse(Project project, SensorContext context) {
    CloneFinder cf = getCloneFinder(new MemoryCloneIndex());

    List<InputFile> inputFiles = project.getFileSystem().mainFiles(project.getLanguageKey());
    if (inputFiles.size() == 0) {
      return;
    }
    for (InputFile inputFile : inputFiles) {
      cf.register(inputFile.getFile());
      cf.addSourceFileForDetection(inputFile.getFile().getAbsolutePath());
    }

    List<Clone> cloneList = removeDuplicateClones(cf.findClones());

    CpdAnalyser analyser = new CpdAnalyser(project, context);
    analyser.analyse(removeDuplicateClones(cloneList));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private List<Clone> removeDuplicateClones(List<Clone> clones) {
    List<Clone> cloneList = new ArrayList<Clone>();
    Set<Clone> cloneSet = new HashSet<Clone>();
    for (Clone clone : clones) {
      if (!cloneSet.contains(clone)) {
        cloneSet.add(clone);
        cloneList.add(clone);
      }
    }
    return cloneList;
  }

  private TokenChunker getTokenChunker() {
    TokenChunker.Builder builder = TokenChunker
        .builder()
        .addBlackHoleChannel("\\s")
        .addBlackHoleChannel("//[^\\n\\r]*+")
        .addBlackHoleChannel("/\\*[\\s\\S]*?\\*/")
        .addChannel("\".*?\"", "LITERAL")
        .addChannel("[a-zA-Z_]++")
        .addChannel("[0-9]++", "INTEGER")
        .addChannel(".");
    return builder.build();
  }

  private StatementChunker getStatementChunker() {
    StatementChunker.Builder builder = StatementChunker
        .builder()
        .addBlackHoleChannel(from("import"), to(";"))
        .addBlackHoleChannel(from("package"), to(";"))
        .addBlackHoleChannel(token("}"))
        .addBlackHoleChannel(token("{"))
        .addChannel(from("@"), anyToken(), opt(bridge("(", ")")))
        .addChannel(from("do"))
        .addChannel(from("if"), bridge("(", ")"))
        .addChannel(from("else"), token("if"), bridge("(", ")"))
        .addChannel(from("else"))
        .addChannel(from("for"), bridge("(", ")"))
        .addChannel(from("while"), bridge("(", ")"), opt(token(";")))
        .addChannel(from("case"), to(":"))
        .addChannel(from("default"), to(":"))
        .addChannel(to(";", "{", "}"), forgiveLastToken());

    return builder.build();
  }

  private CloneFinder getCloneFinder(CloneIndex cloneIndex) {
    CloneFinder.Builder builder = CloneFinder.build()
        .setTokenChunker(getTokenChunker())
        .setStatementChunker(getStatementChunker())
        .setBlockChunker(new BlockChunker(5))
        .setCloneIndex(cloneIndex);
    return builder.build();
  }

}
