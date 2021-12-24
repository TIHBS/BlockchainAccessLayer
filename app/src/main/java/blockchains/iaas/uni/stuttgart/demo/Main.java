package blockchains.iaas.uni.stuttgart.de;

import blockchains.iaas.uni.stuttgart.de.demo.IExtensionInterface;
import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        final PluginManager pluginManager = new DefaultPluginManager() {

            protected ExtensionFinder createExtensionFinder() {
                DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
                extensionFinder.addServiceProviderExtensionFinder(); // to activate "HowdyGreeting" extension
                return extensionFinder;
            }

        };

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<IExtensionInterface> greetings = pluginManager.getExtensions(IExtensionInterface.class);

        for (IExtensionInterface greeting : greetings) {
            System.out.println(">>> " + greeting.getVersion());
        }
    }
}
