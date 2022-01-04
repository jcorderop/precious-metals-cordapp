package com.template.flows;

import com.template.contracts.PreciousMetalContract;
import com.template.model.MetalNames;
import com.template.model.MetalUnits;
import com.template.model.Nodes;
import com.template.states.PreciousMetalState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.StartedMockNode;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CommonFlowTest {

    private static final Logger logger = LoggerFactory.getLogger(CommonFlowTest.class);

    public static CordaFuture<SignedTransaction> getCordaFuturePMIssuingFlowInitiator(StartedMockNode node, Party issuer) {
        final PreciousMetalIssuingFlow.PreciousMetalIssuingFlowInitiator pmIssuingFlowInitiator =
                new PreciousMetalIssuingFlow
                        .PreciousMetalIssuingFlowInitiator(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        100);
        return node.startFlow(pmIssuingFlowInitiator);
    }

    public static Long commonElementsInVault(StartedMockNode node) {
        node.getServices()
                .getVaultService()
                .queryBy(PreciousMetalState.class)
                .getStatesMetadata()
                .stream()
                .forEach(stateMetadata -> logger.info(() -> node.getInfo().getLegalIdentities().get(0)+" PM Status: " + stateMetadata.component5().name()));
        //logger.info(() -> ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+stateStatus.name());
        return node.getServices()
                .getVaultService()
                .queryBy(PreciousMetalState.class)
                .getStates()
                .stream()
                .count();
    }

    public static void commonTransferChecks(SignedTransaction signedTransaction,
                                            Party owner,
                                            StartedMockNode node) {

        PreciousMetalState preciousMetalState = signedTransaction.getTx().outputsOfType(PreciousMetalState.class).get(0);
        assertEquals(owner, preciousMetalState.getOwner());

        //QueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED);
        node.getServices()
                .getVaultService()
                .queryBy(PreciousMetalState.class)
                .getStates()
                .stream()
                .forEach(s -> {
                    logger.info(() -> "State Ref Index: " + s.getRef().getIndex());
                    assertEquals(0, Optional.ofNullable(s.getRef()
                                    .getIndex())
                            .orElseThrow(() -> new IllegalStateException("Expecting Not Null")));

                    logger.info(() -> "State Ref Txhash: " + s.getRef().getTxhash());
                    assertNotEquals(null, Optional.ofNullable(s.getRef()
                                    .getTxhash())
                            .orElseThrow(() -> new IllegalStateException("Expecting Not Null")));

                    logger.info(() -> "State Data: " + s.getState().getData());
                    assertEquals(PreciousMetalState.class.getName(), s.getState().getData().getClass().getName());

                    logger.info(() -> "State PreciousMetal: " + s.getState().getData().getPreciousMetal());
                    assertEquals(MetalNames.GOLD_BAR.getMetalName(), s.getState().getData().getPreciousMetal().getMetalName());
                    assertEquals(MetalUnits.GRAMS.getUnit(), s.getState().getData().getPreciousMetal().getUnit());
                    assertEquals(100, s.getState().getData().getPreciousMetal().getWeight());

                    logger.info(() -> "State Contract Name: " + s.getState().getContract());
                    assertEquals(PreciousMetalContract.class.getName(), s.getState().getContract());

                    logger.info(() -> "State Data N. Participants: " + s.getState().getData().getParticipants().size());
                    assertEquals(2, s.getState().getData().getParticipants().size());

                    logger.info(() -> "State Data List Participants: " + s.getState().getData().getParticipants());
                    assertEquals(Nodes.issuerName, s.getState().getData().getParticipants().get(0).nameOrNull());
                    assertEquals(owner.nameOrNull(), s.getState().getData().getParticipants().get(1).nameOrNull());

                    logger.info(() -> "State Data Issuer: " + s.getState().getData().getIssuer());
                    assertEquals(Nodes.issuerName, s.getState().getData().getIssuer().nameOrNull());

                    logger.info(() -> "State Data Owner: " + s.getState().getData().getOwner());
                    assertEquals(owner.nameOrNull(), s.getState().getData().getOwner().nameOrNull());

                    logger.info(() -> "State Notary: " + s.getState().getNotary());
                });
    }
}
