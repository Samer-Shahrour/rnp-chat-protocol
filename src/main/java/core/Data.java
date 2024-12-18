package core;

import messages.Link;
import utils.IPString;

import java.util.List;

public class Data {
    private final int own_ip;
    public final int port;
    public List<Link> routing_table;
    public appGUI gui;


    public Data(int own_ip, List<Link> routing_table, appGUI gui) {
        this.own_ip = own_ip;
        this.port = 8080;
        this.routing_table = routing_table;
        this.gui = gui;
    }

    public String getIPString(){
        return IPString.string_from_int(own_ip);
    }

    public int getIPint(){
        return own_ip;
    }
}
