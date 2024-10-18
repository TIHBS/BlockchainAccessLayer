package blockchains.iaas.uni.stuttgart.de.scip.model;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class MemberSignature {
    @NonNull private String name;
    private boolean isFunction;
    @NonNull private List<Parameter> parameters;
}
