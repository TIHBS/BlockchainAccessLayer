package blockchains.iaas.uni.stuttgart.de.history;

import blockchains.iaas.uni.stuttgart.de.history.model.RequestDetails;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class RequestHistoryManager {
    private final Map<String, RequestDetails> history;
    private static RequestHistoryManager instance;

    public static RequestHistoryManager getInstance() {
        if (instance == null) {
            instance = new RequestHistoryManager();
        }

        return instance;
    }

    private RequestHistoryManager() {
        history = new HashMap<>();
    }

    public void addRequestDetails(String correlationId, RequestDetails details) {
        history.put(correlationId, details);
    }

    public RequestDetails getRequestDetails(String correlationId) {
        return history.get(correlationId);
    }

}
