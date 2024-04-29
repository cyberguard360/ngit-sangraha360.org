package com.example.sg360.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


 /**
     * A Composable function that displays a list of apps and allows the user to select one.
     *
     * @param apklist The list of apps to display.
     * @param onItemSelected The callback function to be invoked when an item is selected.
     */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppList(apklist: List<String>, onItemSelected: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf("Select Application") }
    println("apklist")
    println(apklist)

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {isExpanded = !isExpanded}
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedIndex,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)}
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                apklist.forEachIndexed { index, packageName ->
                    DropdownMenuItem(
                        text = { Text(text = packageName) },
                        onClick = {
                            selectedIndex = apklist[index]
                            isExpanded = false
                            onItemSelected(selectedIndex) // Call the callback function with the selected item
                        }
                    )
                }
            }
        }

        Text(text = "Currently Selected: $selectedIndex")
    }
}
