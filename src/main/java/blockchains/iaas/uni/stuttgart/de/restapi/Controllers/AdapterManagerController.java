package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BlockchainIdNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("adapter")
public class AdapterManagerController {

    private static final Logger log = LoggerFactory.getLogger(AdapterManagerController.class);

    @Context
    protected UriInfo uriInfo;

    @GET
    @Path("list")
    public Response getActiveAdapters() {
        AbstractConnectionProfile[] profiles = AdapterManager.getInstance().getActiveAdapters();
        log.info("Active profiles: [{}]", profiles);
        return Response.ok().build();

    }

    @POST
    @Path("activate")
    public Response activateConfiguration() {
        // TODO
        String configurationId = this.uriInfo.getQueryParameters().getFirst("configuration-id");

        try {
            AdapterManager.getInstance().getAdapter(configurationId);
        } catch (BlockchainIdNotFoundException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("BlockchainIdNotFoundException for configuration-id: " + configurationId).build();
        }
        log.info("Activating profile: [{}]", configurationId);
        return Response.ok().build();

    }

    @POST
    @Path("deactivate")
    public Response deactivateConfiguration() {
        // TODO
        String configurationId = this.uriInfo.getQueryParameters().getFirst("configuration-id");
        log.info("Deactivating profile: [{}]", configurationId);
        try {
            // BlockchainAdapter map = AdapterManager.getInstance().getActiveAdapters();
        } catch (BlockchainIdNotFoundException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity("Entity not found for configuration-id: " + configurationId).build();
        }
        return Response.ok().build();
    }
}
