package com.template.contracts;

import com.template.CommonTest;
import com.template.model.MetalNames;
import com.template.model.MetalUnits;
import com.template.states.PreciousMetalState;
import net.corda.core.contracts.Contract;
import net.corda.testing.contracts.DummyContract;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.node.MockServices;
import org.junit.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.Arrays;

import static com.template.contracts.CommonContractTest.*;
import static net.corda.testing.node.NodeTestUtils.ledger;

public class PreciousMetalIssuingContractTests {

    private final Logger logger = LoggerFactory.getLogger(PreciousMetalIssuingContractTests.class);

    private final MockServices ledgerServices = new MockServices(Arrays.asList("com.template"));

    @Test
    public void issuingTransactionOK() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.verifies();
            });
            return null;
        });
    }

    @Test
    public void issuingInstanceOfContract () {
        //given
        //when
        //then
        assert(new PreciousMetalContract() instanceof Contract);
    }

    @Test
    public void issuingTransactionOWithInputFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails(); //fails because of having inputs
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionOWithTwoOutputFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test(expected = IllegalStateException.class)
    public void issuingTransactionOWithNoOutputFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionDummyStateFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, new DummyState());
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionDummyCommandFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new DummyContract.Commands.Create());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionMoreThanOneCommandFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionInvalidIssuerRequiredSignerFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test(expected = Exception.class)
    public void issuingTransactionInvalidPM_IDFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output("XXX", pmStateIssuance);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionInvalidStateFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionInvalidIssuerFails() {
        //given
        final PreciousMetalState state = new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                MetalUnits.GRAMS.getUnit(),
                100,
                CommonTest.getSwissBankTestIdentity().getParty(),
                CommonTest.getSwissPmCorporateTestIdentity().getParty());
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, state);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionInvalidMetalNameFails() {
        //given
        final PreciousMetalState state = new PreciousMetalState("Stones",
                MetalUnits.GRAMS.getUnit(),
                100,
                CommonTest.getSwissPmCorporateTestIdentity().getParty(),
                CommonTest.getSwissBankTestIdentity().getParty());
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, state);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionInvalidUnitNameFails() {
        //given
        final PreciousMetalState state = new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                "Liters",
                100,
                CommonTest.getSwissPmCorporateTestIdentity().getParty(),
                CommonTest.getSwissBankTestIdentity().getParty());
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, state);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void issuingTransactionInvalidWeightFails() {
        //given
        final PreciousMetalState state = new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                MetalUnits.GRAMS.getUnit(),
                -100,
                CommonTest.getSwissBankTestIdentity().getParty(),
                CommonTest.getSwissPmCorporateTestIdentity().getParty());
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, state);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Issuing());
                return tx.fails();
            });
            return null;
        });
    }
}