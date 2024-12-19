package messages;

import lombok.ToString;

import java.util.List;

@ToString(includeFieldNames = true)

public class RoutingBody extends Body{
    private List<Link> ROUTING_TABLE;

    public RoutingBody(List<Link> rt) {
        ROUTING_TABLE = rt;
    }

}
