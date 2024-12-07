package blockchains.iaas.uni.stuttgart.de.tccsci.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Getter
public class DistributedTransaction {
    private final UUID id;
    private final List<String> blockchainIds;
    private DistributedTransactionState state;
    private DistributedTransactionVerdict verdict;
    private AtomicInteger yes = new AtomicInteger(0);

    public DistributedTransaction(UUID id) {
        this.id = id;
        this.blockchainIds = new ArrayList<>();
        this.state = DistributedTransactionState.STARTED;
        this.verdict = DistributedTransactionVerdict.NOT_DECIDED;
    }

    public DistributedTransaction() {
        this(UUID.randomUUID());
    }

    public void addBlockchainId(String blockchainId) {
        this.blockchainIds.add(blockchainId);
    }

}
