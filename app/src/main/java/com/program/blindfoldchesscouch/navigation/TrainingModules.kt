// navigation/TrainingModules.kt
package com.program.blindfoldchesscouch.navigation

/**
 * Predstavlja jedan modul za trening.
 * @param route Jedinstveni identifikator za navigaciju.
 * @param title Naziv modula koji se prikazuje u meniju.
 * @param description Kratak opis šta korisnik može da očekuje od modula.
 */
data class TrainingModule(
    val route: String,
    val title: String,
    val description: String
)

/**
 * Lista svih dostupnih modula za trening u aplikaciji.
 * Lako možemo dodati nove module ovde.
 */
val trainingModules = listOf(
    TrainingModule("module_1", "Modul 1: Prepoznavanje polja", "Vežbajte brzo prepoznavanje boje polja na koje se figura postavi."),
    TrainingModule("module_2", "Modul 2: Putevi skakača", "Vizuelizujte i pratite kretanje skakača po tabli."),
    TrainingModule("module_3", "Modul 3: Dijagonale lovca", "Naučite da brzo vidite sve dijagonale koje kontroliše lovac."),
    TrainingModule("module_4", "Modul 4: Koordinatni kviz", "Testirajte svoje znanje koordinata na praznoj tabli.")
)