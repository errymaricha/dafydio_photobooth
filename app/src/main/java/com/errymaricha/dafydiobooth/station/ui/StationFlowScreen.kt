package com.errymaricha.dafydiobooth.station.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StationFlowScreen(
    loginViewModel: LoginViewModel,
    templateListViewModel: TemplateListViewModel,
) {
    val loginState by loginViewModel.state.collectAsState()
    val templateState by templateListViewModel.state.collectAsState()
    var deviceCode by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Station Client", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = deviceCode,
            onValueChange = { deviceCode = it },
            label = { Text("Device Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { loginViewModel.login(deviceCode, apiKey) }) {
                Text(if (loginState.isLoading) "Logging in..." else "Login Device")
            }
            Button(onClick = { templateListViewModel.refresh() }) {
                Text(if (templateState.isLoading) "Refreshing..." else "Refresh Templates")
            }
        }
        loginState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        templateState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(templateState.templates) { item ->
                Text("${item.templateName} (${item.templateCode})")
            }
        }
    }
}
