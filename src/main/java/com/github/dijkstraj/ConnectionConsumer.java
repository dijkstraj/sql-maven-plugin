package com.github.dijkstraj;

import org.apache.maven.plugin.MojoExecutionException;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionConsumer {

    void consume(Connection connection) throws SQLException, MojoExecutionException;

}
