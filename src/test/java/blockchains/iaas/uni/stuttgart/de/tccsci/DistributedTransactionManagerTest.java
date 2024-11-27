package blockchains.iaas.uni.stuttgart.de.tccsci;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionState;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionVerdict;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Log4j2
class DistributedTransactionManagerTest {

    static ResourceManagerSmartContract generateResourceManagerSmartContract() {

        Parameter eventParameter0 = new Parameter("txId", "string", null);
        Parameter eventParameter1 = new Parameter("vote", "boolean", null);
        SmartContractEvent abortEvent = new SmartContractEvent("abortEvent", List.of(eventParameter0));
        SmartContractEvent voteEvent = new SmartContractEvent("voteEvent", List.of(eventParameter0, eventParameter1));
        Parameter inputParameter = new Parameter();
        SmartContractFunction abortFunction = new SmartContractFunction("abort*", List.of(inputParameter), null);
        SmartContractFunction commitFunction = new SmartContractFunction("commit*", List.of(inputParameter), null);
        SmartContractFunction prepareFunction = new SmartContractFunction("prepare*", List.of(inputParameter), null);

        return new ResourceManagerSmartContract() {
            @Override
            public SmartContractEvent getAbortEvent() {
                return abortEvent;
            }

            @Override
            public SmartContractEvent getVoteEvent() {
                return voteEvent;
            }

            @Override
            public SmartContractFunction getPrepareFunction() {
                return prepareFunction;
            }

            @Override
            public SmartContractFunction getAbortFunction() {
                return abortFunction;
            }

            @Override
            public SmartContractFunction getCommitFunction() {
                return commitFunction;
            }

        };
    }

    static AdapterManager generateAdapterManager() {
        BlockchainAdapter adapter = mock(BlockchainAdapter.class);
        when(adapter.getResourceManagerSmartContract()).thenReturn(generateResourceManagerSmartContract());
        AdapterManager adapterManager = mock(AdapterManager.class);
        when(adapterManager.getAdapter(anyString())).thenReturn(adapter);

        return adapterManager;
    }

    static DistributedTransactionManager getDistributedTransactionManager(AdapterManager adapterManager, BlockchainManager blockchainManager) {
        return spy(new DistributedTransactionManager(adapterManager, blockchainManager) {
            @Override
            protected String getBlockchainIdentity(String blockchainId) {
                return blockchainId + "/user1";
            }
        });

    }

    @Test
    void testStartingDtx() {
        AdapterManager adapterManager = generateAdapterManager();
        MockBlockchainManager manager = new MockBlockchainManager(adapterManager);
        DistributedTransactionManager dManager = getDistributedTransactionManager(adapterManager, manager);
        UUID uuid = dManager.startDtx();

        assertNotNull(uuid);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertNotNull(dtx);
        assertEquals(DistributedTransactionState.STARTED, dtx.getState());
    }

