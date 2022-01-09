package blockchains.iaas.uni.stuttgart.demo;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import java.util.Map;

public class FabricPlugin extends Plugin {
    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper
     */
    public FabricPlugin(PluginWrapper wrapper) {
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
    public static class FabricAdapterImpl implements IAdapterExtenstion {

        @Override
        public FabricAdapter getAdapter(Map<String, String> parameters) {
            // TODO: Create read blockchainId from parameters
            String blockchainId = "";
            return FabricAdapter.builder()
                    .blockchainId(blockchainId)
                    .build();
        }

        @Override
        public String getBlockChainId() {
            return "fabric";
        }

    }
}
