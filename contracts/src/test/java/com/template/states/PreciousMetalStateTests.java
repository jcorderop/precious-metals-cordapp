package com.template.states;

import com.template.CommonTest;
import com.template.model.MetalNames;
import com.template.model.MetalUnits;
import com.template.model.Nodes;
import com.template.model.PreciousMetal;
import net.corda.core.contracts.ContractState;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreciousMetalStateTests {

    private final Logger logger = LoggerFactory.getLogger(PreciousMetalStateTests.class);
    
    private final TestIdentity swissPmCorporate = CommonTest.getSwissPmCorporateTestIdentity();
    private final TestIdentity swissBank = CommonTest.getSwissBankTestIdentity();
    
    @Test(expected = IllegalArgumentException.class)
    public void preciousMetalStateInvalidPreciousMetalAttributeMetalNameException() {
        //given
        new PreciousMetalState(null,
                MetalUnits.GRAMS.getUnit(),
                100,
                swissPmCorporate.getParty(),
                swissBank.getParty());
        //when
        //then
    }

    @Test(expected = IllegalArgumentException.class)
    public void preciousMetalStateInvalidPreciousMetalAttributeUnitException() {
        //given
        new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                null,
                100,
                swissPmCorporate.getParty(),
                swissBank.getParty());
        //when
        //then
    }

    @Test(expected = IllegalArgumentException.class)
    public void preciousMetalStateInvalidPreciousMetalAttributeWeightException() {
        //given
        new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                MetalUnits.GRAMS.getUnit(),
                null,
                swissPmCorporate.getParty(),
                swissBank.getParty());
        //when
        //then
    }

    @Test(expected = IllegalArgumentException.class)
    public void preciousMetalStateInvalidIssuerException() {
        //given
        new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                MetalUnits.GRAMS.getUnit(),
                100,
                null,
                swissBank.getParty());
        //when
        //then
    }

    @Test(expected = IllegalArgumentException.class)
    public void preciousMetalStateInvalidOwnerException() {
        //given
        new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                MetalUnits.GRAMS.getUnit(),
                100,
                swissPmCorporate.getParty(),
                null);
        //when
        //then
    }

    @Test
    public void preciousMetalStateInstanceOfContractState() {
        //given
        final PreciousMetalState PreciousMetalState = CommonTest.getPreciousMetalState(swissBank.getParty());
        //when

        //then
        assertTrue(PreciousMetalState instanceof ContractState);
    }

    @Test
    public void preciousMetalStateVerifyParticipantsIssuing() {
        //given
        final PreciousMetalState PreciousMetalState = CommonTest.getPreciousMetalState(swissBank.getParty());
        //when

        //then
        assertEquals(2, PreciousMetalState.getParticipants().size());

        //Party is does not overwrite hashcode and equals
        logger.info(() -> swissPmCorporate.getParty().nameOrNull().toString());
        assertTrue(PreciousMetalState.getParticipants()
                .stream()
                .anyMatch(abstractParty -> abstractParty
                        .nameOrNull()
                        .equals(swissPmCorporate.getParty().nameOrNull())));

        logger.info(() -> swissBank.getParty().nameOrNull().toString());
        assertTrue(PreciousMetalState.getParticipants()
                .stream()
                .anyMatch(abstractParty -> abstractParty
                        .nameOrNull()
                        .equals(swissBank.getParty().nameOrNull())));
    }

    @Test
    public void preciousMetalStateVerifyState() throws NoSuchFieldException {
        //given
        final PreciousMetalState PreciousMetalState = CommonTest.getPreciousMetalState(swissBank.getParty());
        //when

        //then
        logger.info(() -> PreciousMetalState.getMetalName());
        assertEquals(MetalNames.GOLD_BAR.getMetalName(),
                PreciousMetalState.getMetalName());

        logger.info(() -> PreciousMetalState.getWeight().toString());
        assertEquals(100,
                PreciousMetalState.getWeight());

        logger.info(() -> PreciousMetalState.getUnit());
        assertEquals(MetalUnits.GRAMS.getUnit(),
                PreciousMetalState.getUnit());

        logger.info(() -> PreciousMetalState.getIssuer().nameOrNull().toString());
        assertEquals(Nodes.ISSUER_SWISS_PM_CORPORATE.getOrganization(),
                PreciousMetalState.getIssuer().getName().getOrganisation());

        logger.info(() -> PreciousMetalState.getOwner().nameOrNull().toString());
        assertEquals(Nodes.TRADER_SWISS_BANK.getOrganization(),
                PreciousMetalState.getOwner().getName().getOrganisation());
    }

    //Mock State test check for if the state has correct parameters type
    @Test
    public void hasFieldOfCorrectType() throws NoSuchFieldException {
        PreciousMetalState.class.getDeclaredField("preciousMetal");
        assert (PreciousMetalState.class.getDeclaredField("preciousMetal").getType().equals(PreciousMetal.class));
    }
}