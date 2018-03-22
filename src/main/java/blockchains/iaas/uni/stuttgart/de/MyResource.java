package blockchains.iaas.uni.stuttgart.de;

import blockchains.iaas.uni.stuttgart.de.management.ResourceManager;
import blockchains.iaas.uni.stuttgart.de.model.request.SubmitTransactionRequest;
import blockchains.iaas.uni.stuttgart.de.model.response.StateCorrelatedResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("transaction")
public class MyResource {

    @GET
    public String get(){
        return "Well Done!";
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response submitTransaction(SubmitTransactionRequest request){
        final ResourceManager manager = new ResourceManager();
        manager.submitNewTransaction(request.getSubscriptionId(), request.getTo(), request.getValue(), request.getBlockchainId(),
                request.getWaitFor(), request.getTimeout(), request.getEpUrl());

        return Response.accepted().build();
    }

    @POST
    @Path("/dummy")
    @Consumes(MediaType.APPLICATION_XML)
    public Response dummyEndPoint(StateCorrelatedResponse object){

        return Response.accepted().build();
    }
}
