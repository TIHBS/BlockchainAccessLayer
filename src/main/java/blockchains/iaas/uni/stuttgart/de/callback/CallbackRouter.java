package blockchains.iaas.uni.stuttgart.de.callback;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.api.model.LinearChainTransaction;
import blockchains.iaas.uni.stuttgart.de.api.model.Occurrence;
import blockchains.iaas.uni.stuttgart.de.api.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.api.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.restapi.callback.CamundaMessageTranslator;
import blockchains.iaas.uni.stuttgart.de.restapi.callback.RestCallbackManager;
import blockchains.iaas.uni.stuttgart.de.scip.callback.ScipCallbackManager;
import blockchains.iaas.uni.stuttgart.de.scip.model.common.Argument;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.*;

/**
 * Routes callbacks to the correct CallbackManager
 */
public class CallbackRouter {

    private static CallbackRouter instance;

    private CallbackRouter() {

    }

    public static CallbackRouter getInstance() {
        if (instance == null) {
            instance = new CallbackRouter();
        }

        return instance;
    }

    public void sendAsyncError(String correlationId, String endpointUrl, String bindingType, TransactionState txState, BalException exception) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallbackAsync(endpointUrl,
                    CamundaMessageTranslator.convert(correlationId, txState, true, exception.getCode()));
        } else {
            ScipCallbackManager.getInstance().sendAsyncErrorResponse(correlationId, bindingType, new AsynchronousBalException(exception, correlationId));
        }
    }

    public void sendSubmitTransactionResponse(String correlationId, String endpointUrl, String bindingType, Transaction tx) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallback(endpointUrl, CamundaMessageTranslator.convert(correlationId, tx, false));
        } else {
            SendTxResponse response = SendTxResponse.builder()
                    .correlationId(correlationId)
                    .build();
            ScipCallbackManager.getInstance().sendAsyncResponse(endpointUrl, bindingType, response);
        }
    }

    public void sendReceiveTransactionsResponse(String correlationId, String endpointUrl, String bindingType, Transaction tx) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallback(endpointUrl, CamundaMessageTranslator.convert(correlationId, tx, false));
        } else {
            throw new IllegalStateException("SCIP does not include the ReceiveTransactions method");
        }
    }

    public void sendReceiveTransactionResponse(String correlationId, String endpointUrl, String bindingType, Transaction tx) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallback(endpointUrl, CamundaMessageTranslator.convert(correlationId, tx, false));
        } else if (tx instanceof LinearChainTransaction ltx){
            ReceiveTxResponse response = ReceiveTxResponse.builder()
                    .correlationId(correlationId)
                    .from(ltx.getFrom())
                    .value(ltx.getValue().longValue())
                    .build();
            ScipCallbackManager.getInstance().sendAsyncResponse(endpointUrl, bindingType, response);
        } else {
            throw new IllegalStateException("The passed transaction must be of type LinearChainTransaction");
        }
    }

    public void sendDetectOrphanedTransactionResponse(String correlationId, String endpointUrl, String bindingType, TransactionState txState) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallback(endpointUrl, CamundaMessageTranslator.convert(correlationId, txState, false, 0));
        } else {
            throw new IllegalStateException("SCIP does not include the DetectOrphanedTransaction method");
        }
    }

    public void sendEnsureTransactionStateResponse(String correlationId, String endpointUrl, String bindingType, TransactionState txState) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallback(endpointUrl, CamundaMessageTranslator.convert(correlationId, txState, false, 0));
        } else {
            EnsureStateResponse response = EnsureStateResponse.builder().correlationId(correlationId).build();
            ScipCallbackManager.getInstance().sendAsyncResponse(endpointUrl, bindingType, response);
        }
    }

    public void sendInvokeSCFunctionResponse(String correlationId, String endpointUrl, String bindingType, Transaction tx) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallback(endpointUrl,
                    CamundaMessageTranslator.convert(correlationId, tx, false));
        } else {
            InvokeResponse response = InvokeResponse
                    .builder()
                    .correlationId(correlationId)
                    .outputArguments(tx.getReturnValues().stream()
                            .map(p -> Argument.builder()
                                    .name(p.getName()).value(p.getValue())
                                    .build()
                            ).toList())
                    .build();
            ScipCallbackManager.getInstance().sendAsyncResponse(endpointUrl, bindingType, response);
        }
    }

    public void sendSubscribeResponse(String correlationId, String endpointUrl, String bindingType, Occurrence occurrence, LinearChainTransaction tx) {
        if (bindingType == null || bindingType.isEmpty()) {
            RestCallbackManager.getInstance().sendCallback(endpointUrl,
                    CamundaMessageTranslator.convert(correlationId, tx, false));
        } else {
            SubscribeResponse response = SubscribeResponse
                    .builder()
                    .correlationId(correlationId)
                    .timestamp(occurrence.getIsoTimestamp())
                    .arguments(occurrence
                            .getParameters()
                            .stream()
                            .map(p -> Argument.builder()
                                    .name(p.getName())
                                    .value(p.getValue())
                                    .build()
                            ).toList())
                    .build();
            ScipCallbackManager.getInstance().sendAsyncResponse(endpointUrl, bindingType, response);
        }
    }


}
