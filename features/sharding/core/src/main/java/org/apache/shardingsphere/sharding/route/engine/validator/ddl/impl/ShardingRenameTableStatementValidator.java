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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.connection.ShardingDDLRouteException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharding rename table statement validator.
 */
public final class ShardingRenameTableStatementValidator extends ShardingDDLStatementValidator {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final List<Object> params, final ShardingSphereDatabase database,
                            final ConfigurationProperties props) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList())
                : sqlStatementContext.getTablesContext().getTableNames();
        RenameTableStatement renameTableStatement = (RenameTableStatement) sqlStatementContext.getSqlStatement();
        List<SimpleTableSegment> renameTables = renameTableStatement.getRenameTables().stream().map(RenameTableDefinitionSegment::getRenameTable).collect(Collectors.toList());
        if (!renameTables.isEmpty() && shardingRule.tableRuleExists(tableNames)) {
            throw new UnsupportedShardingOperationException("RENAME TABLE", renameTables.get(0).getTableName().getIdentifier().getValue());
        }
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        RenameTableStatement renameTableStatement = (RenameTableStatement) sqlStatementContext.getSqlStatement();
        for (RenameTableDefinitionSegment each : renameTableStatement.getRenameTables()) {
            String primaryTable = each.getTable().getTableName().getIdentifier().getValue();
            if (isRouteUnitDataNodeDifferentSize(shardingRule, routeContext, primaryTable)) {
                throw new ShardingDDLRouteException("RENAME", "TABLE", sqlStatementContext.getTablesContext().getTableNames());
            }
        }
    }
}
