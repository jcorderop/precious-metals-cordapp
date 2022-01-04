package com.template.contracts;

import com.template.states.PreciousMetalState;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.template.contracts.ValidationsContract.*;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class PreciousMetalContract implements Contract {

    private static final Logger logger = LoggerFactory.getLogger(PreciousMetalContract.class);

    // This is used to identify our contract when building a transaction.
    public static final String CONTRACT_ID = "com.template.contracts.PreciousMetalContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        /* We can use the requireSingleCommand function to extract command data from transaction.
         * However, it is possible to have multiple commands in a single transaction.*/
        final CommandWithParties<PreciousMetalCommands> command = getCommandsCommandWithParties(tx);

        if (command.getValue() instanceof PreciousMetalCommands.Issuing) {
            issuingValidation(tx, command);
        } else if (command.getValue() instanceof PreciousMetalCommands.Transfer) {
            transferValidation(tx, command);
        } else {
            throwIllegalArgException("Unsupported command: " + command.getValue().getClass().getName());
        }
    }

    private void issuingValidation(final LedgerTransaction tx, final CommandWithParties<PreciousMetalCommands> command) {
        //Retrieve the output state of the transaction
        final PreciousMetalState output = tx.outputsOfType(PreciousMetalState.class).get(0);

        //Using Corda DSL function requireThat to replicate conditions-checks
        requireThat(require -> {
            validateInputsDuringIssuing(tx, require);
            validateOutputsDuringIssuing(tx, require);
            validateSignatureFromOwner(command, output, require);
            validateValidIssuer(output, require);
            validateIssuerAssignedDuringIssuing(output, require);
            validateMetalName(output, require);
            validateUnits(output, require);
            validateWeight(output, require);
            return null;
        });
    }

    private void transferValidation(LedgerTransaction tx, CommandWithParties<PreciousMetalCommands> command) {
        //Retrieve the output state of the transaction
        final PreciousMetalState input = tx.inputsOfType(PreciousMetalState.class).get(0);
        final PreciousMetalState output = tx.outputsOfType(PreciousMetalState.class).get(0);

        //Using Corda DSL function requireThat to replicate conditions-checks
        requireThat(require -> {
            validateInputsDuringTransfer(tx, require);
            validateOutputsDuringTransfer(tx, require);
            validateSignatureFromOwner(command, input, require);
            //validateSignatureFromOwner(command, output, require);
            //validateValidTrader(output, require);
            validateTraderAssignedDuringTransfer(output, require);
            validateMetalName(output, require);
            validateUnits(output, require);
            validateWeight(output, require);
            return null;
        });
    }
}