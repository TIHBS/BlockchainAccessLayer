package blockchains.iaas.uni.stuttgart.de.management.callback;


import blockchains.iaas.uni.stuttgart.de.adaptation.BlockchainAdapterFactory;
import blockchains.iaas.uni.stuttgart.de.config.ObjectMapperProvider;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Media;
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

    public void sendCallback(final String endpointUrl, final CallbackMessage responseBody) {
        final Client client = ClientBuilder.newBuilder()
                .register(ObjectMapperProvider.class)  // No need to register this provider if no special configuration is required.
                .register(JacksonFeature.class)
                .build();
        final Entity entity = Entity.entity(responseBody, MediaType.APPLICATION_JSON);
        final Response response = client.target(endpointUrl).request(MediaType.APPLICATION_JSON)
                .post(entity);

        log.info("Callback client responded with code({})", response.getStatus());
    }


    public void sendCallbackAsync(final String endpointUrl, final CallbackMessage responseBody) {
        this.executorService.execute(() -> sendCallback(endpointUrl, responseBody));
    }

}
