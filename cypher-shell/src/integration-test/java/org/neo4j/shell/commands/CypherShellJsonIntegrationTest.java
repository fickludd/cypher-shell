package org.neo4j.shell.commands;


import com.sun.org.apache.bcel.internal.generic.NEW;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.neo4j.shell.commands.StringLogger.NEWLINE;

public class CypherShellJsonIntegrationTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private StringLogger logger;
    private CypherShell shell;

    @Before
    public void setUp() throws Exception {
        logger = new StringLogger(Format.JSON);
        shell = new CypherShell(logger);
        shell.connect(new ConnectionConfig("bolt://", "localhost", 7687, "neo4j", "neo", true));
    }

    @After
    public void tearDown() throws Exception {
        shell.execute("MATCH (n) DETACH DELETE (n)");
    }

    @Test
    public void standardQuery() throws CommandException {
        // given
        shell.execute("CREATE (:A)-[:R {age: 3}]->(:A {p: 'hi'})-[:R2 {age: 2}]->(:B {p: 'ho'})<-[:R2]-(:B)");
        logger.clear();

        // when
        shell.execute("MATCH ()-[r]->(x) WHERE r.age > 1 RETURN r, x");

        // then
        System.out.println(logger.output());
        assertThat(logger.output(), equalTo(
                String.join(NEWLINE,
                        "{",
                        "  records: [",
                        "    {",
                        "      r: [:R {age: 3}],",
                        "      x: (:A {p: \"hi\"})",
                        "    },",
                        "    {",
                        "      r: [:R2 {age: 2}],",
                        "      x: (:B {p: \"ho\"})",
                        "    }",
                        "  ]",
                        "}"
                ) + NEWLINE));
    }

    @Test
    public void periodicCommitWorks() throws CommandException {
        shell.execute("USING PERIODIC COMMIT\n" +
                "LOAD CSV FROM 'https://neo4j.com/docs/cypher-refcard/3.2/csv/artists.csv' AS line\n" +
                "CREATE (:Artist {name: line[1], year: toInt(line[2])});");

        shell.execute("MATCH (a:Artist) WHERE a.name = 'Europe' RETURN a.name");

        assertThat(logger.output(), containsString("wAAt"));
    }

    @Test
    public void cypherWithProfileStatements() throws CommandException {
        //when
        shell.execute("CYPHER RUNTIME=INTERPRETED PROFILE RETURN null");

        //then
        String actual = logger.output();
        System.out.println(actual);
        //      This assertion checks everything except for time and cypher
        assertThat(actual, containsString("Plan: \"PROFILE\""));
        assertThat(actual, containsString("Statement: \"READ_ONLY\""));
        assertThat(actual, containsString("Planner: \"COST\""));
        assertThat(actual, containsString("Runtime: \"INTERPRETED\""));
        assertThat(actual, containsString("DbHits: 0"));
        assertThat(actual, containsString("Rows: 1"));
        assertThat(actual, containsString("null"));
        assertThat(actual, containsString("NULL"));
    }

    @Test
    public void cypherWithExplainStatements() throws CommandException {
        //when
        shell.execute("CYPHER RUNTIME=INTERPRETED EXPLAIN RETURN null");

        //then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).printOut(captor.capture());

        List<String> result = captor.getAllValues();
        String actual = result.get(0);
        //      This assertion checks everything except for time and cypher
        assertThat(actual, containsString("Plan: \"EXPLAIN\""));
        assertThat(actual, containsString("Statement: \"READ_ONLY\""));
        assertThat(actual, containsString("Planner: \"COST\""));
        assertThat(actual, containsString("Runtime: \"INTERPRETED\""));
    }
}
