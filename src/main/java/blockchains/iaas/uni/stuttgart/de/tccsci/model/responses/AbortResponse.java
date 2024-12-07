package blockchains.iaas.uni.stuttgart.de.tccsci.model.responses;

import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransaction;
import lombok.Builder;
import lombok.NonNull;

public class AbortResponse extends TccsciResponse {
    @Builder
    public AbortResponse(@NonNull DistributedTransaction distributedTransaction, String message) {
        super(distributedTransaction, message);
    }

    @Override
    public String toString() {
        return "AbortResponse{" +
                "message='" + message + '\'' +
                ", verdict=" + verdict +
                '}';
    }
}
