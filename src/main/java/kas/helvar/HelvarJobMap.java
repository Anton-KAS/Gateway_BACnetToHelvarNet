package kas.helvar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public enum HelvarJobMap {
    HELVAR_JOB_LIST;

    private final Map<String, LinkedList<String>> jobMap;

    HelvarJobMap() {
        this.jobMap = new HashMap<>();
    }

    public void addJob(String host, String message) {
        LinkedList<String> hostJobList = jobMap.get(host);
        if (hostJobList == null) {
            hostJobList = new LinkedList<>();
            hostJobList.addLast(message);
            jobMap.put(host, hostJobList);
        } else {
            hostJobList.addLast(message);
        }
    }

    public String getNextJob(String host) {
        LinkedList<String> hostJobs = jobMap.get(host);
        if (hostJobs == null) {
            return null;
        }
        return hostJobs.getFirst();
    }
}
