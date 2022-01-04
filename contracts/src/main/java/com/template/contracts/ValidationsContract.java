package com.template.contracts;

import com.template.model.MetalNames;
import com.template.model.MetalUnits;
import com.template.states.PreciousMetalState;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Requirements;
import net.corda.core.transactions.LedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.template.model.MetalNames.METAL_NAMES;
import static com.template.model.MetalUnits.UNIT_NAMES;
import static com.template.model.Nodes.issuerName;
import static com.template.model.Nodes.tradersName;
import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

public class ValidationsContract {

    private static final Logger logger = LoggerFactory.getLogger(ValidationsContract.class);

    public static CommandWithParties<PreciousMetalCommands> getCommandsCommandWithParties(LedgerTransaction tx) {
        final CommandWithParties<PreciousMetalCommands> command = requireSingleCommand(tx.getCommands(), PreciousMetalCommands.class);
        logger.info("New command received: "+ command.getValue().getClass().getSimpleName());
        return command;
    }

    public static void validateSignatureFromOwner(CommandWithParties<PreciousMetalCommands> command, PreciousMetalState state, Requirements require) {
        logger.info("Signers: "+command.getSigners().size());
        require.using("Signature from owner is invalid.", command.getSigners().contains(state.getOwner().getOwningKey()));
    }

    public static void validateInputsDuringIssuing(LedgerTransaction tx, Requirements require) {
        //Check Inputs must be 0
        require.using("No inputs should be consumed when Issuing Precious Metals.", tx.getInputStates().size() == 0);
    }

    public static void validateOutputsDuringIssuing(LedgerTransaction tx, Requirements require) {
        //Check Output must be 1
        require.using("Only one outputs should be consumed when Issuing Precious Metals.", tx.getOutputStates().size() == 1);
    }

    public static void validateIssuerAssignedDuringIssuing(PreciousMetalState output, Requirements require) {
        //Check during issuing, asset cannot be transfer
        require.using("During the Issuing the owner must be: ["+issuerName+"] and received: ["+ output.getOwner().nameOrNull()+"]",
                issuerName.equals(output.getOwner()
                        .nameOrNull()));
    }

    public static void validateWeight(PreciousMetalState output, Requirements require) {
        //Check valid metal weight
        require.using("The metal weight must be greater than zero and received: ["+ output.getWeight()+"]",
                output.getWeight() > 0);
    }

    public static void validateUnits(PreciousMetalState output, Requirements require) {
        //Check valid metal units
        require.using("The metal unit must be any of: ["+ UNIT_NAMES +"] and received: ["+ output.getUnit()+"]",
                Arrays.stream(MetalUnits.values())
                        .anyMatch(metalUnits -> metalUnits.getUnit()
                                .equals(output.getUnit())));
    }

    public static void validateMetalName(PreciousMetalState output, Requirements require) {
        //Check valid metal names
        require.using("The metal name must be any of: ["+ METAL_NAMES +"] and received: ["+ output.getMetalName()+"]",
                Arrays.stream(MetalNames.values())
                        .anyMatch(metalNames -> metalNames.getMetalName()
                                .equals(output.getMetalName())));
    }

    public static void validateValidIssuer(PreciousMetalState output, Requirements require) {
        //TODO check how to assign a issuing and trading group
        //Check a valid issuer, cannot be trader
        require.using("It is is not a valid Issuer: ["+issuerName+"] and received: ["+ output.getIssuer().nameOrNull().getOrganisation()+"]",
                issuerName.getOrganisation()
                        .equals(output.getIssuer()
                                .nameOrNull()
                                .getOrganisation()));
    }



    public static void validateInputsDuringTransfer(LedgerTransaction tx, Requirements require) {
        //Check Inputs must be 1
        require.using("No inputs should be consumed when Transfer Precious Metals.", tx.getInputStates().size() == 1);
    }

    public static void validateOutputsDuringTransfer(LedgerTransaction tx, Requirements require) {
        //Check Output must be 1
        require.using("Only one outputs should be consumed when Transfer Precious Metals.", tx.getOutputStates().size() == 1);
    }

    public static void validateTraderAssignedDuringTransfer(PreciousMetalState output, Requirements require) {
        //Check during issuing, asset cannot be transfer
        require.using("During the Transfer the owner must be a valid trader: ["+tradersName+"] and received: ["+ output.getOwner().nameOrNull()+"]",
                tradersName.contains(output.getOwner().nameOrNull()));
    }



    public static void throwIllegalArgException(final String msg) {
        logger.error(msg);
        throw new IllegalArgumentException(msg);
    }
}
