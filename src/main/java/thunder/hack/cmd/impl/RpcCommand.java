package thunder.hack.cmd.impl;

import thunder.hack.cmd.Command;
import thunder.hack.modules.client.RPC;

public class RpcCommand extends Command {

    public RpcCommand() {
        super("rpc");
    }

    @Override
    public void execute(String[] args) {

        if (args.length == 1) {
            ModuleCommand.sendMessage(".rpc url or .rpc url url");
            return;

        }
        if (args.length == 2) {
            RPC.WriteFile(args[0], "none");
            Command.sendMessage("Большая картинка RPC изменена на " + args[0]);
            return;
        }
        if (args.length >= 2) {
            RPC.WriteFile(args[0], args[1]);
            Command.sendMessage("Большая картинка RPC изменена на " + args[0]);
            Command.sendMessage("Маленькая картинка RPC изменена на " + args[1]);
        }

    }
}
