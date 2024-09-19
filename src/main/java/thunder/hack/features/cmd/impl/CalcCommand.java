package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CalcCommand extends Command {
    public CalcCommand() {
        super("calc");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("count", StringArgumentType.string()).executes(context -> {
            String expression = context.getArgument("count", String.class);
            try {
                sendMessage(evaluateExpression(expression));
                return SINGLE_SUCCESS;
            }catch (Exception e){
                sendMessage("Try use operators: + - m(*) d(/)");
            }
            return -1;
        }));
    }

    public static String evaluateExpression(String expression) {
        char operator = 0;
        int operand1 = 0;
        int operand2 = 0;
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch == '+' || ch == '-' || ch == 'm' || ch == 'd') {
                operator = ch;
                operand1 = Integer.parseInt(expression.substring(0, i));
                operand2 = Integer.parseInt(expression.substring(i + 1));
                break;
            }
        }

        return switch (operator) {
            case '+' -> String.valueOf(operand1 + operand2);
            case '-' -> String.valueOf(operand1 - operand2);
            case 'm' -> String.valueOf(operand1 * operand2);
            case 'd' -> String.valueOf(operand1 / operand2);
            default -> throw new IllegalArgumentException("Wrong");
        };
    }
}