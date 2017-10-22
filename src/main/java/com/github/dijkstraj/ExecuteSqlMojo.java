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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jooq.lambda.Unchecked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Stream;

@Mojo(name = "execute", defaultPhase = LifecyclePhase.VERIFY)
public class ExecuteSqlMojo extends AbstractMojo {

    @Parameter()
    private String driver;

    @Parameter(required = true)
    private String url;

    @Parameter(required = true)
    private String username;

    @Parameter(required = true)
    private String password;

    @Parameter(required = true)
    private FileSet fileSet;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        loadJdbcDriverIfDefined();

        final String encoding = project.getProperties().getProperty("project.build.encoding", "UTF-8");
        files().forEach(Unchecked.consumer(file -> withConnection(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute(readFile(file, encoding));
                getLog().info("Executed SQL from " + file.toString());
            }
        })));
    }

    private void loadJdbcDriverIfDefined() throws MojoExecutionException {
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new MojoExecutionException("Could not load database driver", e);
            }
        }
    }

    private Stream<Path> files() {
        FileSetManager fileSetManager = new FileSetManager();
        return Arrays.stream(fileSetManager.getIncludedFiles(fileSet))
                .map(relativePath -> Paths.get(fileSet.getDirectory(), relativePath));
    }

    private String readFile(Path file, String encoding) throws MojoExecutionException {
        try {
            return new String(Files.readAllBytes(file), encoding);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not read " + file.toString(), e);
        }
    }

    private void withConnection(ConnectionConsumer consumer) throws MojoExecutionException {
        try {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                consumer.consume(connection);
            }
        } catch (SQLException e) {
            throw new MojoExecutionException("Error while talking to the database", e);
        }
    }
}
