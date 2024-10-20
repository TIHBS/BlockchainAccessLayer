package blockchains.iaas.uni.stuttgart.de.history;

import blockchains.iaas.uni.stuttgart.de.api.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.history.model.RequestDetails;
import blockchains.iaas.uni.stuttgart.de.history.model.RequestType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestHistoryManagerTest {

    @Test
    void getInstance() {
        RequestHistoryManager instance1 = RequestHistoryManager.getInstance();
        assertNotNull(instance1);
        RequestHistoryManager instance2 = RequestHistoryManager.getInstance();
        assertEquals(instance1, instance2);
    }

    @Test
    void addRequestDetails() {
        RequestHistoryManager instance = RequestHistoryManager.getInstance();
        instance.addRequestDetails("abc1", new RequestDetails(RequestType.Subscribe, "bc1"));
        RequestDetails details = instance.getRequestDetails("abc1");
        assertNotNull(details);
        assertEquals(RequestType.Subscribe, details.getType());
        assertEquals("bc1", details.getBlockchainId());
        assertNull(details.getTransaction());
        assertEquals(TransactionState.UNKNOWN, details.getTxState());
        details.setTxState(TransactionState.NOT_FOUND);
        assertNotNull(details.getTransaction());
        assertEquals(TransactionState.NOT_FOUND, details.getTransaction().getState());
        assertEquals(TransactionState.NOT_FOUND, details.getTxState());
        instance.addRequestDetails("abc2", new RequestDetails(RequestType.InvokeSCFunction, "bc1"));
        details = instance.getRequestDetails("abc2");
        assertNotNull(details);
        assertEquals(RequestType.InvokeSCFunction, details.getType());
        assertEquals("bc1", details.getBlockchainId());
        instance.addRequestDetails("abc1", new RequestDetails(RequestType.SendTx, "bc2"));
        details = instance.getRequestDetails("abc1");
        assertNotNull(details);
        assertEquals(RequestType.SendTx, details.getType());
        assertEquals("bc2", details.getBlockchainId());
    }

}