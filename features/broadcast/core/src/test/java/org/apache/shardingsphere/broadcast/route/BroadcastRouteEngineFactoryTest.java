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

package org.apache.shardingsphere.broadcast.route;

import org.apache.shardingsphere.broadcast.route.engine.BroadcastRouteEngineFactory;
import org.apache.shardingsphere.broadcast.route.engine.type.BroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastRouteEngineFactoryTest {
    
    private BroadcastRule broadcastRule;
    
    private ShardingSphereDatabase database;
    
    private QueryContext queryContext;
    
    private ConfigurationProperties props;
    
    private ConnectionContext connectionContext;
    
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    @BeforeEach
    void setUp() {
        broadcastRule = mock(BroadcastRule.class);
        database = mock(ShardingSphereDatabase.class);
        queryContext = mock(QueryContext.class);
        props = mock(ConfigurationProperties.class);
        connectionContext = mock(ConnectionContext.class);
        globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
    }
    
    @Test
    void assertNewInstanceWithTCLStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(TCLStatement.class));
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        BroadcastRouteEngine engine = BroadcastRouteEngineFactory.newInstance(broadcastRule, database, queryContext, props, connectionContext, globalRuleMetaData);
        assertThat(engine, instanceOf(BroadcastDatabaseBroadcastRoutingEngine.class));
    }
}
