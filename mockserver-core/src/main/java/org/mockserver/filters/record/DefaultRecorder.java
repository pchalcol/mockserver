package org.mockserver.filters.record;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.RecordEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrice Chalcol
 */
public class DefaultRecorder implements RequestAndResponseRecorder {
    public static final Logger log = LoggerFactory.getLogger(RequestAndResponseRecorder.class);
    
    @Override
    public void fireEvent(RecordEvent event) {
        log.debug("fire event: " + event);
    }

    @Override
    public void record(HttpRequest httpRequest) {
        log.debug("record request: " + httpRequest);
    }

    @Override
    public void record(HttpResponse httpResponse) {
        log.debug("record response: " + httpResponse);
    }

    @Override
    public <T extends HttpRequest> T onRequest(T httpRequest) {
        record(httpRequest);
        return httpRequest;
    }

    @Override
    public HttpResponse onResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        record(httpResponse);
        return httpResponse;
    }
}
