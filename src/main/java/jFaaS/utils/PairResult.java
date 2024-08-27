package jFaaS.utils;

public class PairResult {

    private String result;

    private Long rtt;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getRtt() {
        return rtt;
    }

    public void setRtt(Long rtt) {
        this.rtt = rtt;
    }

    @SuppressWarnings("unused") // needed for serialization
    public PairResult() {
    }

    public PairResult(String result, Long RTT) {
        this.result = result;
        this.rtt = RTT;
    }

    @Override
    public java.lang.String toString() {
        return this.result + " " + this.rtt;
    }
}
