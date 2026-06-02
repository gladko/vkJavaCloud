package vk.vkPets;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;

import java.net.HttpURLConnection;
import java.net.URL;


// Tried to start toxiproxy with `docker run -p 8474:8474 -p 12379:12379 -d --name toxiproxy shopify/toxiproxy`
//      Can access to toxiproxy control port
//      but etcd-proxy is not available.
public class ToxiproxyTest {


//    @Test
//    public void testToxi() throws Exception {
    public static void main(String[] args) throws Exception {
        ToxiproxyClient toxiproxyClient = new ToxiproxyClient("localhost", 8474);
        Proxy proxy = toxiproxyClient.createProxy("etcd-proxy", "0.0.0.0:12379", "localhost:2379");
        proxy.toxics().latency("latency", ToxicDirection.UPSTREAM, 3000);
        System.out.println("created proxy with toxic");

        final URL url = new URL("http://localhost:12379/version");
        HttpURLConnection etcdRequest = (HttpURLConnection)  url.openConnection();
        System.out.println("etcdRequest status: " + etcdRequest.getResponseCode());

        Thread.sleep(Long.MAX_VALUE);
    }
}
