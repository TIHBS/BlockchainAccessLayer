package blockchains.iaas.uni.stuttgart.demo;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import io.reactivex.Observable;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EthereumPlugin extends Plugin {
    public EthereumPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Extension
    public static class EthAdapterImpl implements IAdapterExtenstion {

        @Override
        public CompletableFuture<Transaction> submitTransaction(String receiverAddress, BigDecimal value, double requiredConfidence) throws InvalidTransactionException, NotSupportedException {
            return null;
        }

        @Override
        public Observable<Transaction> receiveTransactions(String senderId, double requiredConfidence) throws NotSupportedException {
            return null;
        }

        @Override
        public CompletableFuture<TransactionState> ensureTransactionState(String transactionId, double requiredConfidence) throws NotSupportedException {
            return null;
        }

        @Override
        public CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId) throws NotSupportedException {
            return null;
        }

        @Override
        public CompletableFuture<Transaction> invokeSmartContract(String smartContractPath, String functionIdentifier, List<Parameter> inputs, List<Parameter> outputs, double requiredConfidence) throws BalException {
            return null;
        }

        @Override
        public Observable<Occurrence> subscribeToEvent(String smartContractAddress, String eventIdentifier, List<Parameter> outputParameters, double degreeOfConfidence, String filter) throws BalException {
            return null;
        }

        @Override
        public CompletableFuture<QueryResult> queryEvents(String smartContractAddress, String eventIdentifier, List<Parameter> outputParameters, String filter, TimeFrame timeFrame) throws BalException {
            return null;
        }

        @Override
        public String testConnection() {
            return null;
        }
    }
}
