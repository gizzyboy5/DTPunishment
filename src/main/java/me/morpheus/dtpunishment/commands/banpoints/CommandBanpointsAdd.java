package me.morpheus.dtpunishment.commands.banpoints;

import java.util.UUID;

import me.morpheus.dtpunishment.DTPunishment;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.penalty.BanpointsPunishment;
import me.morpheus.dtpunishment.utils.Util;

public class CommandBanpointsAdd implements CommandExecutor {

	private DataStore dataStore = DTPunishment.getDataStore();

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		User user = args.<User>getOne("player").get();
		UUID uuid = user.getUniqueId();
		String name = user.getName();
		int amount = args.<Integer>getOne("amount").get();

		if(amount < 0) {
			amount = 0;
		}
		dataStore.addBanpoints(uuid, amount);

		int total = dataStore.getBanpoints(uuid);

		if (user.isOnline()) {
			user.getPlayer().get().sendMessage(Util.withWatermark(TextColors.RED,
					String.format("%d banpoints have been added; you now have %d", amount, total)));
		}

		Text adminMessage = Util.withWatermark(TextColors.RED, String
				.format("%s has added %d banpoint(s) to %s; they now have %d", src.getName(), amount, name, total));

		if (src instanceof ConsoleSource)
			src.sendMessage(adminMessage);

		for (Player p : Sponge.getServer().getOnlinePlayers()) {
			if (p.hasPermission("dtpunishment.staff.notify") || p == src) {
				p.sendMessage(adminMessage);
			}
		}

		BanpointsPunishment.check(uuid, total);

		return CommandResult.success();
	}
}
