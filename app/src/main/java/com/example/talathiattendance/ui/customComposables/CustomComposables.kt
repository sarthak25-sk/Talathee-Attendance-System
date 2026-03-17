package com.example.talathiattendance.ui.customComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.talathiattendance.ui.theme.md_theme_dark_outline
import com.example.talathiattendance.ui.theme.md_theme_dark_outlineVariant
import com.example.talathiattendance.ui.theme.md_theme_light_outline
import com.example.talathiattendance.ui.theme.md_theme_light_outlineVariant


@Composable
fun CenterCircularProgressBar() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ContainerBox(
    modifier: Modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.surface).systemBarsPadding(),
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
){
    Box(modifier = modifier, content = content, contentAlignment = contentAlignment)
}

@Composable
fun ContainerColumn(
    modifier: Modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.surface).systemBarsPadding(),
    content: @Composable ColumnScope.() -> Unit
){
    Column(modifier = modifier, content = content)
}



@Composable
fun AppTextField(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    onChange: (String) -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyBoardActions: KeyboardActions = KeyboardActions(),
    isEnabled: Boolean = true
) {
    OutlinedTextField(
        modifier = modifier,
        value = text,
        onValueChange = onChange,
        leadingIcon = leadingIcon,
        textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = keyboardType),
        keyboardActions = keyBoardActions,
        enabled = isEnabled,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = if (isSystemInDarkTheme()) md_theme_dark_outlineVariant else md_theme_light_outlineVariant,
            unfocusedBorderColor = if (isSystemInDarkTheme()) md_theme_dark_outline else md_theme_light_outline,
            disabledBorderColor = Color.Gray,
            disabledTextColor = Color.Black
        ),
        placeholder = {
            Text(text = placeholder, style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f)))
        }
    )
}

@Composable
fun ProgressDialog(showDialog:Boolean,onDismissRequest:()->Unit) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment= Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun VSpace(space: Dp) {
    Spacer(modifier = Modifier
        .height(space))
}

@Composable
fun HSpace(space: Dp) {
    Spacer(modifier = Modifier
        .width(space))
}
@Composable
private fun DisposableEffectWithLifeCycle(
    onResume: () -> Unit,
    onPause: () -> Unit,
) {

    val context = LocalContext.current

    // Safely update the current lambdas when a new one is provided
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

//    Toast.makeText(
//        context,
//        "DisposableEffectWithLifeCycle composition ENTER",
//        Toast.LENGTH_SHORT
//    ).show()

    val currentOnResume by rememberUpdatedState(onResume)
    val currentOnPause by rememberUpdatedState(onPause)

    // If `lifecycleOwner` changes, dispose and reset the effect
    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for lifecycle events
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
//                    Toast.makeText(
//                        context,
//                        "DisposableEffectWithLifeCycle ON_CREATE",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
                Lifecycle.Event.ON_START -> {
//                    Toast.makeText(
//                        context,
//                        "DisposableEffectWithLifeCycle ON_START",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
                Lifecycle.Event.ON_RESUME -> {
                    currentOnResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    currentOnPause()
                }
                Lifecycle.Event.ON_STOP -> {
//                    Toast.makeText(
//                        context,
//                        "DisposableEffectWithLifeCycle ON_STOP",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
                Lifecycle.Event.ON_DESTROY -> {
//                    Toast.makeText(
//                        context,
//                        "DisposableEffectWithLifeCycle ON_DESTROY",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
                else -> {}
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)

//            Toast.makeText(
//                context,
//                "DisposableEffectWithLifeCycle composition EXIT",
//                Toast.LENGTH_SHORT
//            )
//                .show()
        }
    }

    Column(modifier = Modifier.background(Color(0xff03A9F4))) {
        Text(
            text = "Disposable Effect with lifecycle",
            color = Color.White,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
    }
}
