/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.metadata.persist.service.config.database;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.node.NewDatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.config.AbstractPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

/**
 * TODO Rename DatabaseRulePersistService when metadata structure adjustment completed. #25485
 * Database rule persist service.
 */
public final class NewDatabaseRulePersistService extends AbstractPersistService implements DatabaseBasedPersistService<Collection<RuleConfiguration>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    public NewDatabaseRulePersistService(final PersistRepository repository) {
        super(repository);
        this.repository = repository;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void persist(final String databaseName, final Collection<RuleConfiguration> configs) {
        Map<RuleConfiguration, NewYamlRuleConfigurationSwapper> yamlConfigs = new NewYamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
        for (Entry<RuleConfiguration, NewYamlRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            persistDataNodes(databaseName, entry.getValue().getRuleTagName().toLowerCase(), dataNodes);
        }
    }
    
    private void persistDataNodes(final String databaseName, final String ruleName, final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            if (Strings.isNullOrEmpty(NewDatabaseMetaDataNode.getDatabaseRuleActiveVersionNode(databaseName, ruleName, each.getKey()))) {
                repository.persist(NewDatabaseMetaDataNode.getDatabaseRuleActiveVersionNode(databaseName, ruleName, each.getKey()), DEFAULT_VERSION);
            }
            List<String> versions = repository.getChildrenKeys(NewDatabaseMetaDataNode.getDatabaseRuleVersionsNode(databaseName, ruleName, each.getKey()));
            repository.persist(NewDatabaseMetaDataNode.getDatabaseRuleVersionNode(databaseName, ruleName, each.getKey(), versions.isEmpty()
                    ? DEFAULT_VERSION
                    : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), each.getValue());
            
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void delete(final String databaseName, final Collection<RuleConfiguration> configs) {
        Map<RuleConfiguration, NewYamlRuleConfigurationSwapper> yamlConfigs = new NewYamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
        for (Entry<RuleConfiguration, NewYamlRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            deleteDataNodes(databaseName, entry.getValue().getRuleTagName().toLowerCase(), dataNodes);
        }
    }
    
    private void deleteDataNodes(final String databaseName, final String ruleName, final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            repository.delete(NewDatabaseMetaDataNode.getDatabaseRuleNode(databaseName, ruleName, each.getKey()));
        }
    }
    
    @Override
    public Collection<RuleConfiguration> load(final String databaseName) {
        Collection<YamlDataNode> dataNodes = getDataNodes(NewDatabaseMetaDataNode.getRulesNode(databaseName));
        return dataNodes.isEmpty() ? Collections.emptyList() : new NewYamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(dataNodes);
    }
    
    /**
     * Load rule configuration.
     *
     * @deprecated Remove this method when metadata structure adjustment completed. #25485
     */
    @Deprecated
    @Override
    public Collection<RuleConfiguration> load(final String databaseName, final String version) {
        // TODO Remove this method when metadata structure adjustment completed. #25485
        return Collections.emptyList();
    }
}
