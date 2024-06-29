package cn.ac.lz233.emblematix.ui.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.ac.lz233.emblematix.App
import cn.ac.lz233.emblematix.logic.dao.ConfigDao

@Composable
fun ConfigChip(text: String, key: String, defaultValue: Boolean, onclick: () -> Unit) {
    var config by remember { mutableStateOf(App.sp.getBoolean(key, defaultValue)) }
    FilterChip(
        modifier = Modifier.padding(start = 5.dp, end = 5.dp),
        selected = config,
        onClick = {
            config = !config
            App.editor.putBoolean(key, config).apply()
            onclick()
        },
        label = {
            Text(text)
        },
        leadingIcon = if (config) {
            {
                Icon(
                    imageVector = Icons.Outlined.Done,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    contentDescription = ""
                )
            }
        } else {
            null
        }
    )
}

@Composable
fun SingleChoiceConfigChipGroup(modifier: Modifier, key: String, defaultValue: String, vararg chips: Pair<String, () -> Unit>) {
    var config by remember { mutableStateOf(App.sp.getString(key, defaultValue.lowercase())) }
    Row(modifier = modifier) {
        chips.forEach {
            FilterChip(
                modifier = Modifier
                    .padding(start = 5.dp, end = 5.dp)
                    .weight(1f),
                selected = config == it.first.lowercase(),
                onClick = {
                    config = it.first.lowercase()
                    App.editor.putString(key, it.first.lowercase()).apply()
                    it.second()
                },
                label = {
                    Text(it.first)
                },
                leadingIcon = if (config == it.first.lowercase()) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                            contentDescription = ""
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}