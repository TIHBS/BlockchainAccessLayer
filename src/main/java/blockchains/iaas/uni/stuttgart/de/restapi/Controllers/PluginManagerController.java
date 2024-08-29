/********************************************************************************
 * Copyright (c) 2022-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Akshay Patel
 * Co-Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.pf4j.DependencyResolver.DependenciesNotFoundException;
import org.pf4j.PluginWrapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController()
@RequestMapping("plugins")
@Log4j2
public class PluginManagerController {

    @PostMapping
    public ResponseEntity<String> uploadJar(@RequestParam("file") MultipartFile file,
                                            RedirectAttributes redirectAttributes) {

        BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
        final String fileName = file.getOriginalFilename();
        log.info("Received file {}", fileName);
        String uploadedFileLocation = blockchainPluginManager.getPluginsPath() + "/" + fileName;
        java.nio.file.Path filePath = Paths.get(uploadedFileLocation);

        if (Files.exists(filePath)) {
            log.error("Received file {} already exists in plugins directory.", fileName);
            return ResponseEntity.badRequest().body("File already exists with same name.");
        }

        writeToFile(file, uploadedFileLocation);

        try {
            blockchainPluginManager.loadJar(filePath);
            log.info("Successfully loaded jar file.");

            return ResponseEntity.ok().build();
        } catch (DependenciesNotFoundException e) {
            return ResponseEntity.status(400).contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
        }
    }

    @PostMapping(path = "{plugin-id}/enable")
    public void enablePlugin(@PathVariable("plugin-id") final String pluginId) {
        BlockchainPluginManager.getInstance().enablePlugin(pluginId);
    }

    @PostMapping(path = "{plugin-id}/start")
    public void startPlugin(@PathVariable("plugin-id") final String pluginId) {
        BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
        blockchainPluginManager.startPlugin(pluginId);
        blockchainPluginManager.registerConnectionProfileSubtypeClass(pluginId);
    }

    @PostMapping(path = "{plugin-id}/disable")
    public void disablePlugin(@PathVariable("plugin-id") final String pluginId) {
        BlockchainPluginManager.getInstance().disablePlugin(pluginId);
    }

    @PostMapping(path = "{plugin-id}/unload")
    public void unloadPlugin(@PathVariable("plugin-id") final String pluginId) {
        BlockchainPluginManager.getInstance().unloadPlugin(pluginId);
    }

    @DeleteMapping("{plugin-id}")
    public void deletePlugin(@PathVariable("plugin-id") final String pluginId) {
        BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
        blockchainPluginManager.deletePlugin(pluginId);
    }

    @GetMapping
    public ArrayNode getPlugins() {
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

        return parentArray;
    }

    private void writeToFile(MultipartFile file,
                             String uploadedFileLocation) {
        try (InputStream uploadedInputStream = file.getInputStream()) {

            try (OutputStream out = new FileOutputStream(uploadedFileLocation)) {
                int read;
                byte[] bytes = new byte[1024];

                while ((read = uploadedInputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }

                out.flush();
                log.debug("File {} written to disk", uploadedFileLocation);
            } catch (IOException e) {
                log.error("Failure occurred while saving the received file to disk", e);
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            log.error("Failed to create an input stream from the received file.", e);
            throw new RuntimeException(e);
        }
    }
}
