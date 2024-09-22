package game;

public class Averager {
    private double sum = 0;
    private int elements = 0;

    public void push(Number num) {
        sum += num.doubleValue();
        elements += 1;
    }

    public double getAverage() {
        return sum / elements;
    }

}
