package jFaaS.utils;

public class PairResult<String, Long> {

    private String result;

    private Long RTT;

    public PairResult(String result, Long RTT) {
        this.result = result;
        this.RTT = RTT;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getRTT() {
        return RTT;
    }

    public void setRTT(Long RTT) {
        this.RTT = RTT;
    }
}
