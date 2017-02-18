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

package io.crate.operation.reference.sys.node.local;

import io.crate.operation.reference.sys.SysStaticObjectArrayReference;
import org.elasticsearch.threadpool.ThreadPool;

class NodeThreadPoolsExpression extends SysStaticObjectArrayReference {

    private final ThreadPool threadPool;

    NodeThreadPoolsExpression(ThreadPool threadPool) {
        this.threadPool = threadPool;
        addChildImplementations();
    }

    private void addChildImplementations() {
        for (ThreadPool.Info info : threadPool.info()) {
            childImplementations.add(new NodeThreadPoolExpression(threadPool, info.getName()));
        }
    }
}
