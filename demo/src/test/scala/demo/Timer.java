package demo;

class Timer {
    private long start = System.nanoTime();

    public void print(String message) {
        long end = System.nanoTime();
        System.out.println(message + " took " + (end - start) / 1_000_000 + " ms");
        start = end;
    }
}
