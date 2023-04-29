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

package org.apache.shardingsphere.infra.executor.sql.process;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShowProcessListManager.class)
class ProcessReporterTest {
    
    @Mock
    private ShowProcessListManager showProcessListManager;
    
    @BeforeEach
    void setUp() {
        when(ShowProcessListManager.getInstance()).thenReturn(showProcessListManager);
    }
    
    @Test
    void assertReportExecute() {
        ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext = mockExecutionGroupContext();
        new ProcessReporter().reportExecute(new QueryContext(null, null, null), executionGroupContext);
        verify(showProcessListManager).putProcessContext(eq(executionGroupContext.getReportContext().getExecutionID()), any());
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionGroupContext<? extends SQLExecutionUnit> mockExecutionGroupContext() {
        ExecutionGroupContext<? extends SQLExecutionUnit> result = mock(ExecutionGroupContext.class);
        ExecutionGroupReportContext reportContext = mock(ExecutionGroupReportContext.class);
        when(reportContext.getExecutionID()).thenReturn(UUID.randomUUID().toString());
        when(result.getReportContext()).thenReturn(reportContext);
        return result;
    }
    
    @Test
    void assertReportUnit() {
        when(showProcessListManager.getProcessContext("foo_id")).thenReturn(mock(ProcessContext.class));
        new ProcessReporter().reportComplete("foo_id", 1);
        verify(showProcessListManager).getProcessContext("foo_id");
    }
    
    @Test
    void assertReportClean() {
        when(showProcessListManager.getProcessContext("foo_id")).thenReturn(mock(ProcessContext.class));
        new ProcessReporter().reset("foo_id");
        verify(showProcessListManager).removeProcessStatement("foo_id");
    }
}
