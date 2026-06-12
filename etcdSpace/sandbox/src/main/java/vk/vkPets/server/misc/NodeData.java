package vk.vkPets.server.misc;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public record NodeData(UUID uuid, String host) {
    public NodeData() throws UnknownHostException {
        this(UUID.randomUUID(), InetAddress.getLocalHost().getHostAddress());
    }

    public NodeData(UUID uuid, String host) {
        this.uuid = uuid;
        this.host = host;
    }
}