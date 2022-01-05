package com.template.flows;

import com.google.common.collect.ImmutableList;
import com.template.model.MetalNames;
import com.template.model.MetalUnits;
import com.template.model.Nodes;
import net.corda.core.concurrent.CordaFuture;
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

public class SearchFlowTests {

    private static final Logger logger = LoggerFactory.getLogger(SearchFlowTests.class);

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
    public void searchForElementsInTheVaultNotFound() throws ExecutionException, InterruptedException {
        //given
        SearchFlow searchFlow = new SearchFlow();
        CordaFuture<Void> signedTransactionCordaFuture = issuer.startFlow(searchFlow);

        //when
        network.runNetwork();

        //then
        Void signedTransaction = signedTransactionCordaFuture.get();
    }

    @Test
    public void searchForElementsInTheVaultUnconsumed() throws ExecutionException, InterruptedException {
        //given
        final Party owner = issuer.getInfo().getLegalIdentities().get(0);
        final CordaFuture<SignedTransaction> signedTransactionCordaFuture = CommonFlowTest.getCordaFuturePMIssuingFlowInitiator(issuer, owner);
        network.runNetwork();
        signedTransactionCordaFuture.get();

        final CordaFuture<SignedTransaction> signedTransactionCordaFuture2 = CommonFlowTest.getCordaFuturePMIssuingFlowInitiator(issuer, owner);
        network.runNetwork();
        signedTransactionCordaFuture2.get();

        //when
        SearchFlow searchFlow = new SearchFlow();
        CordaFuture<Void> searchCordaFuture = issuer.startFlow(searchFlow);
        network.runNetwork();

        //then
        searchCordaFuture.get();
    }

    @Test
    public void searchForElementsInTheVaultConsumed() throws ExecutionException, InterruptedException {
        //given
        final Party owner = issuer.getInfo().getLegalIdentities().get(0);
        final PreciousMetalIssuingFlow.PreciousMetalIssuingFlowInitiator pmIssuingFlowInitiator =
                new PreciousMetalIssuingFlow
                        .PreciousMetalIssuingFlowInitiator(MetalNames.GOLD_COIN.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        10);
        CordaFuture<SignedTransaction> signedTransactionCordaFuture = issuer.startFlow(pmIssuingFlowInitiator);
        network.runNetwork();
        signedTransactionCordaFuture.get();

        final CordaFuture<SignedTransaction> signedTransactionCordaFuture2 = CommonFlowTest.getCordaFuturePMIssuingFlowInitiator(issuer, owner);
        network.runNetwork();
        signedTransactionCordaFuture2.get();

        //Transfer to a bank
        final Party newOwner =  cantonalBank.getInfo().getLegalIdentities().get(0);
        PreciousMetalTransferFlowTests.trasnferToBank(issuer, newOwner, network);

        //when
        SearchFlow issuerSearchFlow = new SearchFlow();
        CordaFuture<Void> issuerSearchCordaFuture = issuer.startFlow(issuerSearchFlow);
        network.runNetwork();
        issuerSearchCordaFuture.get();

        SearchFlow cantonalBankSearchFlow = new SearchFlow();
        CordaFuture<Void> cantonalBankSearchCordaFuture = cantonalBank.startFlow(cantonalBankSearchFlow);
        network.runNetwork();

        //then
        cantonalBankSearchCordaFuture.get();
    }
}