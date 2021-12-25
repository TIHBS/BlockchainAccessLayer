package blockchains.iaas.uni.stuttgart.de.api;

import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import org.pf4j.ExtensionPoint;

public interface IAdapterExtenstion extends ExtensionPoint {
    BlockchainAdapter getAdapter(String nodeUrl, int avgBlocktime);
}
