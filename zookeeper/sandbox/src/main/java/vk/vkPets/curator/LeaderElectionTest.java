package vk.vkPets.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderElectionTest {
    static final String LEADER_ROOT = "/vk_test_leader";
    private static final String serviceName = "xxx";

    static class ElectingNode extends ServerNode {
        private final AtomicInteger leaderCount = new AtomicInteger();

        public ElectingNode(String serviceName) {
            super(serviceName);
        }

        public void startLeaderElection() throws Exception {
            String path = LEADER_ROOT + "/" + serviceName;

            LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {
                public void takeLeadership(CuratorFramework client) throws Exception {
                    // this callback will get called when you are the leader
                    // do whatever leader work you need to and only exit
                    // this method when you want to relinquish leadership

                    final int waitSeconds = (int) (5 * Math.random()) + 1;

                    String name = "Node_" + port;

                    System.out.println(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
                    System.out.println(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
                    } catch (InterruptedException e) {
                        System.err.println(name + " was interrupted.");
                        Thread.currentThread().interrupt();
                    } finally {
                        System.out.println(name + " relinquishing leadership.\n");
                    }
                }
            };

            LeaderSelector selector = new LeaderSelector(curatorClient, path, listener);
            selector.autoRequeue();  // not required, but this is behavior that you will probably expect
            selector.start();
        }
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 5; i++) {
            ElectingNode node = new ElectingNode(serviceName);
            node.register("test_" + i);
            node.startLeaderElection();
        }

        Thread.sleep(Long.MAX_VALUE);
    }
}
