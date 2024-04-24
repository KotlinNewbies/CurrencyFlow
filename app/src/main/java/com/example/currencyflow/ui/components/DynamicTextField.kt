package com.example.currencyflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R

@Composable
fun DynamicTextField(

) {
    val valueStateList = remember { mutableStateListOf("") }
    val valueStateList2 = remember { mutableStateListOf("") }
    valueStateList.forEachIndexed { index, _ ->
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .width(150.dp),
                value = valueStateList[index],
                onValueChange = {
                    valueStateList[index] = it
                }
            )
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.swap_horizontal),
                contentDescription = null
            )
            OutlinedTextField(
                modifier = Modifier
                    .width(150.dp),
                value = valueStateList2[index],
                onValueChange = {
                    valueStateList2[index] = it
                }
            )
        }
    }
}