    @Test
    void testAwaitingRequestsState() {
        /* DtxStart */
        AdapterManager adapterManager = generateAdapterManager();
        MockBlockchainManager manager = new MockBlockchainManager(adapterManager);
        DistributedTransactionManager dManager = getDistributedTransactionManager(adapterManager, manager);
        UUID uuid = dManager.startDtx();

        /* DtxInvoke 1 */
        Parameter uuidParameter = new Parameter("txid", "string", uuid.toString());
        String identity = dManager.registerBc(uuid, "bc1");
        assertEquals("bc1/user1", identity);
        manager.invokeSmartContractFunction("bc1", "sc1", "userF1", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L, "callback-url", 0, "ABC", "");
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(1, dtx.getBlockchainIds().size());
        assertTrue(dtx.getBlockchainIds().contains("bc1"));
        // we remain in the same state
        assertEquals(DistributedTransactionState.STARTED, dtx.getState());
        assertEquals(1, manager.functionInvocations.size());
        assertTrue(manager.functionInvocations.containsKey("bc1"));
        assertEquals(1, manager.functionInvocations.get("bc1").size());

        /* DtxInvoke 2 */
        manager.invokeSmartContractFunction("bc1", "sc2", "userF2", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");
        dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(1, dtx.getBlockchainIds().size());
        assertTrue(dtx.getBlockchainIds().contains("bc1"));
        // we remain in the same state
        assertEquals(DistributedTransactionState.STARTED, dtx.getState());
        assertEquals(1, manager.functionInvocations.size());
        assertTrue(manager.functionInvocations.containsKey("bc1"));
        assertEquals(2, manager.functionInvocations.get("bc1").size());

        /* DtxInvoke 3 */
        identity = dManager.registerBc(uuid, "bc2");
        assertEquals("bc2/user1", identity);
        manager.invokeSmartContractFunction("bc2", "sc3", "userF3", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");
        dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(2, dtx.getBlockchainIds().size());
        assertTrue(dtx.getBlockchainIds().contains("bc1"));
        assertTrue(dtx.getBlockchainIds().contains("bc2"));
        // we remain in the same state
        assertEquals(DistributedTransactionState.STARTED, dtx.getState());
        assertEquals(2, manager.functionInvocations.size());
        assertTrue(manager.functionInvocations.containsKey("bc1"));
        assertEquals(2, manager.functionInvocations.get("bc1").size());
        assertTrue(manager.functionInvocations.containsKey("bc2"));
        assertEquals(1, manager.functionInvocations.get("bc2").size());

    }

    @Test
    void testAwaitingVotesStateToCommit() {
        /* DtxStart */
        AdapterManager adapterManager = generateAdapterManager();
        MockBlockchainManager manager = new MockBlockchainManager(adapterManager);
        DistributedTransactionManager dManager = getDistributedTransactionManager(adapterManager, manager);
        UUID uuid = dManager.startDtx();

        /* DtxInvoke bc1 */
        Parameter uuidParameter = new Parameter("txid", "string", uuid.toString());
        dManager.registerBc(uuid, "bc1");
        dManager.registerBc(uuid, "bc2");
        manager.invokeSmartContractFunction("bc1", "sc1", "userF1", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* DtxInvoke bc2 */
        manager.invokeSmartContractFunction("bc2", "sc2", "userF2", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* DtxCommit */
        dManager.commitDtx(uuid);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.AWAITING_VOTES, dtx.getState());
        assertEquals(DistributedTransactionVerdict.NOT_DECIDED, dtx.getVerdict());
        assertEquals(0, dtx.getYes());
        assertThrows(NotSupportedException.class, () -> dManager.registerBc(uuid, "bc3"));

        /* vote 1 */
        assertTrue(manager.emitVotes(uuid, "bc1", true));
        dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.AWAITING_VOTES, dtx.getState());
        assertEquals(DistributedTransactionVerdict.NOT_DECIDED, dtx.getVerdict());
        assertEquals(1, dtx.getYes());


        /* vote 2 */
        assertTrue(manager.emitVotes(uuid, "bc2", true));
        dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.COMMITTED, dtx.getState());
        assertEquals(DistributedTransactionVerdict.COMMIT, dtx.getVerdict());
        assertEquals(2, dtx.getYes());
        assertThrows(NotSupportedException.class, () -> dManager.registerBc(uuid, "bc3"));

    }

    @Test
    void testAwaitingVotesStateToAbort() {
        /* DtxStart */
        AdapterManager adapterManager = generateAdapterManager();
        MockBlockchainManager manager = new MockBlockchainManager(adapterManager);
        DistributedTransactionManager dManager = getDistributedTransactionManager(adapterManager, manager);
        UUID uuid = dManager.startDtx();

        /* DtxInvoke bc1 */
        Parameter uuidParameter = new Parameter("txid", "string", uuid.toString());
        dManager.registerBc(uuid, "bc1");
        dManager.registerBc(uuid, "bc2");
        manager.invokeSmartContractFunction("bc1", "sc1", "userF1", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* DtxInvoke bc2 */
        manager.invokeSmartContractFunction("bc2", "sc2", "userF2", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* DtxCommit */
        dManager.commitDtx(uuid);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.AWAITING_VOTES, dtx.getState());
        assertEquals(DistributedTransactionVerdict.NOT_DECIDED, dtx.getVerdict());
        assertEquals(0, dtx.getYes());

        /* yes vote */
        assertTrue(manager.emitVotes(uuid, "bc1", true));
        dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.AWAITING_VOTES, dtx.getState());
        assertEquals(DistributedTransactionVerdict.NOT_DECIDED, dtx.getVerdict());
        assertEquals(1, dtx.getYes());

        /* no vote */
        assertTrue(manager.emitVotes(uuid, "bc2", false));
        dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.ABORTED, dtx.getState());
        assertEquals(DistributedTransactionVerdict.ABORT, dtx.getVerdict());
        assertEquals(1, dtx.getYes());
        assertThrows(NotSupportedException.class, () -> dManager.registerBc(uuid, "bc3"));
    }

