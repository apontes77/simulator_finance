package org.example

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val imovel = 250000.0.reais
    val taxa = 10.5.percent

    val seguro = 50.0.reais
    val totalComSeguro = imovel + seguro // Funciona!

    // val erro = imovel + taxa
    // ^ O código acima NÃO COMPILA.
    // Isso é o "fail-fast" que economiza horas de debug em sistemas financeiros.

    println("Simulação iniciada para valor de $imovel com taxa de ${taxa.value}%")
    }
