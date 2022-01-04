package com.template;

import com.template.model.MetalNames;
import com.template.model.MetalUnits;
import com.template.model.Nodes;
import com.template.states.PreciousMetalState;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class CommonTest {

    public static TestIdentity getSwissPmCorporateTestIdentity() {
        return new TestIdentity(Nodes.issuerName);
    }

    public static TestIdentity getSwissBankTestIdentity() {
        return new TestIdentity(Nodes.swissBankName);
    }

    public static TestIdentity getCantonalBankTestIdentity() {
        return new TestIdentity(Nodes.cantonalBankName);
    }

    public static PreciousMetalState  getPreciousMetalState(Party owner) {
        return new PreciousMetalState(MetalNames.GOLD_BAR.getMetalName(),
                        MetalUnits.GRAMS.getUnit(),
                        100,
                        getSwissPmCorporateTestIdentity().getParty(),
                        owner);
    }
}
