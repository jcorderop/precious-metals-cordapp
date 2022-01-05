package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.PreciousMetalCommands;
import com.template.contracts.PreciousMetalContract;
import com.template.model.PreciousMetal;
import com.template.states.PreciousMetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.template.flows.SearchFlow.getStates;
import static com.template.flows.SearchFlow.printStates;

public class PreciousMetalTransferFlow {

    private static final Logger logger = LoggerFactory.getLogger(PreciousMetalTransferFlow.class);

    @InitiatingFlow
    @StartableByRPC
    public static class PreciousMetalTransferFlowInitiator extends FlowLogic<SignedTransaction>{

        private final ProgressTracker.Step INITIALIZATION = new ProgressTracker.Step("Initialization");
        private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the Notary");
        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction");
        private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying Transaction");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with its private key");
        private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty");
        private final ProgressTracker.Step FINALIZING_TRANSACTION = new ProgressTracker.Step("Obtaining Notary signature and recording transaction");

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
        //new owner
        private Party seller;
        private final Party buyer;

        //public constructor
        public PreciousMetalTransferFlowInitiator(String metalName,
                                                  String unit,
                                                  Integer weight,
                                                  Party buyer) {
            this.metalName = metalName;
            this.unit = unit;
            this.weight = weight;
            this.buyer = buyer;
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
            this.seller = getOurIdentity();

            // Getting element to transfer from the Vault
            final PreciousMetal pmToTransfer = new PreciousMetal(this.metalName,
                    this.unit,
                    this.weight);

            final StateAndRef<PreciousMetalState> pmStateAndRef = getPreciousMetalStateStateAndRef(pmToTransfer);
            final PreciousMetalState pmState = pmStateAndRef.getState().getData();

            // Creating State
            final PreciousMetalState output = new PreciousMetalState(pmState.getMetalName(),
                    pmState.getUnit(),
                    pmState.getWeight(),
                    pmState.getIssuer(),
                    this.buyer);

            // Creating Command
            final Command command = new Command(new PreciousMetalCommands.Transfer(),
                    Arrays.asList(pmState.getIssuer().getOwningKey(),
                            this.seller.getOwningKey(),
                            this.buyer.getOwningKey()));

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
                    .addInputState(pmStateAndRef)
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
            final List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            final List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            final SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            progressTracker.setCurrentStep(FINALIZING_TRANSACTION);
            return subFlow(new FinalityFlow(stx, sessions));
        }

        private StateAndRef<PreciousMetalState> getPreciousMetalStateStateAndRef(PreciousMetal pmToTransfer) throws FlowException {
            //https://docs.r3.com/en/tutorials/corda/4.8/os/build-basic-cordapp/basic-cordapp-flows.html
            //TODO Query criteria is not working as expected
            final List<StateAndRef<PreciousMetalState>> states = getServiceHub()
                    .getVaultService()
                    .queryBy(PreciousMetalState.class)
                    .getStates();

            final List<Vault.StateMetadata> statesMetadata = getServiceHub()
                    .getVaultService()
                    .queryBy(PreciousMetalState.class)
                    .getStatesMetadata();

            final List<StateAndRef<PreciousMetalState>> statesUnconsumed  = getStates(states,
                    statesMetadata,
                    Vault.StateStatus.UNCONSUMED,
                    false);

            StateAndRef<PreciousMetalState> stateAndRefs = statesUnconsumed
                    .stream()
                    .filter(stateAndRef -> stateAndRef
                            .getState()
                            .getData()
                            .getPreciousMetal()
                            .equals(pmToTransfer))
                    .findFirst()
                    .orElseThrow(() -> new FlowException("Has NO assets to transfer..."));

            printStates(stateAndRefs);
            return stateAndRefs;
        }
    }

    @InitiatedBy(PreciousMetalTransferFlowInitiator.class)
    public static class PreciousMetalTransferFlowResponder extends FlowLogic<SignedTransaction> {
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public PreciousMetalTransferFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            logger.info("PreciousMetalTransferFlowResponder - Preparing for confirmation...");
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any aditional checks.
                     * */
                }
            });
            //Stored the transaction into data base.

            logger.info("PreciousMetalTransferFlowResponder - Singing confirmation...");
            return subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
        }
    }

}
