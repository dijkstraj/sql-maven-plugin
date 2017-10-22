package com.github.dijkstraj;

/*-
 * #%L
 * com.github.dijkstraj:sql-maven-plugin
 * %%
 * Copyright (C) 2017 dijkstraj
 * %%
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
 * #L%
 */

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.project.MavenProject;
import org.h2.jdbc.JdbcSQLException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExecuteSqlMojoIT {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private void executeMojoInProject(String projectDir) throws Exception {
        File projectRoot = this.resources.getBasedir(projectDir);
        File pom = new File(projectRoot, "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        MavenProject project = this.rule.readMavenProject(projectRoot);
        ExecuteSqlMojo mojo = (ExecuteSqlMojo) this.rule.lookupConfiguredMojo(project, "execute");
        assertNotNull(mojo);
        mojo.execute();
    }

    @Test
    public void happyPath() throws Exception {
        executeMojoInProject("happy");
    }

    @Test
    public void shouldSortQueriesByFilename() throws Exception {
        thrown.expectCause(allOf(
                instanceOf(JdbcSQLException.class),
                hasProperty("message", containsString("Table \"TEST1\" not found"))
        ));
        executeMojoInProject("wrong-order");
    }
}
