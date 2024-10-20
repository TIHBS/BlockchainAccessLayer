package blockchains.iaas.uni.stuttgart.de.scip.model.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class ReceiveTxResponse extends AsyncScipResponse {
    @NonNull private String from;
    private long value;
    private String timestamp;

    @Builder
    public ReceiveTxResponse(@NonNull String correlationId, String timestamp, long value, @NonNull String from) {
        super(correlationId);
        this.timestamp = timestamp;
        this.value = value;
        this.from = from;
    }

    @Override
    public String toString() {
        return "ReceiveTxRequest{" +
                "from='" + from + '\'' +
                "correlationId='" + getCorrelationId() + '\'' +
                ", value=" + value +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
