package blockchains.iaas.uni.stuttgart.de.management;


import blockchains.iaas.uni.stuttgart.de.adaptation.BlockchainAdapterFactory;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.model.response.CorrelatedResponse;
import blockchains.iaas.uni.stuttgart.de.model.response.StateCorrelatedResponse;
import blockchains.iaas.uni.stuttgart.de.model.response.TransactionCorrelatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CallbackManager {
    private static CallbackManager instance = null;
    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private CallbackManager() {

    }

    public static CallbackManager getInstance() {
        if (instance == null)
            instance = new CallbackManager();

        return instance;
    }

    public <T> void sendCallback(final String endpointUrl, final String correlationId, final T responseData) {
        CorrelatedResponse responseBody;

        if (responseData instanceof Transaction) {
            responseBody = new TransactionCorrelatedResponse();
            ((TransactionCorrelatedResponse) responseBody).setData((Transaction) responseData);
        } else if (responseData instanceof TransactionState) {
            responseBody = new StateCorrelatedResponse();
            ((StateCorrelatedResponse) responseBody).setState((TransactionState) responseData);
        } else {
            log.error("Trying to send unrecognized response body");
            return;
        }

        responseBody.setCorrelationId(correlationId);
        final Client client = ClientBuilder.newClient();
        final Response response = client.target(endpointUrl)
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(responseBody, MediaType.APPLICATION_XML));

        if (response.getStatus() != 201) {
            final String msg = "Failed with HTTP error code : " + response.getStatus();
            log.error(msg);
            throw new RuntimeException(msg);
        }

    }


    public <T> void sendCallbackAsync(final String endpointUrl, final String correlationId, final T responseBody) {
        this.executorService.execute(() -> sendCallback(endpointUrl, correlationId, responseBody));
    }

}
