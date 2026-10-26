#!/bin/zsh
cd "$(dirname "$0")"
javac StudentOpportunityRadar.java || { echo "Could not compile the app. Install a JDK 17 or later and try again."; read -k 1; exit 1; }
java StudentOpportunityRadar
