package blockchains.iaas.uni.stuttgart.de.history.model;

public enum RequestType {
    SendTx,
    ReceiveTx,
    ReceiveTxs,
    EnsureState,
    DetectOrphanedTx,
    InvokeSCFunction,
    Subscribe
}
