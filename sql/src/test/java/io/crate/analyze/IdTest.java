/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.analyze;

import com.google.common.collect.ImmutableList;
import io.crate.metadata.ColumnIdent;
import io.crate.test.integration.CrateUnitTest;
import org.apache.lucene.util.BytesRef;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.core.Is.is;

public class IdTest extends CrateUnitTest {

    private static final ColumnIdent _ID = ci("_id");
    private static final ImmutableList<ColumnIdent> _ID_LIST = ImmutableList.of(_ID);
    private static final ImmutableList<BytesRef> EMPTY_PK_VALUES = ImmutableList.of();

    private static ColumnIdent ci(String ident) {
        return new ColumnIdent(ident);
    }

    private static String generateId(List<ColumnIdent> pkColumns, List<BytesRef> values, ColumnIdent clusteredBy) {
        return Id.compileWithNullValidation(pkColumns, clusteredBy).apply(values);
    }

    @Test
    public void testAutoGenerated() throws Exception {
        String id1 = generateId(_ID_LIST, EMPTY_PK_VALUES, _ID);
        String id2 = generateId(_ID_LIST, EMPTY_PK_VALUES, _ID);

        assertThat(id1, Matchers.not(Matchers.equalTo(id2)));
    }

    @Test
    public void testAutoGeneratedWithRouting() throws Exception {
        // routing is actually not relevant for _id
        String id1 = generateId(_ID_LIST, EMPTY_PK_VALUES, ci("foo"));
        String id2 = generateId(_ID_LIST, EMPTY_PK_VALUES, ci("foo"));

        assertThat(id1, Matchers.not(Matchers.equalTo(id2)));
    }

    @Test
    public void testSinglePrimaryKey() throws Exception {
        String id = generateId(ImmutableList.of(ci("id")), ImmutableList.of(new BytesRef("1")), ci("id"));

        assertThat(id, is("1"));
    }

    @Test
    public void testSinglePrimaryKeyWithoutValue() throws Exception {
        expectedException.expect(NoSuchElementException.class);
        generateId(ImmutableList.of(ci("id")), ImmutableList.<BytesRef>of(), ci("id"));
    }

    @Test
    public void testMultiplePrimaryKey() throws Exception {
        String id = generateId(
            ImmutableList.of(ci("id"), ci("name")),
            ImmutableList.of(new BytesRef("1"), new BytesRef("foo")), null);

        assertThat(id, is("AgExA2Zvbw=="));
    }

    @Test
    public void testMultiplePrimaryKeyWithClusteredBy() throws Exception {
        String id = generateId(
            ImmutableList.of(ci("id"), ci("name")),
            ImmutableList.of(new BytesRef("1"), new BytesRef("foo")),
            ci("name")
        );
        assertThat(id, is("AgNmb28BMQ=="));
    }

    @Test
    public void testNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("A primary key value must not be NULL");
        generateId(ImmutableList.of(ci("id")), Collections.<BytesRef>singletonList(null), ci("id"));
    }
}
