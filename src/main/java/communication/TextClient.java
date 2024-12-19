package communication;

import com.google.gson.Gson;
import core.Data;
import messages.*;
import utils.IPString;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.CRC32;


public class TextClient {
    private Data data;

    public TextClient(Data data) {
        this.data = data;
    }

    public void send_to(String destination_ip, String txt) {
        int id = data.get_next_free_ip();
        if (id == -1) {
            data.gui.logMessage("No free ids. Please wait until a message gets acknowledged.");
            return;
        }

        Optional<Link> link = data.routing_table.stream().filter(l -> l.getDESTINATION() == IPString.int_from_string(destination_ip)).findFirst();
        link.ifPresentOrElse(
            value -> {
                try (Socket socket = new Socket(IPString.string_from_int(value.getGATEWAY()), data.port)) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    Gson gson = new Gson();
                    data.reserve_id(id);
                    Body b = new TextBody(txt, id);
                    String bodystring = gson.toJson(b);
                    System.out.println(bodystring);
                    CRC32 crc = new CRC32();
                    crc.update(bodystring.getBytes(StandardCharsets.UTF_8));

                    Header h = Header.TEXTHEADER;
                    h.SIZE = (short) txt.length();
                    h.set_destination_ip(destination_ip);
                    h.SENDER_IP = data.get_own_IP_int();
                    h.CHECKSUM = (int) crc.getValue();

                    Message m = new Message(h, b);

                    out.println(gson.toJson(m));

                    data.gui.logMessage("Sending to " + destination_ip + ":\n " +
                            " -> " + "\"" + txt + "\"" + "\n " +
                            " -> " + "id: " + id);

                } catch (IOException e) {
                    System.err.println("I/O-Fehler beim Verbinden zum Server: " + e.getMessage());
                }
            },
            () -> data.gui.logMessage("No Connection to send Message")
        );

    }
}
