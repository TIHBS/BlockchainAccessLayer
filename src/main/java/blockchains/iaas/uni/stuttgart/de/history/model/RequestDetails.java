package blockchains.iaas.uni.stuttgart.de.history.model;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.api.model.Block;
import blockchains.iaas.uni.stuttgart.de.api.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.api.model.TransactionState;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class RequestDetails {
    private Transaction transaction;
    private Throwable exception;
    final private RequestType type;
    @NonNull final private String blockchainId;

    public RequestDetails(RequestType type, @NonNull String blockchainId) {
        this.type = type;
        this.blockchainId = blockchainId;
    }

    public void setTxState(TransactionState txState) {
        if (transaction == null) {
            transaction = new Transaction();
        }

        transaction.setState(txState);
    }

    public TransactionState getTxState() {
        if (transaction == null) {
            return TransactionState.UNKNOWN;
        }

        return transaction.getState();
    }
}
