package blockchains.iaas.uni.stuttgart.de.api;

import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import org.pf4j.ExtensionPoint;

import java.util.Map;

public interface IAdapterExtenstion extends ExtensionPoint {
    BlockchainAdapter getAdapter(Map<String, String> parameters);

    String getBlockChainId();
}
