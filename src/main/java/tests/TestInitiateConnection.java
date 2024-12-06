package tests;

import communication.Link;
import core.RoutingClient;
import core.Server;
import utils.IPString;

import java.util.ArrayList;
import java.util.List;

public class TestInitiateConnection {public static void main(String[] args){

    Link l = new Link(IPString.string_to_ip("127.0.0.1"),
            IPString.string_to_ip("225.225.225.0"),
            IPString.string_to_ip("127.0.0.1"),
            0);
    List<Link> rt = new ArrayList<Link>();
    rt.add(l);
    RoutingClient rc = new RoutingClient(rt);

    List<Link> rt2 = new ArrayList<Link>();
    Server s = new Server(rt2, "127.0.0.2");
    Thread t = new Thread(s);




    t.start();
    rc.initiate_connection("127.0.0.2");
    }
}
