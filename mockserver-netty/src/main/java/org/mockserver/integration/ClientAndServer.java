package org.mockserver.integration;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.filters.record.RequestAndResponseRecorder;
import org.mockserver.mockserver.MockServer;
import org.mockserver.socket.PortFactory;

/**
 * @author jamesdbloom
 */
public class ClientAndServer extends MockServerClient {

    private MockServer mockServer;

    public ClientAndServer() {
        this(PortFactory.findFreePort());
    }

    public ClientAndServer(Integer port) {
        this(port, null);
    }

    public ClientAndServer(Integer port, RequestAndResponseRecorder recorder) {
        super("localhost", port);
        mockServer = new MockServer(port, recorder);
    }

    public static ClientAndServer startClientAndServer(Integer port) {
        return new ClientAndServer(port);
    }

    public static ClientAndServer startClientAndServer(Integer port, RequestAndResponseRecorder recorder) {
        return new ClientAndServer(port, recorder);
    }

    public boolean isRunning() {
        return mockServer.isRunning();
    }

}
