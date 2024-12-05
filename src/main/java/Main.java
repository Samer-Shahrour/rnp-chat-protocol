import communication.Link;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Link first_link = new Link(23, 0, 34, 3);
        List<Link> routing_table = new ArrayList<>();
        routing_table.add(first_link);
        first_link = new Link(23, 0, 34, 3);
        routing_table.add(first_link);


        //Server s = new Server(routing_table);
        //Thread thread = new Thread(s);
        //thread.start();

        Client c = new Client(true, routing_table);
        c.send();

        //Client c = new Client();
        //c.send();


        //thread.join();
    }
}