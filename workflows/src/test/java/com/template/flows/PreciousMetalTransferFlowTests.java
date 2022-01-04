package com.template.flows;

import com.google.common.collect.ImmutableList;
import com.template.contracts.PreciousMetalCommands;
import com.template.model.MetalNames;
import com.template.model.MetalUnits;
import com.template.model.Nodes;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.concurrent.ExecutionException;

import static com.template.flows.CommonFlowTest.commonElementsInVault;
import static com.template.flows.CommonFlowTest.commonTransferChecks;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreciousMetalTransferFlowTests {

    private final Logger logger = LoggerFactory.getLogger(PreciousMetalTransferFlowTests.class);
    
    private MockNetwork network;
    private StartedMockNode issuer;
    private StartedMockNode swissBank;
    private StartedMockNode cantonalBank;

    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows"))));

        issuer = network.createPartyNode(Nodes.issuerName);
        swissBank = network.createPartyNode(Nodes.swissBankName);
        cantonalBank = network.createPartyNode(Nodes.cantonalBankName);

        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void TransferPreciousMetalsFromBankToBankOk() throws ExecutionException, InterruptedException {
        //given
        //issue initial asset
        final Party partyIssuer =  issuer.getInfo().getLegalIdentities().get(0);
        issueAsset(partyIssuer);

        //when
        //Transfer from issuer to a bank
        final Party partyOwner =  cantonalBank.getInfo().getLegalIdentities().get(0);
        SignedTransaction signedTransactionToBank = trasnferToBank(issuer, partyOwner);

        assertEquals(1, commonElementsInVault(issuer));
        assertEquals(0, commonElementsInVault(swissBank));
        assertEquals(1, commonElementsInVault(cantonalBank));

        testInputsAndOutputs(signedTransactionToBank);
        testCommand(partyIssuer, partyOwner, signedTransactionToBank);
        commonTransferChecks(signedTransactionToBank, partyOwner, cantonalBank);

        //Transfer from bank to a bank
        final Party partySecondOwner =  swissBank.getInfo().getLegalIdentities().get(0);
        SignedTransaction signedTransactionToSecondBank = trasnferToBank(cantonalBank, partySecondOwner);

        //then

        //Check contracts in the vault
        assertEquals(1, commonElementsInVault(issuer));
        assertEquals(1, commonElementsInVault(swissBank));
        assertEquals(0, commonElementsInVault(cantonalBank));

        testInputsAndOutputs(signedTransactionToSecondBank);
        testCommand(partyIssuer, partySecondOwner, signedTransactionToSecondBank);
        commonTransferChecks(signedTransactionToSecondBank, partySecondOwner, swissBank);
    }

    @Test
    public void TransferPreciousMetalsFromIssuerToBankOk() throws ExecutionException, InterruptedException {
        //given
        //issue initial asset
        final Party partyIssuer =  issuer.getInfo().getLegalIdentities().get(0);
        issueAsset(partyIssuer);

        //when
        //Transfer to a bank
        final Party owner =  cantonalBank.getInfo().getLegalIdentities().get(0);
        SignedTransaction signedTransactionToBank = trasnferToBank(issuer, owner);

        //then
        testInputsAndOutputs(signedTransactionToBank);
        testCommand(partyIssuer, owner, signedTransactionToBank);

        //Check contracts in the vault
        assertEquals(1, commonElementsInVault(issuer));
        assertEquals(0, commonElementsInVault(swissBank));
        assertEquals(1, commonElementsInVault(cantonalBank));

        commonTransferChecks(signedTransactionToBank, owner, cantonalBank);
    }

    private SignedTransaction trasnferToBank(StartedMockNode nodeStarted, Party owner) throws InterruptedException, ExecutionException {
        PreciousMetalTransferFlow.PreciousMetalTransferFlowInitiator pmTransferFlowInitiator =
                new PreciousMetalTransferFlow
                        .PreciousMetalTransferFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        100,
                        owner);

        CordaFuture<SignedTransaction> signedTransactionToBankCordaFuture = nodeStarted.startFlow(pmTransferFlowInitiator);
        logger.info(() -> "Preparing to call signedTransactionToBank...");
        network.runNetwork();
        SignedTransaction signedTransactionToBank = signedTransactionToBankCordaFuture.get();
        logger.info(() -> "Finished signedTransactionToBank...");
        return signedTransactionToBank;
    }

    private void issueAsset(Party partyIssuer) throws InterruptedException, ExecutionException {
        final CordaFuture<SignedTransaction> signedTransactionCordaFuture = CommonFlowTest.getCordaFuturePMIssuingFlowInitiator(issuer, partyIssuer);
        network.runNetwork();
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();

        //Check contracts in the vault
        assertEquals(1, commonElementsInVault(issuer));
        assertEquals(0, commonElementsInVault(swissBank));
        assertEquals(0, commonElementsInVault(cantonalBank));
    }

    private void testCommand(Party seller, Party owner, SignedTransaction signedTransactionToBank) {
        //Check commands
        Command command = signedTransactionToBank.getTx().getCommands().get(0);
        assertTrue(command.getValue() instanceof PreciousMetalCommands.Transfer);
        assertTrue(command.getSigners().contains(seller.getOwningKey()));
        assertTrue(command.getSigners().contains(owner.getOwningKey()));
    }

    private void testInputsAndOutputs(SignedTransaction signedTransactionToBank) {
        //Check inputs and outputs
        assertEquals(1, signedTransactionToBank.getTx().getInputs().size());
        assertEquals(1, signedTransactionToBank.getTx().getOutputs().size());
    }

    @Test(expected = ExecutionException.class)
    public void TransferPreciousMetalsFromBankToIssuerFails() throws ExecutionException, InterruptedException {
        //given
        final Party owner =  issuer.getInfo().getLegalIdentities().get(0);
        PreciousMetalTransferFlow.PreciousMetalTransferFlowInitiator pmTransferFlowInitiator =
                new PreciousMetalTransferFlow
                        .PreciousMetalTransferFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        100,
                        owner);

        CordaFuture<SignedTransaction> signedTransactionCordaFuture = swissBank.startFlow(pmTransferFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }
    
    @Test(expected = ExecutionException.class)
    public void TransferPreciousMetalsInvalidMetalNameFails() throws ExecutionException, InterruptedException {
        //given
        final Party owner =  swissBank.getInfo().getLegalIdentities().get(0);
        PreciousMetalTransferFlow.PreciousMetalTransferFlowInitiator pmTransferFlowInitiator =
                new PreciousMetalTransferFlow
                        .PreciousMetalTransferFlowInitiator("",
                        MetalUnits.GRAMS.getUnit(),
                        100,
                        owner);

        CordaFuture<SignedTransaction> signedTransactionCordaFuture = cantonalBank.startFlow(pmTransferFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }

    @Test(expected = ExecutionException.class)
    public void TransferPreciousMetalsInvalidUnitNameFails() throws ExecutionException, InterruptedException {
        //given
        final Party owner =  swissBank.getInfo().getLegalIdentities().get(0);
        PreciousMetalTransferFlow.PreciousMetalTransferFlowInitiator pmTransferFlowInitiator =
                new PreciousMetalTransferFlow
                        .PreciousMetalTransferFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        "",
                        100,
                        owner);

        CordaFuture<SignedTransaction> signedTransactionCordaFuture = cantonalBank.startFlow(pmTransferFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }

    @Test(expected = ExecutionException.class)
    public void TransferPreciousMetalsInvalidWeightFails() throws ExecutionException, InterruptedException {
        //given
        final Party owner =  swissBank.getInfo().getLegalIdentities().get(0);
        PreciousMetalTransferFlow.PreciousMetalTransferFlowInitiator pmTransferFlowInitiator =
                new PreciousMetalTransferFlow
                        .PreciousMetalTransferFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        -100,
                        owner);

        CordaFuture<SignedTransaction> signedTransactionCordaFuture = swissBank.startFlow(pmTransferFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }
}