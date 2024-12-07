package blockchains.iaas.uni.stuttgart.de.tccsci.model.exception;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;

public class IllegalProtocolStateException extends BalException {
    public IllegalProtocolStateException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return -32300;
    }
}
