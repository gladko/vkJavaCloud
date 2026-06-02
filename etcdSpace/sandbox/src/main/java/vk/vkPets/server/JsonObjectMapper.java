package vk.vkPets.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonObjectMapper {

    public static ObjectMapper INSTANCE = new ObjectMapper();

    public static NodeData read(String txt) throws JsonProcessingException {
        return JsonObjectMapper.INSTANCE.readValue(txt, NodeData.class);
    }

    public static String write(NodeData nodeData) throws JsonProcessingException {
        return JsonObjectMapper.INSTANCE.writeValueAsString(nodeData);
    }
}