package core;

import messages.Link;
import utils.IPString;

import java.util.Arrays;
import java.util.List;

public class Data {
    private final int own_ip;
    public final int port;
    public List<Link> routing_table;
    public appGUI gui;
    private boolean[] msg_ids = new boolean[255];


    public Data(int own_ip, List<Link> routing_table, appGUI gui) {
        this.own_ip = own_ip;
        this.port = 8080;
        this.routing_table = routing_table;
        this.gui = gui;

        Link mylink = new Link(own_ip, own_ip, 0);
        this.routing_table.add(mylink);

        Arrays.fill(msg_ids, true);
    }

    public String get_own_IP_String(){
        return IPString.string_from_int(own_ip);
    }

    public int get_own_IP_int(){
        return own_ip;
    }

    public int get_next_free_ip(){
        for(int i = 0; i < msg_ids.length; i++){
            if(msg_ids[i]){
                return i;
            }
        }
        return -1;
    }

    public void reserve_id(int index){
        msg_ids[index] = false;
    }
    public void free_id(int index){
        msg_ids[index] = true;
    }
}
