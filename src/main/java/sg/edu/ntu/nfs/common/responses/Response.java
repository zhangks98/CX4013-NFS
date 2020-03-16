package sg.edu.ntu.nfs.common.responses;

import sg.edu.ntu.nfs.common.Serializer;
import sg.edu.ntu.nfs.common.values.Value;

import java.util.List;

public interface Response extends Serializer {
    /**
     * Get the response status.
     *
     * @return response status.
     */
    ResponseStatus getStatus();

    /**
     * Get all values in the repsonse.
     *
     * @return values.
     */
    List<Value> getValues();
}
