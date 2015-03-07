package org.mockserver.mockserver;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import org.mockserver.filters.LogFilter;
import org.mockserver.filters.record.RequestAndResponseRecorder;
import org.mockserver.mock.MockServerMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public class MockServer {

    public static final AttributeKey<LogFilter> LOG_FILTER = AttributeKey.valueOf("SERVER_LOG_FILTER");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // ports
    private final Integer port;
    // mockserver
    private final MockServerMatcher mockServerMatcher = new MockServerMatcher();
    private final LogFilter logFilter = new LogFilter();
    private final SettableFuture<String> hasStarted;
    // netty
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    /**
     * Start the instance using the ports provided
     *
     * @param port the http port to use
     */
    public MockServer(final Integer port) {
        this(port, null);
    }
    
    public MockServer(final Integer port, final RequestAndResponseRecorder recorder) {
        if (port == null) {
            throw new IllegalStateException("You must specify a port");
        }
        this.port = port;

        hasStarted = SettableFuture.create();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("MockServer starting up on port: " + port);

                    Channel httpChannel = new ServerBootstrap()
                            .group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 1024)
                            .childHandler(new MockServerInitializer(mockServerMatcher, MockServer.this, recorder, false))
                            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                            .childAttr(LOG_FILTER, logFilter)
                            .bind(port)
                            .sync()
                            .channel();

                    hasStarted.set("STARTED");

                    if (httpChannel != null) {
                        httpChannel.closeFuture().sync();
                    }
                } catch (InterruptedException ie) {
                    logger.error("MockServer receive InterruptedException", ie);
                } finally {
                    bossGroup.shutdownGracefully(0, 1, TimeUnit.MILLISECONDS);
                    workerGroup.shutdownGracefully(0, 1, TimeUnit.MILLISECONDS);
                }
            }
        }).start();

        try {
            // wait for proxy to start all channels
            hasStarted.get();
        } catch (Exception e) {
            logger.debug("Exception while waiting for proxy to complete starting up", e);
        }
    }

    public void stop() {
        try {
            bossGroup.shutdownGracefully(0, 1, TimeUnit.MILLISECONDS);
            workerGroup.shutdownGracefully(0, 1, TimeUnit.MILLISECONDS);
            // wait for socket to be released
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (Exception ie) {
            logger.trace("Exception while stopping MockServer", ie);
        }
    }

    public boolean isRunning() {
        if (hasStarted.isDone()) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                logger.trace("Exception while waiting for the proxy to confirm running status", e);
            }
            return !bossGroup.isShuttingDown() && !workerGroup.isShuttingDown();
        } else {
            return false;
        }
    }

    public Integer getPort() {
        return port;
    }
}
