package net.kunmc.lab.testmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;

public class TestCommand {
    private static String message = "Hello!";

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        // 今回作成するコマンドのルートとなる"test"コマンドノードを作成,登録する.
        LiteralCommandNode<CommandSource> testCommand = dispatcher.register(Commands.literal("test")
                .requires(cs -> cs.hasPermissionLevel(4)));

        // testコマンドノードにプレイヤー引数を追加する.
        // これで"/test <targets>"コマンドが使えるようになる.
        testCommand.addChild(Commands.argument("targets", EntityArgument.players())
                .executes(ctx -> {
                    Collection<ServerPlayerEntity> entities = EntityArgument.getPlayers(ctx, "targets");
                    entities.forEach(p -> {
                        p.sendMessage(new StringTextComponent(message), Util.DUMMY_UUID);
                    });
                    return 1;
                })
                .build()); // registerやthenは内部で"build()してaddChild()しているが,ここでは直接addChild()を呼ぶためbuild()する必要がある.

        // testコマンドノードの下にconfigコマンドノードを追加する.
        // これで"/test config message [value]"コマンドが使えるようになる.
        testCommand.addChild(TestConfigCommand.create());

        // エイリアスの登録を行う.
        // こちらでも独自に権限の設定を行う必要がある.
        dispatcher.register(Commands.literal("testAlias")
                .requires(cs -> cs.hasPermissionLevel(4))
                .redirect(testCommand));
    }

    private static class TestConfigCommand {
        static CommandNode<CommandSource> create() {
            CommandNode<CommandSource> configCommand = Commands.literal("config").build();

            configCommand.addChild(Commands.literal("message")
                    .then(Commands.argument("string", StringArgumentType.string())
                            .executes(ctx -> {
                                message = ctx.getArgument("string", String.class);
                                ctx.getSource().sendFeedback(new StringTextComponent("messageの値を" + message + "に変更しました."), false);
                                return 1;
                            })
                    )
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(new StringTextComponent("messageの値は" + message + "です."), true);
                        return 1;
                    })
                    .build());

            return configCommand;
        }
    }
}

