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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.ClonePart;

import java.io.File;
import java.util.*;

public class CpdAnalyser {

  private static final Logger LOG = LoggerFactory.getLogger(CpdAnalyser.class);

  private SensorContext context;
  private Project project;

  public CpdAnalyser(Project project, SensorContext context) {
    this.context = context;
    this.project = project;
  }

  public void analyse(List<Clone> clones) {
    Map<Resource, DuplicationsData> duplicationsData = new HashMap<Resource, DuplicationsData>();

    for (Clone clone : clones) {
      ClonePart originPart = clone.getOriginPart();
      String firstResourceId = originPart.getResourceId();
      int firstLineStart = originPart.getLineStart();
      int firstLineEnd = originPart.getLineEnd();
      int firstCloneLength = firstLineEnd - firstLineStart + 1;

      Resource firstFile = getResource(new File(firstResourceId));
      if (firstFile == null) {
        LOG.warn("CPD - File not found : {}", firstResourceId);
        continue;
      }

      DuplicationsData firstFileData = getDuplicationsData(duplicationsData, firstFile);
      firstFileData.incrementDuplicatedBlock();

      for (ClonePart secondPart : clone.getCloneParts()) {
        if (secondPart.equals(originPart)) {
          continue;
        }
        String secondResourceId = secondPart.getResourceId();

        int secondLineStart = secondPart.getLineStart();

        firstFileData.cumulate(secondResourceId, secondLineStart, firstLineStart, firstCloneLength);
      }
    }

    for (DuplicationsData data : duplicationsData.values()) {
      data.saveUsing(context);
    }
  }

  private Resource getResource(File file) {
    return JavaFile.fromIOFile(file, project.getFileSystem().getSourceDirs(), false);
  }

  private DuplicationsData getDuplicationsData(Map<Resource, DuplicationsData> fileContainer, Resource file) {
    DuplicationsData data = fileContainer.get(file);
    if (data == null) {
      data = new DuplicationsData(file, context);
      fileContainer.put(file, data);
    }
    return data;
  }

  private static final class DuplicationsData {

    protected Set<Integer> duplicatedLines = new HashSet<Integer>();
    protected double duplicatedBlocks = 0;
    protected Resource resource;
    private SensorContext context;
    private List<XmlEntry> duplicationXMLEntries = new ArrayList<XmlEntry>();

    private static final class XmlEntry {
      protected StringBuilder xml;
      protected int startLine;
      protected int lines;

      private XmlEntry(int startLine, int lines, StringBuilder xml) {
        this.xml = xml;
        this.startLine = startLine;
        this.lines = lines;
      }
    }

    private DuplicationsData(Resource resource, SensorContext context) {
      this.context = context;
      this.resource = resource;
    }

    protected void cumulate(String targetResource, int targetDuplicationStartLine, int duplicationStartLine, int duplicatedLines) {
      StringBuilder xml = new StringBuilder();
      xml.append("<duplication lines=\"").append(duplicatedLines).append("\" start=\"").append(duplicationStartLine)
          .append("\" target-start=\"").append(targetDuplicationStartLine).append("\" target-resource=\"")
          .append(targetResource).append("\"/>");

      duplicationXMLEntries.add(new XmlEntry(duplicationStartLine, duplicatedLines, xml));

      for (int duplicatedLine = duplicationStartLine; duplicatedLine < duplicationStartLine + duplicatedLines; duplicatedLine++) {
        this.duplicatedLines.add(duplicatedLine);
      }
    }

    protected void incrementDuplicatedBlock() {
      duplicatedBlocks++;
    }

    protected void saveUsing(SensorContext context) {
      context.saveMeasure(resource, CoreMetrics.DUPLICATED_FILES, 1d);
      context.saveMeasure(resource, CoreMetrics.DUPLICATED_LINES, (double) duplicatedLines.size());
      context.saveMeasure(resource, CoreMetrics.DUPLICATED_BLOCKS, duplicatedBlocks);
      context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATIONS_DATA, getDuplicationXMLData()));
    }

    private String getDuplicationXMLData() {
      StringBuilder duplicationXML = new StringBuilder("<duplications>");

      Comparator<XmlEntry> comp = new Comparator<XmlEntry>() {
        public int compare(XmlEntry o1, XmlEntry o2) {
          if (o1.startLine == o2.startLine) {
            return o2.lines - o1.lines;
          }
          return o1.startLine - o2.startLine;
        }
      };
      Collections.sort(duplicationXMLEntries, comp);

      for (XmlEntry xmlEntry : duplicationXMLEntries) {
        duplicationXML.append(xmlEntry.xml);
      }
      duplicationXML.append("</duplications>");
      return duplicationXML.toString();
    }
  }
}
