package communication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import utils.IPString;

@Getter
@Setter
@AllArgsConstructor
public class Link {
    int DESTINATION;
    int NETMASK;
    int GATEWAY;
    int HOP_COUNT;


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Link link = (Link) obj;

        return DESTINATION == link.DESTINATION;  // Compare DESTINATION values
    }

    public void incrementHopCount() {
        HOP_COUNT++;
    }

    public String toString(){
        return "Link{" + "\n" +
                "  " + "destination: " + IPString.string_from_int(DESTINATION) + "\n" +
                "  " + "netmask: " + IPString.string_from_int(NETMASK) + "\n" +
                "  " + "gateway: " + IPString.string_from_int(GATEWAY) + "\n" +
                "  " + "hop_count: " + HOP_COUNT + "\n" +
                "  " + "}\n";
    }
}
