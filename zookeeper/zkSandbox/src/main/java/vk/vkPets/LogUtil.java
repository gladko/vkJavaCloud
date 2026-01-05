package vk.vkPets;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class LogUtil {
    public static void log(Object... msg) {
        String txt = Arrays.stream(msg)
                .map(o -> o != null ? o.toString() : "NULL")
                .collect(Collectors.joining(" "));
        System.out.println(new Date() + ": " + txt);
    }
}
