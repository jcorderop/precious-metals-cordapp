package com.template.contracts;

import com.template.CommonTest;
import com.template.states.PreciousMetalState;
import net.corda.testing.core.TestIdentity;

public class CommonContractTest {

    public static final TestIdentity swissPmCorporate = CommonTest.getSwissPmCorporateTestIdentity();
    public static final TestIdentity swissBank = CommonTest.getSwissBankTestIdentity();
    public static final TestIdentity cantonalBank = CommonTest.getCantonalBankTestIdentity();

    public static final PreciousMetalState pmStateIssuance = CommonTest.getPreciousMetalState(swissPmCorporate.getParty());
    public static final PreciousMetalState pmStateTransferSwissBank = CommonTest.getPreciousMetalState(swissBank.getParty());
    public static final PreciousMetalState pmStateTransferCantonalBank = CommonTest.getPreciousMetalState(cantonalBank.getParty());
}
