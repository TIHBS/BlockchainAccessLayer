package blockchains.iaas.uni.stuttgart.de.jsonrpc.model;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import lombok.Data;

import java.util.List;

@Data
public class MemberSignature {
    private String name;
    private boolean isFunction;
    private List<Parameter> parameters;
}
