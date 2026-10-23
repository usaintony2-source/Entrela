package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EntrelaViewModel
import com.example.ui.Screen
import com.example.ui.components.EntrelaGradientButton
import com.example.ui.theme.*

private enum class ForgotStep {
    ENTER_EMAIL,
    ENTER_CODE,
    RESET_PASSWORD,
    SUCCESS
}

@Composable
fun ForgotPasswordScreen(viewModel: EntrelaViewModel) {
    var step by remember { mutableStateOf(ForgotStep.ENTER_EMAIL) }
    var email by remember { mutableStateOf("alex@entrela.app") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EntrelaDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (step == ForgotStep.ENTER_EMAIL || step == ForgotStep.SUCCESS) {
                            viewModel.navigateTo(Screen.LOGIN)
                        } else if (step == ForgotStep.ENTER_CODE) {
                            step = ForgotStep.ENTER_EMAIL
                        } else if (step == ForgotStep.RESET_PASSWORD) {
                            step = ForgotStep.ENTER_CODE
                        }
                    },
                    modifier = Modifier.testTag("forgot_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Password Recovery",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StepPill(title = "1. Email", isActive = step == ForgotStep.ENTER_EMAIL, isCompleted = step > ForgotStep.ENTER_EMAIL, modifier = Modifier.weight(1f))
                StepPill(title = "2. Code", isActive = step == ForgotStep.ENTER_CODE, isCompleted = step > ForgotStep.ENTER_CODE, modifier = Modifier.weight(1f))
                StepPill(title = "3. Reset", isActive = step == ForgotStep.RESET_PASSWORD, isCompleted = step == ForgotStep.SUCCESS, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Surface(
                    color = EntrelaHeartRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaHeartRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            when (step) {
                ForgotStep.ENTER_EMAIL -> {
                    Text(
                        text = "Enter your registered email address to receive a secure 6-digit recovery code.",
                        color = EntrelaTextSecondary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address", color = EntrelaTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EntrelaPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_email_input")
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    EntrelaGradientButton(
                        text = "Send Recovery Code →",
                        onClick = {
                            if (!email.contains("@") || email.isBlank()) {
                                errorMessage = "Please enter a valid email address."
                            } else {
                                val generated = viewModel.requestPasswordReset(email.trim())
                                generatedCode = generated
                                code = generated // Pre-fill convenience or display in hint
                                step = ForgotStep.ENTER_CODE
                            }
                        },
                        testTag = "forgot_send_code_button"
                    )
                }

                ForgotStep.ENTER_CODE -> {
                    Text(
                        text = "We sent a 6-digit verification code to $email. Enter it below to verify your identity.",
                        color = EntrelaTextSecondary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Code Hint Banner
                    Surface(
                        color = EntrelaSecondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaSecondary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔑", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Verification Code (Demo)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Code: ${generatedCode ?: "849201"}",
                                    color = EntrelaPrimaryVariant,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            if (it.length <= 6) code = it.filter { ch -> ch.isDigit() }
                            errorMessage = null
                        },
                        label = { Text("6-Digit Code", color = EntrelaTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = EntrelaSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_code_input")
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    EntrelaGradientButton(
                        text = "Verify Code →",
                        onClick = {
                            if (code.length != 6) {
                                errorMessage = "Please enter the complete 6-digit verification code."
                            } else if (generatedCode != null && code != generatedCode) {
                                errorMessage = "Invalid code. Please check and try again."
                            } else {
                                step = ForgotStep.RESET_PASSWORD
                            }
                        },
                        testTag = "forgot_verify_code_button"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Didn't receive code? Resend",
                        color = EntrelaPrimaryVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                val newCode = viewModel.requestPasswordReset(email.trim())
                                generatedCode = newCode
                                code = newCode
                            }
                            .padding(8.dp)
                    )
                }

                ForgotStep.RESET_PASSWORD -> {
                    Text(
                        text = "Create a strong new password for your account.",
                        color = EntrelaTextSecondary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMessage = null },
                        label = { Text("New Password (min 6 chars)", color = EntrelaTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EntrelaPrimary) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_new_password_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Confirm New Password", color = EntrelaTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EntrelaSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = outlinedTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_confirm_password_input")
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    EntrelaGradientButton(
                        text = "Save New Password",
                        onClick = {
                            if (newPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                            } else if (newPassword != confirmPassword) {
                                errorMessage = "Passwords do not match."
                            } else {
                                val ok = viewModel.resetPasswordWithCode(code, newPassword)
                                if (ok) {
                                    step = ForgotStep.SUCCESS
                                } else {
                                    errorMessage = "Reset session expired. Please restart."
                                }
                            }
                        },
                        testTag = "forgot_save_password_button"
                    )
                }

                ForgotStep.SUCCESS -> {
                    Spacer(modifier = Modifier.height(20.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EntrelaOnlineGreen,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Password Reset Complete!",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your password has been updated securely. You can now log in with your new credentials.",
                        color = EntrelaTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    EntrelaGradientButton(
                        text = "Back to Sign In",
                        onClick = { viewModel.navigateTo(Screen.LOGIN) },
                        testTag = "forgot_success_back_to_login"
                    )
                }
            }
        }
    }
}

@Composable
private fun StepPill(
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isCompleted -> EntrelaSecondary.copy(alpha = 0.4f)
        isActive -> EntrelaPrimary.copy(alpha = 0.3f)
        else -> EntrelaDarkSurfaceVariant
    }
    val textColor = when {
        isCompleted || isActive -> Color.White
        else -> EntrelaTextMuted
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
