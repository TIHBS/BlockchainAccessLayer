package blockchains.iaas.uni.stuttgart.de.restapi.controllers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.SubmitTransactionRequest;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("submit-transaction")
public class SubmitTransactionController extends SubscriptionController {
    private static final Logger log = LoggerFactory.getLogger(SubmitTransactionController.class);

    @GET
    public Response get() {
        return getSubscriptions(SubscriptionType.SUBMIT_TRANSACTION, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response submitTransaction(SubmitTransactionRequest request) {
        final BlockchainManager manager = new BlockchainManager();
        manager.submitNewTransaction(request.getSubscriptionId(), request.getTo(), request.getValue(), request.getBlockchainId(),
                request.getRequiredConfidence(), request.getEpUrl());

        return Response.created(UriUtil.generateSubResourceURI(this.uriInfo, request.getSubscriptionId(), false))
                .build();
    }

    @POST
    @Path("/dummy")
    @Consumes( {MediaType.APPLICATION_JSON})
    public Response dummyEndPoint(Object response) {
        log.info("dummy path received the following response: {}", response.toString());
        return Response.accepted().build();
    }
}
