package com.pranshulgg.clockmaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranshulgg.clockmaster.ui.components.tiles.ActionTile
import com.pranshulgg.clockmaster.ui.components.tiles.CategoryTile
import com.pranshulgg.clockmaster.ui.components.tiles.DialogOptionTile
import com.pranshulgg.clockmaster.ui.components.tiles.SingleSwitchTile
import com.pranshulgg.clockmaster.ui.components.tiles.SwitchTile
import com.pranshulgg.clockmaster.ui.components.tiles.TextTile

sealed class SettingTile {
    abstract val title: String
    abstract val description: String?

    data class TextTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
    ) : SettingTile()


    data class SwitchTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val enabled: Boolean = true
    ) : SettingTile()

    data class SingleSwitchTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val enabled: Boolean = true
    ) : SettingTile()

    data class CategoryTile(
        override val title: String,
        override val description: String? = null,
        val leading: Int,
        val color: Color,
        val onColor: Color,
        val onClick: () -> Unit

    ) : SettingTile()

    data class DialogOptionTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val options: List<String>,
        val selectedOption: String?,
        val onOptionSelected: (String) -> Unit,
        val optionLabel: (String) -> String = { it }
    ) : SettingTile()

    data class ActionTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val onClick: () -> Unit
    ) : SettingTile()
}


@Composable
fun SettingSection(
    tiles: List<SettingTile>,
    title: String? = null,
    primarySwitch: Boolean = false,
    errorTile: Boolean = false
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        title?.let {
            Text(
                text = it,
                modifier = Modifier.padding(bottom = 10.dp, top = 10.dp, start = 3.dp),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W700
            )
        }



        tiles.forEachIndexed { index, tile ->
            val isFirst = index == 0
            val isLast = index == tiles.lastIndex
            val isOnly = tiles.size == 1
            val primarySwitch = primarySwitch


            val shape = when {
                primarySwitch -> RoundedCornerShape(50.dp)
                isOnly -> RoundedCornerShape(18.dp)
                isFirst -> RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 2.6.dp,
                    bottomEnd = 2.6.dp
                )

                isLast -> RoundedCornerShape(
                    topStart = 2.6.dp,
                    topEnd = 2.6.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                )

                else -> RoundedCornerShape(2.6.dp)
            }

            when (tile) {
                is SettingTile.TextTile -> TextTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape
                )

                is SettingTile.CategoryTile -> CategoryTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    color = tile.color,
                    iconColor = tile.onColor,
                    onClick = tile.onClick
                )


                is SettingTile.DialogOptionTile -> DialogOptionTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    options = tile.options,
                    selectedOption = tile.selectedOption,
                    onOptionSelected = tile.onOptionSelected,
                    optionLabel = tile.optionLabel
                )

                is SettingTile.ActionTile -> ActionTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    onClick = tile.onClick
                )

                is SettingTile.SwitchTile -> SwitchTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    checked = tile.checked,
                    onCheckedChange = tile.onCheckedChange,
                    shapes = shape,
                    switchEnabled = tile.enabled
                )

                is SettingTile.SingleSwitchTile -> SingleSwitchTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    checked = tile.checked,
                    onCheckedChange = tile.onCheckedChange,
                    switchEnabled = tile.enabled
                )
            }
        }
    }
}

