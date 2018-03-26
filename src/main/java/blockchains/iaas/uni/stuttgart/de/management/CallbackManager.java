package blockchains.iaas.uni.stuttgart.de.management;


import blockchains.iaas.uni.stuttgart.de.adaptation.BlockchainAdapterFactory;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CorrelatedResponse;
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

    public void sendCallback(final String endpointUrl, final CorrelatedResponse responseBody) {
        final Client client = ClientBuilder.newClient();
        final Response response = client.target(endpointUrl)
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(responseBody, MediaType.APPLICATION_XML));

        log.info("Callback client responded with code({})", response.getStatus());

    }


    public void sendCallbackAsync(final String endpointUrl, final CorrelatedResponse responseBody) {
        this.executorService.execute(() -> sendCallback(endpointUrl, responseBody));
    }

}
