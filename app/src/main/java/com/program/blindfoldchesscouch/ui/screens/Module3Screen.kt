// ui/screens/Module3Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Move
import com.program.blindfoldchesscouch.model.PieceType
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
import com.program.blindfoldchesscouch.viewmodel.Module3UiState
import com.program.blindfoldchesscouch.viewmodel.Module3ViewModel
import com.program.blindfoldchesscouch.viewmodel.TestState
import java.util.concurrent.TimeUnit

// --- Boje za ovaj modul ---
private val moveFromColor = Color(0x993498DB) // Plava za polje SA kog se kreće
private val moveToColor = Color(0x99F1C40F)   // Žuta za polje NA koje se kreće
private val feedbackCorrectColor = Color(0x992ECC71) // Zelena za tačan odgovor
private val feedbackWrongColor = Color(0x99E74C3C)   // Crvena za pogrešan odgovor

/**
 * Glavni ekran za Modul 3, koji se menja u zavisnosti od stanja testa.
 */
@Composable
fun Module3Screen(viewModel: Module3ViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Prikazujemo različite komponente u zavisnosti od stanja testa
    when (uiState.testState) {
        TestState.SETUP -> SetupView(uiState, viewModel)
        TestState.IN_PROGRESS -> TestView(uiState, viewModel)
        TestState.FINISHED -> {
            // Prikazujemo test view i dijalog preko njega
            TestView(uiState, viewModel)
            TestEndDialog(uiState = uiState, onDismiss = { viewModel.onDismissDialog() })
        }
    }
}

/**
 * Prikaz za podešavanje testa pre početka.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupView(uiState: Module3UiState, viewModel: Module3ViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Modul 3: Interaktivni Parovi") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Izaberite figure za vežbu:", style = MaterialTheme.typography.titleMedium)

            // Paneli za biranje broja figura
            PieceCountSelector(PieceType.KNIGHT, uiState.pi<ctrl63>