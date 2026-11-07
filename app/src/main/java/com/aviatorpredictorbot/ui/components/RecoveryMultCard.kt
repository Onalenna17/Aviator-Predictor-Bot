package com.aviatorpredictorbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.ui.theme.LightBorder
import com.aviatorpredictorbot.ui.theme.LightCard
import com.aviatorpredictorbot.ui.theme.WaitingGrey

/** "RECOVERY MULT" input card (a place to note a recovery multiplier, e.g. 1.01 - 4.44). */
@Composable
fun RecoveryMultCard(modifier: Modifier = Modifier) {
    var value by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(LightCardShape)
            .background(LightCard)
            .border(1.dp, LightBorder, LightCardShape)
            .padding(20.dp)
            .testTag("recovery_mult_card")
    ) {
        LightFieldLabel("RECOVERY MULT (similar decimals e.g. 1.01 - 4.44)")

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { new ->
                if (new.all { it.isDigit() || it == '.' } && new.count { it == '.' } <= 1) {
                    value = new
                }
            },
            placeholder = {
                Text(
                    text = "e.g. 4.41",
                    color = WaitingGrey,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = TextStyle(fontSize = 22.sp, textAlign = TextAlign.Center),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = LightFieldShape,
            colors = lightFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recovery_mult_input")
        )
    }
}
