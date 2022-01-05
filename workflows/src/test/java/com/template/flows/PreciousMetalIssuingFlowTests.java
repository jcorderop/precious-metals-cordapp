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

import static com.template.flows.CommonFlowTest.commonTransferChecks;
import static org.junit.jupiter.api.Assertions.*;

public class PreciousMetalIssuingFlowTests {

    private static final Logger logger = LoggerFactory.getLogger(PreciousMetalIssuingFlowTests.class);
    
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
    public void IssuingPreciousMetalsOwnerIssuerOk() throws ExecutionException, InterruptedException {
        //given
        final Party owner = issuer.getInfo().getLegalIdentities().get(0);
        final CordaFuture<SignedTransaction> signedTransactionCordaFuture = CommonFlowTest.getCordaFuturePMIssuingFlowInitiator(issuer, owner);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
        assertEquals(0, signedTransaction.getTx().getInputs().size());
        assertEquals(1, signedTransaction.getTx().getOutputs().size());

        Command command = signedTransaction.getTx().getCommands().get(0);
        assertTrue(command.getValue() instanceof PreciousMetalCommands.Issuing);
        assertTrue(command.getSigners().contains(owner.getOwningKey()));

        commonTransferChecks(signedTransaction, owner, issuer);
    }

    @Test(expected = ExecutionException.class)
    public void IssuingPreciousMetalsBankAsOwnerFails() throws ExecutionException, InterruptedException {
        //given
        PreciousMetalIssuingFlow.PreciousMetalIssuingFlowInitiator pmIssuingFlowInitiator =
                new PreciousMetalIssuingFlow
                        .PreciousMetalIssuingFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        100);
        CordaFuture<SignedTransaction> signedTransactionCordaFuture = swissBank.startFlow(pmIssuingFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }

    @Test(expected = ExecutionException.class)
    public void IssuingPreciousMetalsBankStartIssuingFlowFails() throws ExecutionException, InterruptedException {
        //given
        PreciousMetalIssuingFlow.PreciousMetalIssuingFlowInitiator pmIssuingFlowInitiator =
                new PreciousMetalIssuingFlow
                        .PreciousMetalIssuingFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        100);
        CordaFuture<SignedTransaction> signedTransactionCordaFuture = swissBank.startFlow(pmIssuingFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }

    @Test(expected = ExecutionException.class)
    public void IssuingPreciousMetalsInvalidMetalNameFails() throws ExecutionException, InterruptedException {
        //given
        PreciousMetalIssuingFlow.PreciousMetalIssuingFlowInitiator pmIssuingFlowInitiator =
                new PreciousMetalIssuingFlow
                        .PreciousMetalIssuingFlowInitiator("",
                        MetalUnits.GRAMS.getUnit(),
                        100);
        CordaFuture<SignedTransaction> signedTransactionCordaFuture = issuer.startFlow(pmIssuingFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }

    @Test(expected = ExecutionException.class)
    public void IssuingPreciousMetalsInvalidUnitNameFails() throws ExecutionException, InterruptedException {
        //given
        PreciousMetalIssuingFlow.PreciousMetalIssuingFlowInitiator pmIssuingFlowInitiator =
                new PreciousMetalIssuingFlow
                        .PreciousMetalIssuingFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        "",
                        100);
        CordaFuture<SignedTransaction> signedTransactionCordaFuture = issuer.startFlow(pmIssuingFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }

    @Test(expected = ExecutionException.class)
    public void IssuingPreciousMetalsInvalidWeightFails() throws ExecutionException, InterruptedException {
        //given
        PreciousMetalIssuingFlow.PreciousMetalIssuingFlowInitiator pmIssuingFlowInitiator =
                new PreciousMetalIssuingFlow
                        .PreciousMetalIssuingFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        -100);
        CordaFuture<SignedTransaction> signedTransactionCordaFuture = issuer.startFlow(pmIssuingFlowInitiator);

        //when
        network.runNetwork();

        //then
        SignedTransaction signedTransaction = signedTransactionCordaFuture.get();
    }
}