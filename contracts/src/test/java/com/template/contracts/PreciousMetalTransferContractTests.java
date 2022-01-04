package com.template.contracts;

import net.corda.testing.contracts.DummyContract;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.node.MockServices;
import org.junit.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.Arrays;

import static com.template.contracts.CommonContractTest.*;
import static net.corda.testing.node.NodeTestUtils.ledger;

public class PreciousMetalTransferContractTests {

    private final Logger logger = LoggerFactory.getLogger(PreciousMetalTransferContractTests.class);

    private final MockServices ledgerServices = new MockServices(Arrays.asList("com.template"));
    
    @Test
    public void transferTransactionWithOneInputAndOneOutputFromIssuingToTraderOK() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.verifies();
            });
            return null;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithOneInputAndOneOutputFromTraderToTraderOK() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.verifies();
            });
            return null;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                //swissPmCorporate is signing the transaction
                tx.command(cantonalBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithTwoInputFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithNoInputFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithTwoOutputFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithNoOutputFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithWithDummyStateFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, new DummyState());
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });

        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, new DummyState());
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithDummyCommandFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new DummyContract.Commands.Create());
                return tx.fails();
            });
            return null;
        });
    }

    @Test
    public void transferTransactionWithInvalidSignatureFails() {
        //given
        //when
        //then
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateIssuance);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissBank.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PreciousMetalContract.CONTRACT_ID, pmStateTransferCantonalBank);
                tx.output(PreciousMetalContract.CONTRACT_ID, pmStateTransferSwissBank);
                //swissPmCorporate is signing the transaction
                tx.command(swissPmCorporate.getPublicKey(), new PreciousMetalCommands.Transfer());
                return tx.fails();
            });
            return null;
        });
    }
}