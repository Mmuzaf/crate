/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.integrationtests

import io.crate.Version
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.test.ElasticsearchIntegrationTest
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

@ElasticsearchIntegrationTest.ClusterScope(randomDynamicTemplates = false)
public class GrDDLIntegrationTest extends SQLTransportIntegrationTest {

    @Test
    public void testCreateTable() throws Exception {
        execute("create table test (col1 integer primary key, col2 string) " +
                "clustered into 5 shards with (number_of_replicas = 1)");

        assert response.duration() >= 0L
        ensureYellow()
        assert client().admin().indices().exists(new IndicesExistsRequest("test")).actionGet().isExists()

        String expectedMapping = toSingleLine('''{"default":{
                "dynamic":"true",
                "_meta":{"primary_keys":["col1"]},
                "_all":{"enabled":false},
                "properties":{
                    "col1":{"type":"integer","doc_values":true},
                    "col2":{"type":"string","index":"not_analyzed","doc_values":true}
                }}}''')

        String expectedSettings = toSingleLine("""{"test":{
                "settings":{
                "index.number_of_replicas":"1",
                "index.number_of_shards":"5",
                "index.version.created":"${Version.CURRENT.esVersion.id}"
                }}}""")

        assert expectedMapping == getIndexMapping("test")
        JSONAssert.assertEquals(expectedSettings, getIndexSettings("test"), false)

        // test index usage
        execute("insert into test (col1, col2) values (1, 'foo')")
        assert response.rowCount() == 1
        refresh()
        execute("SELECT * FROM test")
        assert response.rowCount() == 1
    }
}
