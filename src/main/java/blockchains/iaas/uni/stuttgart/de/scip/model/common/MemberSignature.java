package blockchains.iaas.uni.stuttgart.de.scip.model.common;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignature {
    @NonNull private String name;
    private boolean isFunction;
    @NonNull private List<Parameter> parameters;
}
