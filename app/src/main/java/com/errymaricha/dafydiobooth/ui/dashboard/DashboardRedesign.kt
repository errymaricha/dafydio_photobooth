package com.errymaricha.dafydiobooth.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private object T {
    val surface = Color(0xFFF8F9FF)
    val surfaceLow = Color(0xFFEFF4FF)
    val surfaceContainer = Color(0xFFE5EEFF)
    val surfaceHigh = Color(0xFFDCE9FF)
    val surfaceHighest = Color(0xFFD3E4FE)
    val onSurface = Color(0xFF0B1C30)
    val onSurfaceVar = Color(0xFF45464D)
    val outlineVar = Color(0xFFC6C6CD)
    val primary = Color(0xFF000000)
    val onPrimary = Color(0xFFFFFFFF)
    val secondary = Color(0xFF006A61)
    val secondaryContainer = Color(0xFF86F2E4)
    val error = Color(0xFFBA1A1A)
}

private val displayLg = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp)
private val headlineMd = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp)
private val bodyMd = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
private val labelLg = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
private val labelSm = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)

sealed class DashRoute(val route: String, val label: String) {
    data object Quick : DashRoute("quick", "Quick Booth")
    data object Template : DashRoute("template", "Template")
    data object Recent : DashRoute("recent", "Recent")
    data object Event : DashRoute("event", "Event")
    data object Setup : DashRoute("setup", "Setup")
}

@Composable
fun DashboardApp() {
    val nav = rememberNavController()
    Scaffold(containerColor = T.surface, bottomBar = { BottomBar(nav) }) { pad ->
        NavHost(navController = nav, startDestination = DashRoute.Quick.route, modifier = Modifier.padding(pad)) {
            composable(DashRoute.Quick.route) { DashboardScreen() }
            composable(DashRoute.Template.route) { Placeholder("Template") }
            composable(DashRoute.Recent.route) { Placeholder("Recent") }
            composable(DashRoute.Event.route) { Placeholder("Event") }
            composable(DashRoute.Setup.route) { Placeholder("Setup") }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val routes = listOf(DashRoute.Quick, DashRoute.Template, DashRoute.Recent, DashRoute.Event, DashRoute.Setup)
    val current by nav.currentBackStackEntryAsState()
    val route = current?.destination?.route
    NavigationBar(containerColor = T.surfaceLow) {
        routes.forEach { r ->
            NavigationBarItem(
                selected = route == r.route,
                onClick = {
                    nav.navigate(r.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Text(r.label.take(1), style = labelLg) },
                label = { Text(r.label, style = labelSm) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = T.onSurface,
                    selectedTextColor = T.onSurface,
                    indicatorColor = T.surfaceContainer,
                    unselectedIconColor = T.onSurfaceVar,
                    unselectedTextColor = T.onSurfaceVar,
                ),
            )
        }
    }
}

@Composable
fun DashboardScreen() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(T.surface)) {
        val mobile = maxWidth < 1024.dp
        Column(Modifier.fillMaxSize()) {
            TopBar()
            if (mobile) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StationCard()
                    TemplateCard()
                    MetricsRow()
                    ActivityCard()
                }
            } else {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StationCard()
                        TemplateCard()
                    }
                    Column(Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricsRow()
                        ActivityCard()
                    }
                }
            }
        }
        if (!mobile) {
            HardwareCheck(Modifier.align(Alignment.TopEnd).padding(top = 70.dp, end = 22.dp))
        }
        LaunchButton(Modifier.align(Alignment.BottomEnd).padding(16.dp))
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(Color(0xFFF8FAFC)).border(1.dp, Color(0xFFE2E8F0)).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("H", style = labelLg, color = T.onSurface)
        Spacer(Modifier.width(8.dp))
        Text("DafydioBooth", style = labelLg.copy(fontWeight = FontWeight.Black, fontSize = 28.sp), color = T.onSurface)
        Spacer(Modifier.weight(1f))
        Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFD1FAE5)) {
            Text("  * STATION ONLINE  ", style = labelSm, color = Color(0xFF065F46), modifier = Modifier.padding(vertical = 6.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("o", style = labelLg, color = T.onSurface)
    }
}

@Composable
private fun BaseCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, T.outlineVar),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun StationCard() = BaseCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column {
            Text("Station Status", style = headlineMd, color = T.onSurface)
            Text("Booth ID: #DB-742-XP", style = bodyMd, color = T.onSurfaceVar)
        }
        Text("[]", color = T.secondary)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = T.secondaryContainer.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("o", color = T.secondary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Connected", style = labelLg, color = T.secondary)
                Text("LATENCY: 12ms", style = labelSm, color = T.secondary)
            }
            Text("||||", color = T.secondary)
        }
    }
    Row(Modifier.fillMaxWidth()) {
        Text("UPTIME", style = labelSm, color = T.onSurfaceVar, modifier = Modifier.weight(1f))
        Text("08h 42m 15s", style = labelSm, color = T.onSurface)
    }
    Box(Modifier.fillMaxWidth().height(3.dp).background(T.surfaceContainer, RoundedCornerShape(999.dp))) {
        Box(Modifier.fillMaxWidth(0.92f).height(3.dp).background(T.secondary, RoundedCornerShape(999.dp)))
    }
}

