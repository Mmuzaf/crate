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

package io.crate.plugin.sigar;

import com.google.common.collect.Lists;
import io.crate.plugin.AbstractPlugin;
import org.elasticsearch.ElasticsearchIllegalStateException;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class CrateSigarPlugin extends AbstractPlugin {

    private final SigarService sigarService;
    private final NodeEnvironment nodeEnvironment;
    private final Settings settings;

    public CrateSigarPlugin(Settings settings) {
        this.settings = settings;
        this.sigarService = new SigarService(settings);
        Environment environment = new Environment(settings);
        try {
            nodeEnvironment = new NodeEnvironment(settings, environment);
        } catch (IOException ex) {
            throw new ElasticsearchIllegalStateException("Failed to created node environment", ex);
        }
    }

    @Override
    public String name() {
        return "crate-sigar";
    }

    @Override
    public String description() {
        return "crate node info";
    }

    @Override
    public Collection<Module> modules(Settings settings) {
        List<Module> modules = Lists.newArrayList(super.modules(settings));
        if(settings.getAsBoolean("sigar.enabled", true)) {
            modules.add(new CrateSigarModule(sigarService, nodeEnvironment, settings));
        }
        return modules;
    }

}
