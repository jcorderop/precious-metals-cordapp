package com.template.model;

import net.corda.core.identity.CordaX500Name;

import java.util.Arrays;
import java.util.List;

public enum Nodes {
    ISSUER_SWISS_PM_CORPORATE("SwissPMCorporate", "Zurich", "CH"),
    TRADER_SWISS_BANK("SwissBank", "Zurich", "CH"),
    TRADER_CANTONAL_BANK("CantonalBank", "Geneva", "CH");

    private final String organization;
    private final String location;
    private final String country;

    Nodes(String organization, String location, String country) {
        this.organization = organization;
        this.location = location;
        this.country = country;
    }

    public String getOrganization() {
        return this.organization;
    }

    public String getLocation() {
        return this.location;
    }

    public String getCountry() {
        return this.country;
    }

    public final static CordaX500Name issuerName = new CordaX500Name(Nodes.ISSUER_SWISS_PM_CORPORATE.getOrganization(),
                                                                        Nodes.ISSUER_SWISS_PM_CORPORATE.getLocation(),
                                                                        Nodes.ISSUER_SWISS_PM_CORPORATE.getCountry());

    public final static CordaX500Name swissBankName = new CordaX500Name(Nodes.TRADER_SWISS_BANK.getOrganization(),
                                                                        Nodes.TRADER_SWISS_BANK.getLocation(),
                                                                        Nodes.TRADER_SWISS_BANK.getCountry());

    public final static CordaX500Name cantonalBankName = new CordaX500Name(Nodes.TRADER_CANTONAL_BANK.getOrganization(),
                                                                        Nodes.TRADER_CANTONAL_BANK.getLocation(),
                                                                        Nodes.TRADER_CANTONAL_BANK.getCountry());

    public final static List<CordaX500Name> tradersName = Arrays.asList(swissBankName, cantonalBankName);
}
