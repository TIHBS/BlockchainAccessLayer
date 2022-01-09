package blockchains.iaas.uni.stuttgart.demo;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.api.utils.PoWConfidenceCalculator;
import com.neemre.btcdcli4j.core.BitcoindException;
import com.neemre.btcdcli4j.core.CommunicationException;
import com.neemre.btcdcli4j.core.client.BtcdClient;
import com.neemre.btcdcli4j.core.client.BtcdClientImpl;
import com.neemre.btcdcli4j.daemon.BtcdDaemon;
import com.neemre.btcdcli4j.daemon.BtcdDaemonImpl;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import java.util.Map;

public class BitcoinPlugin extends Plugin {
    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper
     */
    public BitcoinPlugin(PluginWrapper wrapper) {
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
    public static class BitcoinPluginImpl implements IAdapterExtenstion {

        @Override
        public BlockchainAdapter getAdapter(Map<String, String> parameters) {
            // TODO: Create blockchains.iaas.uni.stuttgart.demo.BitcoinConnectionProfile object from parameters
            BitcoinConnectionProfile gateway = new BitcoinConnectionProfile();
            final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            final CloseableHttpClient httpProvider = HttpClients.custom().setConnectionManager(connManager).build();

            try {
                final BtcdClient client = new BtcdClientImpl(httpProvider, gateway.getAsProperties());
                final BtcdDaemon daemon = new BtcdDaemonImpl(client);
                final BitcoinAdapter result = new BitcoinAdapter(client, daemon);
                final PoWConfidenceCalculator cCalc = new PoWConfidenceCalculator();
                cCalc.setAdversaryRatio(gateway.getAdversaryVotingRatio());
                result.setConfidenceCalculator(cCalc);
                return result;
            } catch (BitcoindException e) {
                e.printStackTrace();
            } catch (CommunicationException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String getBlockChainId() {
            return "bitcoin";
        }

    }
}
