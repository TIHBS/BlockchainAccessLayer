package blockchains.iaas.uni.stuttgart.de;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.List;

import static blockchains.iaas.uni.stuttgart.de.Constants.PF4J_AUTOLOAD_PROPERTY;

@SpringBootApplication
@Log4j2
public class BlockchainAccessLayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockchainAccessLayerApplication.class, args);
	}

	@Bean
	ApplicationRunner loadPlugins(BlockchainPluginManager blockchainPluginManager, @Value("${" + PF4J_AUTOLOAD_PROPERTY + ":false}") String strConf) {
		return args ->  {
			boolean enablePlugins = Boolean.parseBoolean(strConf);
			log.debug("{}={}", PF4J_AUTOLOAD_PROPERTY, enablePlugins);

			if (enablePlugins) {
				log.info("pf4j.autoLoadPlugins=true -> attempting to enable blockchain adapter plugins");
				blockchainPluginManager.startPlugins();
			}
		};

	}

}
