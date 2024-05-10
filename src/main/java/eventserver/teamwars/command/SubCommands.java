package eventserver.teamwars.command;

import eventserver.teamwars.command.list.*;
import lombok.Getter;
import org.bukkit.permissions.Permission;

public enum SubCommands {

    JOIN(new JoinCommand(), new String [] {"join"}, null),
    SET_SLOTS(new SetSlotsCommand(), new String[] {"setslots"}, new Permission("teamwars.admin")),
    SET_GLOBAL_SPAWN(new SetGlobalSpawn(), new String[] {"setglobalspawn"}, new Permission("teamwars.admin")),
    KICK(new KickMemberCommand(), new String[] {"kickmember"}, new Permission("teamwars.moder")),
    SET_GAME_TYPE(new SetGameTypeCommand(), new String[] {"gametype"}, new Permission("teamwars.admin")),
    LEAVE(new LeaveCommand(), new String[] {"leave"}, null),
    PAY(new PayCommand(), new String[] {"pay"}, null),
    ADDITIONAL(new AdditionalCommand(), new String[] {"additional"}, new Permission("teamwars.admin")),
    RETURN_INVENTORY(new ReturnInventoryCommand(), new String[] {"returnInv"}, null),
    SET_CENTER_GAME(new SetCenterGameCommand(), new String[] {"setcentergame"}, new Permission("teamwars.admin")),
    SET_BALANCE(new SetBalanceCommand(), new String[] {"setbalance"}, new Permission("teamwars.admin")),
    GET_BALANCE(new GetBalanceCommand(), new String[] {"getbalance"}, new Permission("teamwars.moder")),
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