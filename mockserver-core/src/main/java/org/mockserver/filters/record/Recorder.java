package org.mockserver.filters.record;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.RecordEvent;

/**
 * @author Patrice Chalcol
 */
public interface Recorder {
    void fireEvent(RecordEvent event);
    void record(HttpRequest request);
    void record(HttpResponse response);
}
