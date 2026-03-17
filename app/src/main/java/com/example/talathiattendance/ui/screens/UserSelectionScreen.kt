package com.example.talathiattendance.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.talathiattendance.domain.utils.popNavigate
import com.example.talathiattendance.domain.utils.superNavigate
import com.example.talathiattendance.ui.customComposables.HSpace
import com.example.talathiattendance.ui.customComposables.VSpace
import com.example.talathiattendance.ui.navigation.Screens
import com.example.talathiattendance.ui.theme.md_theme_dark_surface
import com.example.talathiattendance.ui.theme.md_theme_light_surface

@Composable
fun UserSelectionScreen(navController: NavHostController) {
    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) md_theme_dark_surface else md_theme_light_surface),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "You are.....",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            VSpace(50.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .weight(1f)
                    .clickable {
                        navController.apply {
                            currentBackStackEntry?.savedStateHandle?.set(
                                key = "userType",
                                value = "talathi"
                            )

                            popNavigate(Screens.Login)
                        }
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(50.dp),
                        imageVector = Icons.Filled.HowToReg,
                        contentDescription = "Talathi Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VSpace(space = 20.dp)

                    Text(
                        text = "Talathi",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HSpace(space = 10.dp)
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .weight(1f)
                    .clickable {
                        navController.apply {
                            currentBackStackEntry?.savedStateHandle?.set(
                                key = "userType",
                                value = "admin"
                            )

                            superNavigate(Screens.Login)
                        }
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(50.dp),
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = "Admin Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VSpace(space = 20.dp)
                    Text(
                        text = "Admin(DC)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }


        }
    }
}

@Composable
@Preview
fun UserSelectionScreenPreview() {
    UserSelectionScreen(rememberNavController())
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun UserSelectionScreenDarkPreview() {
    UserSelectionScreen(rememberNavController())
}