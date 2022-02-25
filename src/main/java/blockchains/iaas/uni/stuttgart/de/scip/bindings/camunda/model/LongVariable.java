package blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model;

import lombok.Getter;

@Getter
public class LongVariable extends Variable {
    private final long value;

    public LongVariable(long value) {
        super("Number");
        this.value = value;
    }
}
