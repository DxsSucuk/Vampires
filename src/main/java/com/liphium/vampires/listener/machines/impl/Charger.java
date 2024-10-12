package com.liphium.vampires.listener.machines.impl;

import com.liphium.vampires.Vampires;
import com.liphium.vampires.game.state.IngameState;
import com.liphium.vampires.game.team.impl.VampireTeam;
import com.liphium.vampires.listener.machines.Machine;
import com.liphium.vampires.util.LocationAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class Charger extends Machine {

    private final ArmorStand stand;

    int bloodLevel = 0;

    public Charger(Location location) {
        super(location, false);

        stand = location.getWorld().spawn(location.clone().add(0, -1.5, 0), ArmorStand.class);

        stand.setCustomNameVisible(true);
        stand.customName(Vampires.S_BRACKET
                .append(Component.text(VampireTeam.getLongString(0), NamedTextColor.GRAY))
                .append(Vampires.E_BRACKET));
        stand.setGravity(false);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setRemoveWhenFarAway(false);
    }

    int tickCount = 0, rechargeTicks = 0, chargeTicks = 0;

    @Override
    public void tick() {
        if (tickCount++ >= 7) {
            tickCount = 0;

            // Recharge charger
            Location origin = LocationAPI.getLocation("Cell");
            IngameState state = (IngameState) Vampires.getInstance().getGameManager().getCurrentState();

            int amount = 0;
            for (Player player : Vampires.getInstance().getGameManager().getTeamManager().getTeam("Humans").getPlayers()) {
                if (player.getLocation().distance(origin) <= 7) {
                    amount += 1;
                } else state.prison.remove(player);
            }

            if (rechargeTicks++ >= 4) {
                rechargeTicks = 0;
                bloodLevel += amount;
            }

            // Recharge vampires
            if (chargeTicks++ >= 1) {
                chargeTicks = 0;

                VampireTeam team = (VampireTeam) Vampires.getInstance().getGameManager().getTeamManager().getTeam("Vampires");
                for (Player player : team.getPlayers()) {
                    if (player.getLocation().distance(location) <= 1.5 && bloodLevel > 0) {
                        team.bloodLevel.put(player, team.bloodLevel.get(player) + 1);
                        bloodLevel--;
                        break;
                    }
                }
            }

            stand.customName(Component.text("§8[§7" + VampireTeam.getLongString(bloodLevel) + "§8]"));
        }
    }

    @Override
    public void destroy() {
        stand.remove();
    }
}
