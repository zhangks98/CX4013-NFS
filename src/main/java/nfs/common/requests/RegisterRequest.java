package nfs.common.requests;

import nfs.common.values.Int32;
import nfs.common.values.Str;

public class RegisterRequest extends AbstractRequest {
    RegisterRequest(RequestId id) {
        super(id, RequestName.REGISTER);
    }

    /**
     * Clients subscribe its interest to observe {@link FileUpdatedCallback} for specified file
     * within the monitor interval.
     *
     * @param path            the file path
     * @param monitorInterval time interval before server no longer updates the client,
     *                        measured in seconds.
     */
    public RegisterRequest(String path, int monitorInterval) {
        super(RequestName.REGISTER);
        addParam(new Int32(monitorInterval));
        addParam(new Str(path));
    }

    public int getMonitorInterval() {
        return (int) getParam(0).getVal();
    }

    public void setMonitorInterval(int monitorInterval) {
        setParam(0, new Int32(monitorInterval));
    }

    public String getPath() {
        return (String) getParam(1).getVal();
    }

    public void setPath(String path) {
        setParam(1, new Str(path));
    }
}
