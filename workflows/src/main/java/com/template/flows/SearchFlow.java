package com.template.flows;

import com.template.states.PreciousMetalState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.node.services.Vault;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@InitiatingFlow
@StartableByRPC
public class SearchFlow extends FlowLogic<Void> {

    private static final Logger logger = LoggerFactory.getLogger(SearchFlow.class);

    @Override
    public Void call() throws FlowException {

        searchForStates(Vault.StateStatus.UNCONSUMED,
                Vault.StateStatus.CONSUMED);

        return null;
    }

    //TODO query criteria gives cast error -> this filtering is temporal
    private void searchForStates(Vault.StateStatus ... vaultStatus) {
        System.out.println("___ New Search ___");
        System.out.println("LegalIdentities: "+ getServiceHub().getMyInfo().getLegalIdentities().get(0));

        final List<StateAndRef<PreciousMetalState>> states = getServiceHub()
                .getVaultService()
                .queryBy(PreciousMetalState.class)
                .getStates();

        final List<Vault.StateMetadata> statesMetadata = getServiceHub()
                .getVaultService()
                .queryBy(PreciousMetalState.class)
                .getStatesMetadata();

        for (Vault.StateStatus vs: vaultStatus) {
            final List<StateAndRef<PreciousMetalState>> statesUnconsumed  = getStates(states,
                    statesMetadata,
                    vs);
        }
    }

    public static List<StateAndRef<PreciousMetalState>> getStates(final List<StateAndRef<PreciousMetalState>> states,
                                                            final List<Vault.StateMetadata> statesMetadata,
                                                            final Vault.StateStatus stateStatus) {
        final List<Vault.StateMetadata> stateMetadata = getStateMetadata(statesMetadata, stateStatus);
        List<StateAndRef<PreciousMetalState>> stateAndRefs = IntStream.range(0, stateMetadata.size())
                .mapToObj(value -> states
                        .get(stateMetadata
                                .get(value)
                                .getRef()
                                .getIndex()))
                .collect(Collectors.toList());

        IntStream.range(0, stateAndRefs.size())
                .forEach(value -> {
                    System.out.println("------------------------------ Printing -------------------------------");
                    final int index = stateAndRefs.get(value).getRef().getIndex();
                    printStates(states.get(index));
                    printStateMetadata(statesMetadata.get(index));
                });
        return stateAndRefs;
    }

    @NotNull
    private static List<Vault.StateMetadata> getStateMetadata(final List<Vault.StateMetadata> statesMetadata,
                                                       final Vault.StateStatus stateStatus) {
        System.out.println("Querying with StateStatus: ["+stateStatus.name()+"]");

        List<Vault.StateMetadata> statesMetadataFound = statesMetadata.stream()
                .filter(stateMetadata -> stateMetadata.
                        getStatus().
                        equals(stateStatus))
                .collect(Collectors.toList());
        System.out.println("Found with StateStatus: ["+statesMetadataFound.size()+"]");
        return statesMetadataFound;
    }

    private static void printStates(StateAndRef<PreciousMetalState> stateAndRef) {
        System.out.println("-------------------------------- State --------------------------------");
        System.out.println("Index: " + stateAndRef.getRef().getIndex());
        System.out.println("Tx-hash: " + stateAndRef.getRef().getTxhash());
        System.out.println("Issuer: " + stateAndRef.getState().getData().getIssuer());
        System.out.println("Owner: " + stateAndRef.getState().getData().getOwner());
        System.out.println("PreciousMetal: " + stateAndRef.getState().getData().getPreciousMetal());
        System.out.println("Notary: " + stateAndRef.getState().getNotary().nameOrNull());
        System.out.println("Contract: " + stateAndRef.getState().getContract());
    }

    private static void printStateMetadata(Vault.StateMetadata stateMetadata) {
        System.out.println("---------------------------- StateMetadata ----------------------------");
        System.out.println("Index: " + stateMetadata.component1().getIndex());
        System.out.println("Tx-hash: " + stateMetadata.component1().getTxhash());
        System.out.println("RecordedTime: " + stateMetadata.getRecordedTime());
        System.out.println("ConsumedTime: " + stateMetadata.getConsumedTime());
        System.out.println("LockId: " + stateMetadata.getLockId());
        System.out.println("LockUpdateTime: " + stateMetadata.getLockUpdateTime());
        System.out.println("ContractStateClassName: " + stateMetadata.getContractStateClassName());
        System.out.println("Status: " + stateMetadata.getStatus().name());
        System.out.println("RelevancyStatus: "+ stateMetadata.getRelevancyStatus().name());
    }
}