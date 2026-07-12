package cn.aetheris.yuki.functionality.code;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.player.PlayerData;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.InstructionSet;
import com.ql.util.express.config.QLExpressRunStrategy;
import com.ql.util.express.config.whitelist.CheckerFactory;

import java.util.*;

public class ExpressCommandInstance {
    static {
        QLExpressRunStrategy.setCompileWhiteCheckerList(Arrays.asList(
                CheckerFactory.assignable(Date.class),
                CheckerFactory.assignable(Random.class),
                CheckerFactory.assignable(List.class),
                CheckerFactory.assignable(String.class),
                CheckerFactory.assignable(Double.class),
                CheckerFactory.assignable(Float.class),
                CheckerFactory.assignable(Character.class),
                CheckerFactory.assignable(Integer.class),
                CheckerFactory.assignable(System.class),
                CheckerFactory.assignable(ExpressAPI.class)
        ));
        QLExpressRunStrategy.setForbidInvokeSecurityRiskMethods(true);
        QLExpressRunStrategy.setMaxArrLength(100);
    }

    DefaultContext<String, Object> context;
    ExpressRunner runner;
    InstructionSet instructionSet;

    public void initContext(
            PlayerData data,
            String original,
            int vl,
            Check check,
            String alertString,
            String verbose) {
        runner = new ExpressRunner();
        context = new DefaultContext<>();
        ExpressAPI expressAPI = new ExpressAPI(data, original, vl, check, alertString, verbose);

        
        context.put("vl", vl);
        context.put("checkName", check.getCheckName());
        context.put("alertString", alertString);
        context.put("verbose", verbose);
        context.put("onGround", data.isOnGround() && data.isClientClaimsLastOnGround());
        context.put("transaction", data.getTransactionPing());
        context.put("keepalive", data.getKeepAlivePing());

        
        try {
            runner.addFunctionOfServiceMethod("executeCommand", expressAPI, "executeCommand", new Class[]{String.class}, null);
            runner.addFunctionOfServiceMethod("hasPermission", expressAPI, "hasPermission", new Class[]{String.class}, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean initScript(String value) {
        try {
            if (!runner.checkSyntax(value)) {
                return false;
            }
            instructionSet = runner.getInstructionSetFromLocalCache(value); 
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            List<String> errorList = new ArrayList<>();
            runner.execute(instructionSet, context, errorList, true, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}