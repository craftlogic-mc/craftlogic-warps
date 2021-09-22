package ru.craftlogic.warps;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import ru.craftlogic.api.world.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Warp {
    private final String name;
    private final Set<UUID> invites;
    private Location location;
    private UUID owner;
    private boolean pub;
    private float price;

    public Warp(String name, JsonObject data) {
        this(name,
            Location.deserialize(JsonUtils.getInt(data, "dim", 0), JsonUtils.getJsonObject(data, "loc")),
            UUID.fromString(JsonUtils.getString(data, "owner", "")),
            JsonUtils.getBoolean(data, "public", true),
            JsonUtils.getFloat(data, "price", -1F),
            new HashSet<>()
        );
        JsonArray invites = JsonUtils.getJsonArray(data, "invites", new JsonArray());
        int i = 0;
        for (JsonElement element : invites) {
            UUID invite = UUID.fromString(JsonUtils.getString(element, "invites[" + (i++) + "]"));
            this.invites.add(invite);
        }
    }

    public Warp(String name, Location location, UUID owner, boolean pub, float price, Set<UUID> invites) {
        this.name = name;
        this.invites = invites;
        this.location = location;
        this.owner = owner;
        this.pub = pub;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isPublic() {
        return pub;
    }

    public void setPublic(boolean pub) {
        this.pub = pub;
    }

    public Set<UUID> getInvites() {
        return invites;
    }

    public boolean isInvited(UUID user) {
        return owner.equals(user) || invites.contains(user);
    }

    public boolean addInvited(UUID user) {
        return invites.add(user);
    }

    public boolean removeInvited(UUID user) {
        return invites.remove(user);
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isForSale() {
        return price >= 0;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("owner", owner.toString());
        obj.addProperty("public", pub);
        obj.addProperty("price", price);
        obj.addProperty("dim", location.getDimensionId());
        obj.add("loc", location.serialize());
        JsonArray invites = new JsonArray();
        for (UUID invite : this.invites) {
            invites.add(invite.toString());
        }
        obj.add("invites", invites);
        return obj;
    }
}
