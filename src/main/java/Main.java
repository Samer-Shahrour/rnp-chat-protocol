public class Main {
    public static void main(String[] args) throws InterruptedException {

        Server s = new Server();
        Thread thread = new Thread(s);
        thread.start();

        Client c = new Client();
        c.send();


        thread.join();
    }
}