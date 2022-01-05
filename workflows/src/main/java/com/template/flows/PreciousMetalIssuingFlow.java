package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.PreciousMetalCommands;
import com.template.contracts.PreciousMetalContract;
import com.template.states.PreciousMetalState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreciousMetalIssuingFlow {

    private static final Logger logger = LoggerFactory.getLogger(PreciousMetalIssuingFlow.class);

    @InitiatingFlow
    @StartableByRPC
    public static class PreciousMetalIssuingFlowInitiator extends FlowLogic<SignedTransaction>{

        private final ProgressTracker.Step INITIALIZATION = new ProgressTracker.Step("Step 1. Initialization");
        private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Step 2. Retrieving the Notary");
        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Step 3. Generating transaction");
        private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Step 4. Verifying Transaction");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Step 5. Signing transaction with its private key");
        private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Step 6. Sending flow to counterparty");
        private final ProgressTracker.Step FINALIZING_TRANSACTION = new ProgressTracker.Step("Step 7. Obtaining Notary signature and recording transaction");

        private final ProgressTracker progressTracker = new ProgressTracker(
                INITIALIZATION,
                RETRIEVING_NOTARY,
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                COUNTERPARTY_SESSION,
                FINALIZING_TRANSACTION
        );

        //private variables
        //used to build the Precious Metal
        private final String metalName;
        private final String unit;
        private final Integer weight;

        private Party issuer ;
        //new owner that will the issuer
        private Party owner;

        //public constructor
        public PreciousMetalIssuingFlowInitiator(String metalName, String unit, Integer weight) {
            this.metalName = metalName;
            this.unit = unit;
            this.weight = weight;
        }

        @Nullable
        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            progressTracker.setCurrentStep(INITIALIZATION);
            // Step 1. Compose the Issuer, State and command
            this.issuer = getOurIdentity();
            this.owner = getOurIdentity();

            // Creating Command
            final Command command = new Command(new PreciousMetalCommands.Issuing(),
                    Arrays.asList(this.issuer.getOwningKey(),
                            this.owner.getOwningKey()));

            // Creating State
            final PreciousMetalState output = new PreciousMetalState(this.metalName,
                    this.unit,
                    this.weight,
                    this.issuer,
                    this.owner);

            // Step 2. Get a reference to the notary service on our network and our key pair.
            // Note: ongoing work to support multiple notary identities is still in progress.
            progressTracker.setCurrentStep(RETRIEVING_NOTARY);
            final Party notary = getServiceHub()
                    .getNetworkMapCache()
                    .getNotaryIdentities().get(0);

            // Step 3.
            // Create a new TransactionBuilder object.
            // Add the iou as an output state, as well as a command to the transaction builder.
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            final TransactionBuilder builder = new TransactionBuilder(notary)
                    .addOutputState(output, PreciousMetalContract.CONTRACT_ID)
                    .addCommand(command);

            // Step 4. Verify and sign it with our KeyPair.
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            builder.verify(getServiceHub());

            // Step 5. Signing its own transaction.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            final SignedTransaction ptx = getServiceHub()
                    .signInitialTransaction(builder);

            // Step 6. Collect the other party's signature using the SignTransactionFlow.
            progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
            // During issuing the issuer is the owner, no transfer is needed
            final List<FlowSession> sessions = new ArrayList<>();

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
            return subFlow(new FinalityFlow(ptx, sessions));
        }
    }
}
