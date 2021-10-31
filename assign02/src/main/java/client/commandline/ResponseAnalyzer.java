package client.commandline;

import client.model.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResponseAnalyzer {

    private ArrayList<Long> data;

    public ResponseAnalyzer(ConcurrentLinkedQueue<Request> input) {
        ArrayList<Long> data = new ArrayList<>();
        input.forEach(r -> data.add(r.getResponseTime()));
        Collections.sort(data);
        this.data = data;
    }

    public double getMean() {
        return this.data.stream().mapToDouble(r -> r).average().orElse(0);
    }

    public double getMedian(){
        if(data.size() % 2 == 1) {
            return data.get(data.size() / 2);
        }
        return (data.get(data.size()/2) + data.get(data.size()/2-1)) / 2;
    }

    public long getP99() {
        return data.get((int) Math.ceil(data.size() * 0.99));
    }

    public long getMax() {
        return data.get(data.size()-1);
    }

}
