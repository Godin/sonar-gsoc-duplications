/*
 * Sonar, open source software quality management tool.
 * Written (W) 2011 Andrew Tereskin
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
package org.sonar.duplications.api.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonar.duplications.api.CloneIndexException;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.lexer.BlockBuilder;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.StatementBuilder;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.JavaStatementBuilder;

public class FileBlockGroup {
	private final String fileResourceId;
	private final List<Block> fileBlocks = new ArrayList<Block>();

	public FileBlockGroup(String fileResourceId) {
		this.fileResourceId = fileResourceId;
		//init();
	}

	public FileBlockGroup(File sourceFile) {
		this.fileResourceId = sourceFile.getAbsolutePath();
		//init();
	}

	public void addBlock(Block block) {
		if (!getFileResourceId().equals(block.getResourceId())) {
			throw new CloneIndexException(
					"Block resourceId not equals to FileBlockGroup resourceId");
		}
		fileBlocks.add(block);
	}

	public String getFileResourceId() {
		return fileResourceId;
	}

	public List<Block> getBlockList() {
		return Collections.unmodifiableList(fileBlocks);
	}

	public void init() {
		try {
			Lexer lexer = JavaLexer.build();
			StatementBuilder statementBuilder = JavaStatementBuilder.build();
			BlockBuilder blockBuilder = new BlockBuilder(new File(
					fileResourceId));

			fileBlocks.addAll(blockBuilder.build(statementBuilder.build(lexer
					.lex(new File(fileResourceId)))));
		} catch (Exception e) {
			throw new CloneIndexException("Error in initialization", e);
		}
	}

}
