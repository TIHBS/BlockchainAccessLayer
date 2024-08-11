package blockchains.iaas.uni.stuttgart.de;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class BlockchainAccessLayerApplication {

	public static void main(String[] args) {
		// Required to load the plugins at startup
		if (Boolean.getBoolean("enablePluginsAtStart")) {
			BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
			blockchainPluginManager.startPlugins();
			List<PluginWrapper> plugins = blockchainPluginManager.getPlugins(PluginState.STARTED);

			for (PluginWrapper pluginWrapper : plugins) {
				blockchainPluginManager.registerConnectionProfileSubtypeClass(pluginWrapper.getPluginId());
			}
		}
		SpringApplication.run(BlockchainAccessLayerApplication.class, args);
	}

}
