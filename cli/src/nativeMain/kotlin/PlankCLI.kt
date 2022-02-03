package com.gabrielleeg1.plank.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class PlankCLI : CliktCommand() {
  init {
    subcommands(PlankJIT(), PlankREPL())
  }

  override fun run() {
    if (currentContext.invokedSubcommand != null) return

    TODO("Not yet implemented: plank")
  }
}
