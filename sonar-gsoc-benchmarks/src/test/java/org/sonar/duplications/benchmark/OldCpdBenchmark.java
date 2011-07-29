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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.pmd.cpd.AbstractLanguage;
import net.sourceforge.pmd.cpd.JavaTokenizer;
import net.sourceforge.pmd.cpd.TokenEntry;

import org.sonar.duplications.cpd.CPD;
import org.sonar.duplications.cpd.Match;

public class OldCpdBenchmark extends Benchmark {

  public static final int MIN_TOKENS_DEFAULT = 100;
  public static final boolean IGNORE_LITERALS_DEFAULT = true;
  public static final boolean IGNORE_IDENTIFIERS = false;

  private final List<File> files;
  private int count;

  public OldCpdBenchmark(List<File> files) {
    this.files = files;
  }

  @Override
  public void runRound() throws Exception {
    count = 0;
    Iterator<Match> matches = singleRun(files);
    while (matches.hasNext()) {
      matches.next();
      count++;
    }
  }

  public int getCount() {
    return count;
  }

  public static Iterator<Match> singleRun(List<File> files) {
    try {
      TokenEntry.clearImages();

      JavaTokenizer tokenizer = new JavaTokenizer();
      tokenizer.setIgnoreLiterals(IGNORE_LITERALS_DEFAULT); // Default in Sonar
      tokenizer.setIgnoreIdentifiers(IGNORE_IDENTIFIERS); // Default in Sonar
      AbstractLanguage javaLanguage = new AbstractLanguage(tokenizer, ".java") {
      };

      CPD cpd = new CPD(MIN_TOKENS_DEFAULT, javaLanguage);
      cpd.setEncoding("UTF-8");
      cpd.setLoadSourceCodeSlices(false);
      cpd.add(files);

      cpd.go();

      return cpd.getMatches();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}