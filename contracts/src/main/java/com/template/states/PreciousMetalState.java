package com.template.states;

import com.template.contracts.PreciousMetalContract;
import com.template.model.PreciousMetal;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// *********
// * State *
// *********

@BelongsToContract(PreciousMetalContract.class)
public class PreciousMetalState implements ContractState {

    //private variables
    private final Party issuer;
    private final Party owner;
    private final PreciousMetal preciousMetal;

    /* Constructor of your Corda state */
    public PreciousMetalState(String metalName,
                              String unit,
                              Integer weight,
                              Party issuer,
                              Party owner) {
        this.preciousMetal = new PreciousMetal(metalName, unit, weight);
        this.issuer = Optional.ofNullable(issuer)
                .orElseThrow(() -> new IllegalArgumentException("Issuer Party cannot be null..."));
        this.owner = Optional.ofNullable(owner)
                .orElseThrow(() -> new IllegalArgumentException("Owner Party cannot be null..."));;

    }


    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @Override
    public List<AbstractParty> getParticipants() {
        return Collections.unmodifiableList(Arrays.asList(issuer, owner));
    }

    public String getMetalName() {
        return this.preciousMetal.getMetalName();
    }

    public Integer getWeight() {
        return this.preciousMetal.getWeight();
    }

    public String getUnit() {
        return this.preciousMetal.getUnit();
    }

    public Party getIssuer() {
        return this.issuer;
    }

    public Party getOwner() {
        return this.owner;
    }

    public PreciousMetal getPreciousMetal() {
        return preciousMetal;
    }
}