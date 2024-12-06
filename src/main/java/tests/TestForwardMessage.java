package tests;

import communication.Link;
import core.Server;
import core.TextClient;
import utils.IPString;

import java.util.ArrayList;
import java.util.List;

public class TestForwardMessage {
    public static void main(String[] args){

        List<Link> rt1 = new ArrayList<>();
        Link l = new Link(IPString.int_from_string("127.0.0.3"),
                IPString.int_from_string("225.225.225.0"),
                IPString.int_from_string("127.0.0.3"),
                1);
        rt1.add(l);
        Server forwarder = new Server(rt1, "127.0.0.2");
        Thread t = new Thread(forwarder);
        t.start();

        List<Link> rt2 = new ArrayList<>();
        Server receiver = new Server(rt2, "127.0.0.3");
        Thread t2 = new Thread(receiver);
        t2.start();

        List<Link> rt3 = new ArrayList<>();
        Link l2 = new Link(IPString.int_from_string("127.0.0.3"),
                IPString.int_from_string("225.225.225.0"),
                IPString.int_from_string("127.0.0.2"),
                1);
        rt3.add(l2);
        TextClient sender = new TextClient(rt3);
        sender.send_to("127.0.0.3", "hello world");
    }
}
