package eventserver.teamwars.command;

import eventserver.teamwars.command.list.*;
import lombok.Getter;
import org.bukkit.permissions.Permission;

public enum SubCommands {

    JOIN(new JoinCommand(), new String [] {"join"}, new Permission("teamwars.join")),
    SET_SLOTS(new SetSlotsCommand(), new String[] {"setslots"}, new Permission("teamwars.admin")),
    SET_GLOBAL_SPAWN(new SetGlobalSpawn(), new String[] {"setglobalspawn"}, new Permission("teamwars.admin")),
    KICK(new KickMemberCommand(), new String[] {"kickmember"}, new Permission("teamwars.admin")),
    SET_GAME_TYPE(new SetGameTypeCommand(), new String[] {"gametype"}, new Permission("teamwars.admin")),
    TP_HERE(new TphereCommand(), new String[] {"tphere"}, new Permission("teamwars.admin"));
    @Getter
    final SubCommand command;
    @Getter
    final String [] aliases;
    @Getter
    final Permission permission;

    SubCommands(SubCommand command, String[] aliases, Permission permission) {
        this.command = command;
        this.aliases = aliases;
        this.permission = permission;
    }
}