package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.config.ObjectMapperProvider;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

@Path("plugins")
public class PluginManagerController {
    private static final Logger log = LoggerFactory.getLogger(PluginManagerController.class);

    @Context
    ResourceContext resourceContext;

    @Context
    protected UriInfo uriInfo;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadJar(@FormDataParam("file") InputStream uploadedInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetails) {

        BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
        String fileName = fileDetails.getFileName();
        log.info("Received file {}", fileName);
        String uploadedFileLocation = blockchainPluginManager.getPluginsPath() + "/" + fileDetails.getFileName();
        java.nio.file.Path filePath = Paths.get(uploadedFileLocation);
        if (Files.exists(filePath)) {
            log.error("Received file {} already exists in plugins directory.", fileName);
            return Response.status(Response.Status.BAD_REQUEST).entity("File already exists with same name.").build();
        }
        writeToFile(uploadedInputStream, uploadedFileLocation);

        blockchainPluginManager.loadJar(filePath);

        return Response.ok().build();
    }

    @POST
    @Path("{plugin-id}/enable")
    public Response enablePlugin(@PathParam("plugin-id") final String pluginId) {
        BlockchainPluginManager.getInstance().enablePlugin(pluginId);
        return Response.ok().build();
    }

    @POST
    @Path("{plugin-id}/start")
    public Response startPlugin(@PathParam("plugin-id") final String pluginId) {
        BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
        blockchainPluginManager.startPlugin(pluginId);

        ObjectMapperProvider objectMapperProvider = resourceContext.getResource(ObjectMapperProvider.class);

        ObjectMapper objectMapper = objectMapperProvider.getContext(ObjectMapper.class);
        blockchainPluginManager.registerConnectionProfileSubtypeClass(objectMapper, pluginId);
        return Response.ok().build();
    }

    @POST
    @Path("{plugin-id}/disable")
    public Response disablePlugin(@PathParam("plugin-id") final String pluginId) {
        BlockchainPluginManager.getInstance().disablePlugin(pluginId);
        return Response.ok().build();
    }

    @POST
    @Path("{plugin-id}/unload")
    public Response unloadPlugin(@PathParam("plugin-id") final String pluginId) {
        BlockchainPluginManager.getInstance().unloadPlugin(pluginId);
        return Response.ok().build();
    }

    @DELETE
    @Path("{plugin-id}")
    public Response deletePlugin(@PathParam("plugin-id") final String pluginId) {
        BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
        blockchainPluginManager.deletePlugin(pluginId);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlugins() {
        BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
        List<PluginWrapper> plugins = blockchainPluginManager.getPlugins();

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode parentArray = objectMapper.createArrayNode();
        for (PluginWrapper p : plugins) {
            ObjectNode pluginInfo = objectMapper.createObjectNode();
            pluginInfo.put("plugin-id", p.getPluginId());
            pluginInfo.put("status", String.valueOf(p.getPluginState()));
            parentArray.add(pluginInfo);
        }
        return Response.ok().entity(parentArray).build();
    }

    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) {

        try {
            OutputStream out = new FileOutputStream(new File(
                    uploadedFileLocation));
            ;
            try {
                int read = 0;
                byte[] bytes = new byte[1024];

                out = new FileOutputStream(new File(uploadedFileLocation));
                while ((read = uploadedInputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
            } finally {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
