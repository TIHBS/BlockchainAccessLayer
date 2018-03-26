# Blockchain Access Layer
The project is a Java 8 web application that uses Jersey to expose a RESTful API.

## Building and Deployment
After cloning, you can build the project and package it into a WAR
file using the following command:
```
mvn package
```
Then, the WAR file (which can be found in the folder 'target' generated after 
a successful build) can be deployed on an Apache Tomcat server.

## Accessing the API
After deploying the application, you can access a json file that contains OpenApi 3 
documentation of the provided RESTful API in the following path:

```
<application-URL>/webapi/openapi.json
```

This can, in turn, be visualized using [Swagger UI](https://swagger.io/swagger-ui/)

**To summarize:** a POST method is provided for each of the following
paths to create the corresponding subscription:

```
{application-URL}/webapi/submit-transaction
{application-URL}/webapi/receive-transaction
{application-URL}/webapi/receive-transactions
{application-URL}/webapi/detect-orphaned-transaction
{application-URL}/webapi/ensure-transaction-state
```

A GET method is also provided for the aforementioned URLs that lists the currently active subscriptions.

And a DELETE method is provided in each of the following
paths to manually delete the corresponding subscription:

```
{application-URL}/webapi/submit-transaction/{subscription-id}
{application-URL}/webapi/receive-transaction/{subscription-id}
{application-URL}/webapi/receive-transactions/{subscription-id}
{application-URL}/webapi/detect-orphaned-transaction/{subscription-id}
{application-URL}/webapi/ensure-transaction-state/{subscription-id}
```