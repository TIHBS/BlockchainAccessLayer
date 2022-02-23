/*******************************************************************************
 * Copyright (c) 2022 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.scip.bindings.jsonrpc;

import java.io.IOException;
import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscriptionResponse;
import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.Transport;
import com.github.arteam.simplejsonrpc.client.builder.NotificationRequestBuilder;
import com.google.common.base.Charsets;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

public class JsonRpcBinding implements AbstractBinding {

    @Override
    public String getBindingIdentifier() {
        return "json-rpc";
    }

    @Override
    public void sendInvocationResponse(String endpointUrl, InvocationResponse response) {
        NotificationRequestBuilder builder = createNotificationBuilder(endpointUrl);

        builder.param("correlationIdentifier", response.getCorrelationIdentifier())
                .param("parameters", response.getParams() == null ? Collections.emptyList() : response.getParams())
                .param("timestamp", response.getTimestamp() == null ? "" : response.getTimestamp())
                .execute();
    }

    @Override
    public void sendSubscriptionResponse(String endpointUrl, SubscriptionResponse response) {
        NotificationRequestBuilder builder = createNotificationBuilder(endpointUrl);
        builder.param("correlationIdentifier", response.getCorrelationIdentifier())
                .param("parameters", response.getParams() == null ? Collections.emptyList() : response.getParams())
                .param("timestamp", response.getTimestamp() == null ? "" : response.getTimestamp())
                .execute();
    }

    @Override
    public void sendAsyncErrorResponse(String endpointUrl, AsynchronousBalException exception) {
        NotificationRequestBuilder builder = createNotificationBuilder(endpointUrl);

        builder = builder.param("errorCode", exception.getCode());
        builder = builder.param("errorMessage", exception.getMessage());
        builder = builder.param("correlationIdentifier", exception.getCorrelationIdentifier());

        if (exception instanceof TimeoutException) {
            builder = builder.param("transactionHash", ((TimeoutException) exception).getTransactionHash());
            builder = builder.param("reachedDoC", ((TimeoutException) exception).getDoc());
        }

        builder.execute();
    }

    private NotificationRequestBuilder createNotificationBuilder(final String endpointUrl) {
        final String METHOD_NAME = "ReceiveResponse";
        JsonRpcClient client = new JsonRpcClient(new Transport() {
            CloseableHttpClient httpClient = HttpClients.createDefault();

            @NotNull
            @Override
            public String pass(@NotNull String request) throws IOException {
                // Used Apache HttpClient 4.3.1 as an example
                HttpPost post = new HttpPost(endpointUrl);
                post.setEntity(new StringEntity(request, Charsets.UTF_8));
                post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                    return EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
                }
            }
        });

        return client.createNotification()
                .method(METHOD_NAME);
    }
}
