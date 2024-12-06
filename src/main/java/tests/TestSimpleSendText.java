package tests;

import communication.Link;
import core.RoutingClient;
import core.Server;
import core.TextClient;
import utils.IPString;

import java.util.ArrayList;
import java.util.List;

public class TestSimpleSendText {
    public static void main(String[] args){

        List<Link> rtc = new ArrayList<>();
        Link l = new Link(
                IPString.string_to_ip("127.0.0.1"),
                IPString.string_to_ip("255.255.255.0"),
                IPString.string_to_ip("127.0.0.1"),
                1
        );
        rtc.add(l);
        TextClient tc = new TextClient(rtc);

        List<Link> rts = new ArrayList<>();
        Server s = new Server(rts, "127.0.0.1");
        Thread t = new Thread(s);
        t.start();
        tc.send_to("127.0.0.1", "hello world");
    }
}
