/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.task.repository.database.support;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.cloud.task.repository.database.PagingQueryProvider;
import org.springframework.cloud.task.util.TestDBUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author Glenn Renfro
 */
@RunWith(Parameterized.class)
public class WhereClausePagingQueryProviderTests {

	private String databaseProductName;
	private String expectedQuery;
	private Pageable pageable = new PageRequest(0, 10);

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"Oracle", "SELECT TASK_EXECUTION_ID, START_TIME, END_TIME, TASK_NAME, "
						+ "EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED FROM "
						+ "(SELECT TASK_EXECUTION_ID, START_TIME, END_TIME, TASK_NAME, "
						+ "EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED, ROWNUM as "
						+ "TMP_ROW_NUM FROM (SELECT TASK_EXECUTION_ID, START_TIME, "
						+ "END_TIME, TASK_NAME, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED "
						+ "FROM %PREFIX%EXECUTION "
						+ "WHERE TASK_EXECUTION_ID = '0000' ORDER BY START_TIME DESC, "
						+ "TASK_EXECUTION_ID DESC)) WHERE TMP_ROW_NUM >= 1 AND "
						+ "TMP_ROW_NUM < 11"},
				{"HSQL Database Engine","SELECT LIMIT 0 10 TASK_EXECUTION_ID, "
						+ "START_TIME, END_TIME, TASK_NAME, EXIT_CODE, EXIT_MESSAGE, "
						+ "LAST_UPDATED FROM %PREFIX%EXECUTION "
						+ "WHERE TASK_EXECUTION_ID = '0000' ORDER BY "
						+ "START_TIME DESC, TASK_EXECUTION_ID DESC"},
				{"PostgreSQL","SELECT TASK_EXECUTION_ID, START_TIME, END_TIME, "
						+ "TASK_NAME, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED "
						+ "FROM %PREFIX%EXECUTION WHERE TASK_EXECUTION_ID = '0000' "
						+ "ORDER BY START_TIME DESC, "
						+ "TASK_EXECUTION_ID DESC LIMIT 10 OFFSET 0"},
				{"MySQL","SELECT TASK_EXECUTION_ID, START_TIME, END_TIME, TASK_NAME, "
						+ "EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED FROM "
						+ "%PREFIX%EXECUTION WHERE TASK_EXECUTION_ID = '0000' "
						+ "ORDER BY START_TIME DESC, "
						+ "TASK_EXECUTION_ID DESC LIMIT 0, 10"},
				{"Microsoft SQL Server","SELECT TASK_EXECUTION_ID, START_TIME, END_TIME, "
						+ "TASK_NAME, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED FROM "
						+ "(SELECT TASK_EXECUTION_ID, START_TIME, END_TIME, TASK_NAME, "
						+ "EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED, ROW_NUMBER() "
						+ "OVER (ORDER BY START_TIME DESC, TASK_EXECUTION_ID DESC) AS "
						+ "TMP_ROW_NUM  FROM %PREFIX%EXECUTION WHERE TASK_EXECUTION_ID = "
						+ "'0000') TASK_EXECUTION_PAGE  WHERE TMP_ROW_NUM >= 1 "
						+ "AND TMP_ROW_NUM < 11 ORDER BY START_TIME DESC, TASK_EXECUTION_ID DESC"}
		});
	}

	public WhereClausePagingQueryProviderTests(String databaseProductName, String expectedQuery) {
		this.databaseProductName = databaseProductName;
		this.expectedQuery = expectedQuery;
	}

	@Test
	public void testGeneratedQuery() throws Exception{
		PagingQueryProvider pagingQueryProvider =
				TestDBUtils.getPagingQueryProvider(databaseProductName,
						"TASK_EXECUTION_ID = '0000'");
		String actualQuery = pagingQueryProvider.getPageQuery(pageable);
		assertEquals(String.format(
				"the generated query for %s, was not the expected query",
				databaseProductName), expectedQuery, actualQuery);
	}
}
