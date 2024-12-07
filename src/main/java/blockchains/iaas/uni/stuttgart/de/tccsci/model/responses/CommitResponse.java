package blockchains.iaas.uni.stuttgart.de.tccsci.model.responses;

import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransaction;
import lombok.Builder;
import lombok.NonNull;

public class CommitResponse extends TccsciResponse{
    @Builder
    public CommitResponse(@NonNull DistributedTransaction distributedTransaction, String message) {
        super(distributedTransaction, message);
    }

    @Override
    public String toString() {
        return "CommitResponse{" +
                "message='" + message + '\'' +
                ", verdict=" + verdict +
                '}';
    }
}
