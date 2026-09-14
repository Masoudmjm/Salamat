package ir.salamat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.salamat.core.datetime.JalaliDate
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.ui.theme.SalamatTheme
import ir.salamat.ui.AppUiState
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(
    state: AppUiState,
    onNavigateToFamily: () -> Unit,
    onNavigateToQuickTools: () -> Unit,
    onNavigateToAddProfile: () -> Unit,
    onNavigateToMemberDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeProfile = state.activeProfile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            if (activeProfile != null) {
                ActiveProfileHeroCard(
                    profile = activeProfile,
                    isPersian = state.isPersian,
                    onClick = {
                        onNavigateToMemberDetail?.invoke(activeProfile.id) ?: onNavigateToFamily()
                    }
                )
            } else {
                EmptyProfileHeroCard(
                    isPersian = state.isPersian,
                    onAddProfile = onNavigateToAddProfile
                )
            }
        }

        item {
            Text(
                text = if (state.isPersian) "دسترسی سریع" else "Quick Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = if (state.isPersian) "واکسیناسیون" else "Vaccines",
                    subtitle = if (state.isPersian) "برنامه کشوری" else "National Schedule",
                    icon = Icons.Default.Favorite,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activeProfile != null) {
                            onNavigateToMemberDetail?.invoke(activeProfile.id) ?: onNavigateToQuickTools()
                        } else {
                            onNavigateToQuickTools()
                        }
                    }
                )
                QuickActionCard(
                    title = if (state.isPersian) "پایش رشد" else "Growth Tracker",
                    subtitle = if (state.isPersian) "قد، وزن و BMI" else "Height & Weight",
                    icon = Icons.Default.DateRange,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activeProfile != null) {
                            onNavigateToMemberDetail?.invoke(activeProfile.id) ?: onNavigateToQuickTools()
                        } else {
                            onNavigateToQuickTools()
                        }
                    }
                )
            }
        }

        item {
            Text(
                text = if (state.isPersian) "وضعیت سلامت و یادآورها" else "Health Status & Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (activeProfile != null) {
                            if (state.isPersian) {
                                "برای ${activeProfile.name} همه واکسن‌ها و چکاپ‌ها تا این تاریخ طبق برنامه در جریان است."
                            } else {
                                "All immunization records for ${activeProfile.name} are on track."
                            }
                        } else {
                            if (state.isPersian) {
                                "برای مشاهده یادآورهای واکسن و چکاپ، ابتدا یک عضو خانواده اضافه کنید."
                            } else {
                                "Add a family member to see tailored immunization alerts."
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActiveProfileHeroCard(
    profile: Profile,
    isPersian: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(profile.avatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val jalali = profile.birthDate.toJalali()
                val birthText = if (isPersian) {
                    "متولد: ${jalali.formatPersian()}"
                } else {
                    "Born: ${profile.birthDate}"
                }
                val typeText = if (profile.type == ProfileType.CHILD) {
                    if (isPersian) "کودک" else "Child"
                } else {
                    if (isPersian) "بزرگسال" else "Adult"
                }
                Text(
                    text = "$typeText • $birthText",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun EmptyProfileHeroCard(
    isPersian: Boolean,
    onAddProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddProfile),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPersian) "افزودن اولین عضو خانواده" else "Add First Family Member",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPersian) "برای پایش واکسیناسیون و رشد کودک" else "Start tracking vaccinations & growth",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenActiveProfilePreview() {
    val sampleChild = Profile(
        id = "p1",
        name = "کیان رستمی",
        birthDate = LocalDate(2023, 8, 20),
        gender = Gender.MALE,
        type = ProfileType.CHILD,
        avatarColor = 0xFF0A686D.toInt(),
        createdAt = 0L
    )
    SalamatTheme(isRtl = true) {
        HomeScreen(
            state = AppUiState(
                profiles = listOf(sampleChild),
                activeProfile = sampleChild,
                isPersian = true,
                isLoading = false
            ),
            onNavigateToFamily = {},
            onNavigateToQuickTools = {},
            onNavigateToAddProfile = {},
            onNavigateToMemberDetail = {}
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyProfilePreview() {
    SalamatTheme(isRtl = true) {
        HomeScreen(
            state = AppUiState(
                profiles = emptyList(),
                activeProfile = null,
                isPersian = true,
                isLoading = false
            ),
            onNavigateToFamily = {},
            onNavigateToQuickTools = {},
            onNavigateToAddProfile = {},
            onNavigateToMemberDetail = {}
        )
    }
}
