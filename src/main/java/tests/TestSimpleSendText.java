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

        TextClient tc = new TextClient();
        List<Link> rt = new ArrayList<>();

        Server s = new Server(rt, "127.0.0.1");
        Thread t = new Thread(s);
        t.start();
        tc.send_to("127.0.0.1", "hello world");
    }
}
