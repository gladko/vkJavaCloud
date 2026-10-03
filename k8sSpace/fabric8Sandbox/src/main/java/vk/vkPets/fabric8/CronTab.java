package vk.vkPets.fabric8;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;

public class CronTab extends CustomResource<CronTabSpec, Void> implements Namespaced {
}