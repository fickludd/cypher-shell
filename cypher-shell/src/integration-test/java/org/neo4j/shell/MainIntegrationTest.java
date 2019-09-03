package org.neo4j.shell;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.log.AnsiLogger;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.prettyprint.PrettyConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainIntegrationTest {

    private String inputString = String.format( "neo4j%nneo%n" );
    private ByteArrayOutputStream baos;
    private ConnectionConfig connectionConfig;
    private CypherShell shell;
    private Main main;

    @Before
    public void setup() {
        // given
        InputStream inputStream = new ByteArrayInputStream( inputString.getBytes() );

        baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream( baos );

        main = new Main( inputStream, ps );

        CliArgs cliArgs = new CliArgs();
        cliArgs.setUsername("", "");
        cliArgs.setPassword( "", "" );

        Logger logger = new AnsiLogger(cliArgs.getDebugMode());
        PrettyConfig prettyConfig = new PrettyConfig(cliArgs);
        connectionConfig = new ConnectionConfig(
                cliArgs.getScheme(),
                cliArgs.getHost(),
                cliArgs.getPort(),
                cliArgs.getUsername(),
                cliArgs.getPassword(),
                cliArgs.getEncryption());

        shell = new CypherShell(logger, prettyConfig);
    }


    @Test
    public void promptsOnWrongAuthenticationIfInteractive() throws Exception {
        // when
        assertEquals("", connectionConfig.username());
        assertEquals("", connectionConfig.password());

        main.connectMaybeInteractively(shell, connectionConfig, true, true);

        // then
        // should be connected
        assertTrue(shell.isConnected());
        // should have prompted and set the username and password
        assertEquals("neo4j", connectionConfig.username());
        assertEquals("neo", connectionConfig.password());

        String out = baos.toString();
        assertEquals( String.format( "username: neo4j%npassword: ***%n" ), out );
    }

    @Test
    public void promptsSilentlyOnWrongAuthenticationIfOutputRedirected() throws Exception {
        // when
        assertEquals("", connectionConfig.username());
        assertEquals("", connectionConfig.password());

        main.connectMaybeInteractively(shell, connectionConfig, true, false);

        // then
        // should be connected
        assertTrue(shell.isConnected());
        // should have prompted silently and set the username and password
        assertEquals("neo4j", connectionConfig.username());
        assertEquals("neo", connectionConfig.password());

        String out = baos.toString();
        assertEquals( "", out );
    }
}
