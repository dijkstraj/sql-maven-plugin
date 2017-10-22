# sql-maven-plugin

Execute SQL commands during a Maven build.
Can be used to create a database for an integration test.

## Usage

Add this to your POM to execute your scripts during the `verify` phase:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.dijkstraj</groupId>
      <artifactId>sql-maven-plugin</artifactId>
      <version>1.0-SNAPSHOT</version>
      <configuration>
        <url>jdbc:mysql://127.0.0.1:3306</url>
        <username>root</username>
        <password>${mysql.root.password}</password>
      </configuration>
      <executions>
        <execution>
          <id>create-databases</id>
          <goals>
            <goal>execute</goal>
          </goals>
          <configuration>
            <fileSet>
              <directory>src/test/sql</directory>
              <includes>
                <include>create-databases.sql</include>
                <include>insert-testdata.sql</include>
              </includes>
            </fileSet>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```