package communication;


import lombok.Getter;
import lombok.Setter;
import utils.IPString;

@Getter
@Setter
public class Link {
    int DESTINATION;
    int GATEWAY;
    int HOP_COUNT;

    public Link(int destination, int gateway, int hopCount) {
        this.DESTINATION = destination;
        this.GATEWAY = gateway;
        this.HOP_COUNT = hopCount;
    }


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
                "  " + "gateway: " + IPString.string_from_int(GATEWAY) + "\n" +
                "  " + "hop_count: " + HOP_COUNT + "\n" +
                "  " + "}\n";
    }
}