    @Test
    void testAbortViaError() {
        /* DtxStart */
        AdapterManager adapterManager = generateAdapterManager();
        MockBlockchainManager manager = new MockBlockchainManager(adapterManager);
        DistributedTransactionManager dManager = getDistributedTransactionManager(adapterManager, manager);
        UUID uuid = dManager.startDtx();

        /* DtxInvoke bc1 */
        Parameter uuidParameter = new Parameter("txid", "string", uuid.toString());
        dManager.registerBc(uuid, "bc1");
        dManager.registerBc(uuid, "bc2");
        manager.invokeSmartContractFunction("bc1", "sc1", "userF1", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* DtxInvoke bc2 */
        manager.invokeSmartContractFunction("bc2", "sc2", "userF2", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* SC Error */
       manager.emitAborts(uuid, "bc2");
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.ABORTED, dtx.getState());
        assertEquals(DistributedTransactionVerdict.ABORT, dtx.getVerdict());
        assertEquals(0, dtx.getYes());

        // At this point the distributed transaction manager is not listening to votes anymore.
        /* yes vote */
        assertFalse(manager.emitVotes(uuid, "bc1", true));

        /* yes vote */
        assertFalse(manager.emitVotes(uuid, "bc2", true));
    }

    @Test
    void testAbortViaDtxAbort() {
        /* DtxStart */
        AdapterManager adapterManager = generateAdapterManager();
        MockBlockchainManager manager = new MockBlockchainManager(adapterManager);
        DistributedTransactionManager dManager = getDistributedTransactionManager(adapterManager, manager);
        UUID uuid = dManager.startDtx();

        /* DtxInvoke bc1 */
        Parameter uuidParameter = new Parameter("txid", "string", uuid.toString());
        manager.invokeSmartContractFunction("bc1", "sc1", "userF1", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* DtxInvoke bc2 */
        manager.invokeSmartContractFunction("bc2", "sc2", "userF2", List.of(uuidParameter), List.of(),
                0, "json-rpc", true, 0L,"callback-url", 0, "ABC", "");

        /* SC Error */
        dManager.abortDtx(uuid);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        assertEquals(DistributedTransactionState.ABORTED, dtx.getState());
        assertEquals(DistributedTransactionVerdict.ABORT, dtx.getVerdict());
        assertEquals(0, dtx.getYes());

        // At this point the distributed transaction manager is not listening to votes anymore.
        /* yes vote */
        assertFalse(manager.emitVotes(uuid, "bc1", true));

        /* yes vote */
        assertFalse(manager.emitVotes(uuid, "bc2", true));
    }

    static class MockBlockchainManager extends BlockchainManager {

        Map<String, List<SmartContractFunction>> functionInvocations = new HashMap<>();
        Map<String, List<SmartContractEvent>> eventInvocations = new HashMap<>();
        Map<ImmutablePair<UUID, String>, List<ObservableEmitter<Occurrence>>> abortEventEmitters = new HashMap<>();
        Map<ImmutablePair<UUID, String>, List<ObservableEmitter<Occurrence>>> voteEventEmitters = new HashMap<>();

        public MockBlockchainManager(AdapterManager adapterManager) {
            super(adapterManager);
        }


        @Override
        public CompletableFuture<Transaction> invokeSmartContractFunction(
                final String blockchainIdentifier,
                final String smartContractPath,
                final String functionIdentifier,
                final List<Parameter> inputs,
                final List<Parameter> outputs,
                final double requiredConfidence,
                final long timeoutMillis,
                final String signature,
                final boolean sideEffects) throws BalException {
            log.info("Mock-invoking function: {}.{}.{}", blockchainIdentifier, smartContractPath, functionIdentifier);
            SmartContractFunction function = new SmartContractFunction(functionIdentifier, inputs, outputs);

            if (!functionInvocations.containsKey(blockchainIdentifier)) {
                functionInvocations.put(blockchainIdentifier, new ArrayList<>());
            }

            functionInvocations.get(blockchainIdentifier).add(function);

            return CompletableFuture.completedFuture(new Transaction());
        }

        @Override
        public void invokeSmartContractFunction(
                final String blockchainIdentifier,
                final String smartContractPath,
                final String functionIdentifier,
                final List<Parameter> inputs,
                final List<Parameter> outputs,
                final double requiredConfidence,
                final String callbackBinding,
                final boolean sideEffects,
                final Long nonce,
                final String callbackUrl,
                final long timeoutMillis,
                final String correlationId,
                final String signature) throws BalException {
            this.invokeSmartContractFunction(blockchainIdentifier, smartContractPath,
                    functionIdentifier, inputs, outputs, requiredConfidence, timeoutMillis, signature, sideEffects);
        }

        @Override
        public Observable<Occurrence> subscribeToEvent(String blockchainIdentifier,
                                                       final String smartContractPath,
                                                       final String eventIdentifier,
                                                       final List<Parameter> outputParameters,
                                                       final double degreeOfConfidence,
                                                       final String filter) {
            log.info("Mock-subscribing to event: {}.{}.{} Filter = {}", blockchainIdentifier, smartContractPath, eventIdentifier, filter);
            SmartContractEvent event = new SmartContractEvent(eventIdentifier, outputParameters);
            String uuIdStr = filter.split("==")[1];
            uuIdStr = uuIdStr.substring(1, uuIdStr.length() - 1);
            UUID uuid = UUID.fromString(uuIdStr);

            if (!eventInvocations.containsKey(blockchainIdentifier)) {
                eventInvocations.put(blockchainIdentifier, new ArrayList<>());
            }

            eventInvocations.get(blockchainIdentifier).add(event);
            Observable<Occurrence> observable;
            ImmutablePair<UUID, String> key = ImmutablePair.of(uuid, blockchainIdentifier);

            if ("abortEvent".equals(eventIdentifier)) {

                if (!abortEventEmitters.containsKey(key)) {
                    abortEventEmitters.put(key, new ArrayList<>());
                }

                observable = Observable.create(observableEmitter -> {
                    abortEventEmitters.get(key).add(observableEmitter);
                });

            } else {
                if (!voteEventEmitters.containsKey(key)) {
                    voteEventEmitters.put(key, new ArrayList<>());
                }

                observable = Observable.create(observableEmitter -> {
                    voteEventEmitters.get(key).add(observableEmitter);
                });
            }

            return observable;
        }

        boolean emitAborts(UUID txId, String blockchainIdentifier) {
            log.info("Emitting aborts for the txid: {} and blockchainId: {}", txId, blockchainIdentifier);
            ImmutablePair<UUID, String> key = ImmutablePair.of(txId, blockchainIdentifier);

            if (this.abortEventEmitters.containsKey(key)) {
                this.abortEventEmitters.get(key).forEach(emitter -> emitter.onNext(new Occurrence(List.of(new Parameter("txId", "string", txId.toString())), "")));
                return true;
            }

            return false;
        }

        boolean emitVotes(UUID txId, String blockchainIdentifier, boolean isYes) {
            log.info("Emitting votes for the txid: {} and blockchainId: {}. Vote={}", txId, blockchainIdentifier, isYes);
            ImmutablePair<UUID, String> key = ImmutablePair.of(txId, blockchainIdentifier);
            Parameter txIdPar = new Parameter("txId", "string", txId.toString());
            Parameter votePar = new Parameter("vote", "boolean", String.valueOf(isYes));

            if (this.voteEventEmitters.containsKey(key)) {
                this.voteEventEmitters.get(key).forEach(emitter -> emitter.onNext(new Occurrence(List.of(txIdPar, votePar), "")));
                return true;
            }

            return false;
        }


    }


}