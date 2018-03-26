package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.ResourceManager;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.SubmitTransactionRequest;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CorrelatedResponse;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.TransactionCorrelatedResponse;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


@Path("submit-transaction")
public class SubmitTransactionController extends SubscriptionController {
    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);

    @GET
    public Response get(){
        return getSubscriptions(SubscriptionType.SUBMIT_TRANSACTION, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response submitTransaction(SubmitTransactionRequest request){
        final ResourceManager manager = new ResourceManager();
        manager.submitNewTransaction(request.getSubscriptionId(), request.getTo(), request.getValue(), request.getBlockchainId(),
                request.getWaitFor(), request.getEpUrl());

        return Response.created(UriUtil.generateSubResourceURI(this.uriInfo, request.getSubscriptionId(), false))
                .build();
    }


    @POST
    @Path("/dummy")
    @Consumes(MediaType.APPLICATION_XML)
    public Response dummyEndPoint(TransactionCorrelatedResponse response){

        log.info("dummy path received the following transaction status: {}", response.getState());
        return Response.accepted().build();
    }

}
