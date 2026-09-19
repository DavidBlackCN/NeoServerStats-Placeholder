package io.github.davidblackcn.neoserverstatsplaceholder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.github.davidblackcn.neoserverstatsplaceholder.compat.ForgePlaceholderApiCompat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Administration / debugging command that evaluates an arbitrary placeholder string through Forge
 * PlaceholderAPI: {@code /neoserverstats placeholder <text>}.
 *
 * <p>Forge PlaceholderAPI only ships {@code /placeholderapi}, which lists the registered namespaces
 * and their descriptions but never evaluates anything. Without an evaluation entry point nobody can
 * tell whether a placeholder actually resolves, so this command is the supported way to check it. It
 * is a debugging aid only: it runs the exact same lookup a consumer mod runs and no placeholder
 * implementation depends on it.
 */
public final class PlaceholderDebugCommand {

    /** Vanilla permission level 2 = game masters / operators. */
    private static final int PERMISSION_LEVEL_GAME_MASTERS = 2;

    private static final String ROOT_LITERAL = "neoserverstats";
    private static final String PLACEHOLDER_LITERAL = "placeholder";
    private static final String TEXT_ARGUMENT = "text";

    private PlaceholderDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ROOT_LITERAL)
                .requires(source -> source.hasPermission(PERMISSION_LEVEL_GAME_MASTERS))
                .then(Commands.literal(PLACEHOLDER_LITERAL)
                        .then(Commands.argument(TEXT_ARGUMENT, StringArgumentType.greedyString())
                                .executes(context -> evaluate(
                                        context.getSource(),
                                        StringArgumentType.getString(context, TEXT_ARGUMENT))))));
    }

    private static int evaluate(CommandSourceStack source, String text) {
        // A player sender exercises Forge PlaceholderAPI's player resolution path; the console and
        // RCON exercise the non-player path. Neither is allowed to throw.
        Object context = source.getEntity() == null ? source : source.getEntity();

        String resolved = ForgePlaceholderApiCompat.evaluatePlaceholders(context, text);
        source.sendSuccess(() -> Component.literal(text + " -> " + resolved), false);
        return 1;
    }
}
