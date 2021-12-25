package blockchains.iaas.uni.stuttgart.demo;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import io.reactivex.Observable;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        public BlockchainAdapter getAdapter(String nodeUrl, int avgBlocktime) {
            return new EthereumAdapter(nodeUrl, avgBlocktime);
        }
    }
}
