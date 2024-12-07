package blockchains.iaas.uni.stuttgart.de.tccsci.model.responses;

import blockchains.iaas.uni.stuttgart.de.scip.model.responses.AsyncScipResponse;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionVerdict;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public abstract class TccsciResponse extends AsyncScipResponse {
    protected DistributedTransactionVerdict verdict;
    protected String message;

    public TccsciResponse(@NonNull DistributedTransaction distributedTransaction, String message) {
        super(distributedTransaction.getId().toString());
        this.message = message;
        this.verdict = distributedTransaction.getVerdict();
    }
}
