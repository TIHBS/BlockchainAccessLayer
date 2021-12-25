package blockchains.iaas.uni.stuttgart.de.management;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import org.pf4j.*;

import java.nio.file.Paths;
import java.util.List;

public class BlockchainPluginManager {

    String pluginPath = "/home/ash/software/apache-tomcat-8.5.73/plugins";
    private PluginManager pluginManager = null;
    private static BlockchainPluginManager instance = null;

    private BlockchainPluginManager() {
        this.pluginManager = new DefaultPluginManager(Paths.get(pluginPath)) {
            //
            @Override
            protected PluginLoader createPluginLoader() {
                // load only jar plugins
                return new JarPluginLoader(this);
            }

            @Override
            protected PluginDescriptorFinder createPluginDescriptorFinder() {
                // read plugin descriptor from jar's manifest
                return new ManifestPluginDescriptorFinder();
            }
        };

        // TODO: Create plugin management rest api
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }

    public static BlockchainPluginManager getInstance() {
        if (instance == null) {
            instance = new BlockchainPluginManager();
        }
        return instance;
    }

    public List<IAdapterExtenstion> getExtensions() {
        return this.pluginManager.getExtensions(IAdapterExtenstion.class);
    }

}
