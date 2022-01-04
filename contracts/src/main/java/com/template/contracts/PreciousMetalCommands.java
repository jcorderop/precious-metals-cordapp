package com.template.contracts;

import net.corda.core.contracts.CommandData;

// Used to indicate the transaction's intent.
public interface PreciousMetalCommands extends CommandData {
    //commands.
    class Issuing implements PreciousMetalCommands {}
    class Transfer implements PreciousMetalCommands {}
}