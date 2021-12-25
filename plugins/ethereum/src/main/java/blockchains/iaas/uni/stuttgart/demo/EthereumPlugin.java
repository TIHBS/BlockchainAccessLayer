package blockchains.iaas.uni.stuttgart.demo;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import java.util.Map;

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
        public BlockchainAdapter getAdapter(Map<String, String> parameters) {
            String nodeUrl = parameters.get("nodeUrl");
            int averageBlockTimeSeconds = Integer.parseInt(parameters.get("averageBlockTimeSeconds"));
            return new EthereumAdapter(nodeUrl, averageBlockTimeSeconds);
        }

        @Override
        public String getBlockChainId() {
            return "ethereum";
        }
    }
}
