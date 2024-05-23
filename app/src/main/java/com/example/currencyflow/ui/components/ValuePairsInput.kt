package com.example.currencyflow.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.currencyflow.R

@Composable
fun ValuePairsInput(
    valuePairs: List<Pair<String, String>>,
    onValueChanged: (Int, String, String) -> Unit,
    onRemovePair: (Int) -> Unit
) {
    valuePairs.forEachIndexed { index, (value1, value2) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column {
                        Row {
                           CurrencyDropDownMenuL()
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(55.dp)
                    )
                    Column {
                        Row {
                            CurrencyDropDownMenuR()
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                )
                HorizontalDivider()
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        modifier = Modifier.width(140.dp),
                        value = value1,
                        onValueChange = { newValue ->
                            onValueChanged(index, newValue, valuePairs[index].second)
                        }
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.round_swap_horiz_40),
                        contentDescription = null
                    )

                    OutlinedTextField(
                        modifier = Modifier.width(140.dp),
                        value = value2,
                        onValueChange = { newValue ->
                            onValueChanged(index, valuePairs[index].first, newValue)
                        }
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )
                Icon(
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            onRemovePair(index)
                        },
                    imageVector = ImageVector.vectorResource(id = R.drawable.round_close_25),
                    contentDescription = null
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )
    }
}