@Composable
private fun TemplateCard() = BaseCard {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text("Template Assets", style = headlineMd, color = T.onSurface)
        Surface(color = T.error, shape = RoundedCornerShape(bottomStart = 10.dp)) {
            Text("UPDATE READY", style = labelSm, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(56.dp).background(T.onSurface, RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Corporate_Gala_v2", style = labelLg, color = T.onSurface)
            Text("Size: 42.5 MB", style = bodyMd, color = T.onSurfaceVar)
            Text("SYNC NOW", style = labelSm, color = T.secondary)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(color = T.surfaceLow, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
            Column(Modifier.padding(10.dp)) {
                Text("OVERLAYS", style = labelSm, color = T.onSurfaceVar)
                Text("12/12", style = headlineMd, color = T.onSurface)
            }
        }
        Surface(color = T.surfaceLow, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
            Column(Modifier.padding(10.dp)) {
                Text("BACKGROUNDS", style = labelSm, color = T.onSurfaceVar)
                Text("04", style = headlineMd, color = T.onSurface)
            }
        }
    }
}

@Composable
private fun MetricsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard("TOTAL SESSIONS", "128", "+12% vs last hour", Modifier.weight(1f), showProgress = false)
        MetricCard("PRINT QUEUE", "03", "pending", Modifier.weight(1f), showProgress = true)
        MetricCard("CLOUD SYNC", "100%", "All assets synced", Modifier.weight(1f), showProgress = false)
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier, showProgress: Boolean) = BaseCard(modifier) {
    Text("*", style = labelLg, color = T.onSurfaceVar)
    Text(title, style = labelSm, color = T.onSurfaceVar)
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value, style = displayLg.copy(fontSize = 52.sp, lineHeight = 54.sp), color = T.primary)
        if (title == "PRINT QUEUE") {
            Spacer(Modifier.width(4.dp))
            Text(subtitle, style = bodyMd, color = T.onSurfaceVar)
        }
    }
    if (title != "PRINT QUEUE") {
        Text(subtitle, style = labelSm, color = if (title == "TOTAL SESSIONS") T.secondary else T.onSurfaceVar)
    }
    if (showProgress) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f).height(4.dp).background(T.secondary, RoundedCornerShape(999.dp)))
            Box(Modifier.weight(1f).height(4.dp).background(T.secondary, RoundedCornerShape(999.dp)))
            Box(Modifier.weight(1f).height(4.dp).background(T.secondary, RoundedCornerShape(999.dp)))
            Box(Modifier.weight(1f).height(4.dp).background(T.surfaceContainer, RoundedCornerShape(999.dp)))
        }
    }
}

data class Activity(val time: String, val event: String, val status: String, val color: Color, val action: String)

@Composable
private fun ActivityCard() {
    val rows = listOf(
        Activity("14:52:10", "Session #428 Completed", "SUCCESS", Color(0xFF16A34A), "view"),
        Activity("14:50:02", "Print Request Sent", "PROCESSING", Color(0xFF0F766E), "print"),
        Activity("14:48:45", "Camera Recalibration", "SYSTEM", Color(0xFF64748B), "reset"),
        Activity("14:45:12", "Session #427 Aborted", "CANCELLED", Color(0xFFB91C1C), "info"),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, T.outlineVar),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(T.surfaceLow).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Recent Activity", style = headlineMd, color = T.onSurface)
            Spacer(Modifier.weight(1f))
            Text("View All Logs", style = labelSm, color = T.secondary)
        }
        Row(Modifier.fillMaxWidth().background(T.surfaceContainer).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("TIME", style = labelSm, color = T.onSurfaceVar, modifier = Modifier.weight(0.18f))
            Text("EVENT", style = labelSm, color = T.onSurfaceVar, modifier = Modifier.weight(0.42f))
            Text("STATUS", style = labelSm, color = T.onSurfaceVar, modifier = Modifier.weight(0.25f))
            Text("ACTION", style = labelSm, color = T.onSurfaceVar, modifier = Modifier.weight(0.15f))
        }
        LazyColumn {
            items(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().border(0.5.dp, T.outlineVar).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(row.time, style = bodyMd, color = T.onSurface, modifier = Modifier.weight(0.18f))
                    Text(row.event, style = bodyMd, color = T.onSurface, modifier = Modifier.weight(0.42f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Box(Modifier.weight(0.25f)) {
                        Surface(color = row.color.copy(alpha = 0.2f), shape = RoundedCornerShape(999.dp)) {
                            Text(row.status, style = labelSm, color = row.color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    Box(Modifier.weight(0.15f), contentAlignment = Alignment.CenterEnd) {
                        Text(row.action, style = labelSm, color = T.onSurfaceVar)
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareCheck(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = T.surfaceHighest.copy(alpha = 0.7f), shape = RoundedCornerShape(10.dp), shadowElevation = 6.dp) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(999.dp), color = T.secondary, modifier = Modifier.size(30.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("ok", style = labelSm, color = Color.White) }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Hardware Check", style = labelLg, color = T.onSurface)
                Text("All components performing optimally.", style = labelSm, color = T.onSurfaceVar)
            }
        }
    }
}

@Composable
private fun LaunchButton(modifier: Modifier = Modifier) {
    Button(
        onClick = {},
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = T.primary, contentColor = T.onPrimary),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        Text("LAUNCH EVENT", style = labelLg)
    }
}

@Composable
private fun Placeholder(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = headlineMd, color = T.onSurface)
    }
}

@Preview(name = "Dashboard Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun DashboardTabletPreview() { MaterialTheme { DashboardApp() } }

@Preview(name = "Dashboard Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun DashboardMobilePreview() { MaterialTheme { DashboardApp() } }
