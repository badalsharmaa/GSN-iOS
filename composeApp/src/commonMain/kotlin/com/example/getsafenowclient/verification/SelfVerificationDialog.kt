package com.example.getsafenowclient.verification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.EyeSlash
import compose.icons.fontawesomeicons.solid.Key
import compose.icons.fontawesomeicons.solid.Lock
import compose.icons.fontawesomeicons.solid.User
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.verification.SelfVerificationMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfVerificationDialog(
    methods: Set<SelfVerificationMethod>,
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    BasicAlertDialog(onDismissRequest, Modifier, DialogProperties(), {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    // Title
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 20.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        text = "Verify this device"
                    )

                    var selectedMethod: SelfVerificationMethod? by remember { mutableStateOf(null) }

                    if (selectedMethod == null) {
                        // Method picker
                        methods.forEach { method ->
                            when (method) {
                                is SelfVerificationMethod.AesHmacSha2RecoveryKey -> {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                "Verify with recovery key",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = FontAwesomeIcons.Solid.Key,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        modifier = Modifier.clickable { selectedMethod = method }
                                    )
                                }

                                is SelfVerificationMethod.AesHmacSha2RecoveryKeyWithPbkdf2Passphrase -> {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                "Verify with recovery passphrase",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = FontAwesomeIcons.Solid.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        modifier = Modifier.clickable { selectedMethod = method }
                                    )
                                }

                                is SelfVerificationMethod.CrossSignedDeviceVerification -> {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                "Verify with another device",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                imageVector = FontAwesomeIcons.Solid.User,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        modifier = Modifier.clickable { selectedMethod = method }
                                    )
                                }
                            }
                        }
                    } else {
                        var failure by remember { mutableStateOf(false) }
                        var inputVisibility by remember { mutableStateOf(false) }
                        var progress by remember { mutableStateOf(false) }
                        var action by remember { mutableStateOf<suspend () -> Unit>({}) }

                        when (val method = selectedMethod!!) {
                            is SelfVerificationMethod.AesHmacSha2RecoveryKey -> {
                                var key by remember { mutableStateOf("") }
                                action = {
                                    progress = true
                                    method.verify(key)
                                        .onSuccess { onDismissRequest() }
                                        .onFailure {
                                            progress = false
                                            failure = true
                                        }
                                }
                                OutlinedTextField(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth(),
                                    value = key,
                                    onValueChange = { key = it },
                                    label = { Text("Recovery key", style = MaterialTheme.typography.labelMedium) },
                                    isError = failure,
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    visualTransformation = if (inputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(
                                            modifier = Modifier,
                                            onClick = { inputVisibility = !inputVisibility }
                                        ) {
                                            Icon(
                                                imageVector = if (inputVisibility) FontAwesomeIcons.Solid.EyeSlash else FontAwesomeIcons.Solid.Eye,
                                                contentDescription = if (inputVisibility) "Hide" else "Show",
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                )
                            }

                            is SelfVerificationMethod.AesHmacSha2RecoveryKeyWithPbkdf2Passphrase -> {
                                var passphrase by remember { mutableStateOf("") }
                                action = {
                                    progress = true
                                    method.verify(passphrase)
                                        .onSuccess { onDismissRequest() }
                                        .onFailure {
                                            failure = true
                                            progress = false
                                        }
                                }
                                OutlinedTextField(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth(),
                                    value = passphrase,
                                    onValueChange = { passphrase = it },
                                    label = { Text("Recovery passphrase", style = MaterialTheme.typography.labelMedium) },
                                    isError = failure,
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    visualTransformation = if (inputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(
                                            modifier = Modifier,
                                            onClick = { inputVisibility = !inputVisibility }
                                        ) {
                                            Icon(
                                                imageVector = if (inputVisibility) FontAwesomeIcons.Solid.EyeSlash else FontAwesomeIcons.Solid.Eye,
                                                contentDescription = if (inputVisibility) "Hide" else "Show",
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                )
                            }

                            is SelfVerificationMethod.CrossSignedDeviceVerification -> {
                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth(),
                                    text = "Verification with another device is not implemented yet.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onDismissRequest() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("Cancel", style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(modifier = Modifier.size(12.dp))
                            Button(
                                onClick = { scope.launch { action() } },
                                enabled = !progress
                            ) {
                                if (progress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                }
                                Text("Verify", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        })
}