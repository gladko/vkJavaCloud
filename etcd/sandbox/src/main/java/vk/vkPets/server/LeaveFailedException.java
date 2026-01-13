package vk.vkPets.server;


public class LeaveFailedException extends Exception {

    public LeaveFailedException(NodeData nodeData, Exception cause) {
        super(String.format("Node %s failed to leave", nodeData.getUuid()), cause);
    }
}