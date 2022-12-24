package blockchains.iaas.uni.stuttgart.de.models;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PendingTransaction {
    private String blockchainIdentifier;
    private String correlationIdentifier;
    private List<Parameter> inputs;
    private List<Parameter> outputs;
    private List<String> signers;
    private List<String> typeArguments;
    private double requiredConfidence;
    private String callbackUrl;
    private String signature;
    private long minimumNumberOfSignatures;
    private List<String> signatures;
    private String functionIdentifier;
    private String smartContractPath;
    private long timeoutMillis;
}
