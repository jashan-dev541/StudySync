package com.example.studyfocusgo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyfocusgo.ui.theme.StudyFocusGoTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


enum class StudyTheme {
    ROSE_BLOOM,
    SAGE_CALM,
    LAVENDER_DREAM
}

data class ThemePalette(
    val primary: Color,
    val softSurface: Color,
    val background: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val track: Color
)

fun getThemePalette(theme: StudyTheme): ThemePalette {
    return when (theme) {
        StudyTheme.ROSE_BLOOM -> ThemePalette(
            primary = Color(0xFFD95783),
            softSurface = Color(0xFFF8DCE7),
            background = Color(0xFFFFF7F8),
            card = Color.White,
            text = Color(0xFF754456),
            muted = Color(0xFF987D89),
            track = Color(0xFFF3DCE5)
        )

        StudyTheme.SAGE_CALM -> ThemePalette(
            primary = Color(0xFF668B75),
            softSurface = Color(0xFFDDEBDD),
            background = Color(0xFFF7FAF4),
            card = Color.White,
            text = Color(0xFF385746),
            muted = Color(0xFF78877C),
            track = Color(0xFFDDE9DE)
        )

        StudyTheme.LAVENDER_DREAM -> ThemePalette(
            primary = Color(0xFF9275C7),
            softSurface = Color(0xFFE9DFF8),
            background = Color(0xFFFAF7FF),
            card = Color.White,
            text = Color(0xFF554477),
            muted = Color(0xFF89809C),
            track = Color(0xFFE9E0F3)
        )
    }
}

data class StudySubject(
    val name: String
)

data class ManagedTask(
    val title: String,
    val subject: String,
    val priority: String,
    val completed: Boolean = false,
    val durationMinutes: Int = 30
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StudyFocusGoTheme {
                StudySyncDashboard()
            }
        }
    }
}


@Composable
fun StudySyncDashboard() {
    var selectedTheme by remember {
        mutableStateOf(StudyTheme.ROSE_BLOOM)
    }
    val palette = getThemePalette(selectedTheme)


    var themeMenuExpanded by remember {
        mutableStateOf(false)
    }

    // Focus Mode state
    var showFocusScreen by remember {
        mutableStateOf(false)
    }
    var focusSchedule by remember {
        mutableStateOf<List<ScheduleItem>>(emptyList())
    }

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    val subjects = remember {
        mutableStateListOf(
            StudySubject("Mathematics"),
            StudySubject("Science"),
            StudySubject("Programming")
        )
    }

    val tasks = remember {
        mutableStateListOf<ManagedTask>()
    }

    var availableHours by remember {
        mutableIntStateOf(3)
    }

    var generatedSchedule by remember {
        mutableStateOf<List<ScheduleItem>>(emptyList())
    }

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            if(!showFocusScreen){
            NavigationBar(
                containerColor = palette.card
            ) {
                val tabs = listOf(
                    "Home",
                    "Studies",
                    "Schedule",
                    "Focus",
                    "Progress"
                )

                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.Book,
                    Icons.Default.CalendarMonth,
                    Icons.Default.Timer,
                    Icons.Default.BarChart
                )

                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        icon = {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = palette.primary,
                            selectedTextColor = palette.primary,
                            indicatorColor = palette.softSurface
                        )
                    )
                }
            }
        }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Keep the dashboard composed underneath Focus Mode.
            // This preserves your subjects and tasks.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            )
            {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "StudySync",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )

                    Box {
                        TextButton(
                            onClick = { themeMenuExpanded = true }
                        ) {
                            Text("🎨 Theme", color = palette.primary)
                        }

                        DropdownMenu(
                            expanded = themeMenuExpanded,
                            onDismissRequest = {
                                themeMenuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🌸 Rose Bloom") },
                                onClick = {
                                    selectedTheme = StudyTheme.ROSE_BLOOM
                                    themeMenuExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("🌿 Sage Calm") },
                                onClick = {
                                    selectedTheme = StudyTheme.SAGE_CALM
                                    themeMenuExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("💜 Lavender Dream") },
                                onClick = {
                                    selectedTheme = StudyTheme.LAVENDER_DREAM
                                    themeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Welcome back, Student! 👋",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )

                Text(
                    text = "Enjoy your holidays. Don't lose your progress.",
                    fontSize = 13.sp,
                    color = palette.muted
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = SimpleDateFormat(
                        "EEEE, d MMMM",
                        Locale.getDefault()
                    ).format(Date()),
                    color = palette.muted,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )


                Spacer(modifier = Modifier.height(24.dp))

// Quick overview
                Text(
                    text = "Your Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = palette.card
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "${subjects.size}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                            Text("Subjects", color = palette.muted)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = palette.card
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "${tasks.count { !it.completed }}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                            Text("Pending tasks", color = palette.muted)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = palette.card
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${tasks.count { it.completed }} of ${tasks.size} tasks completed",
                            fontWeight = FontWeight.SemiBold,
                            color = palette.text
                        )

                        Spacer(Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = {
                                if (tasks.isEmpty()) 0f
                                else tasks.count { it.completed }.toFloat() / tasks.size
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = palette.primary,
                            trackColor = palette.muted.copy(alpha = 0.2f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Quick Access",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Card(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = palette.softSurface
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📚", fontSize = 26.sp)

                                Text(
                                    "My Studies",
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Text(
                                    "Subjects & tasks",
                                    fontSize = 12.sp,
                                    color = palette.muted
                                )
                            }
                        }

                        Card(
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = palette.softSurface
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📅", fontSize = 26.sp)

                                Text(
                                    "My Schedule",
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Text(
                                    "Plan your day",
                                    fontSize = 12.sp,
                                    color = palette.muted
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Card(
                            onClick = { selectedTab = 3 },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = palette.softSurface
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⏱️", fontSize = 26.sp)

                                Text(
                                    "Focus Mode",
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Text(
                                    "Stay concentrated",
                                    fontSize = 12.sp,
                                    color = palette.muted
                                )
                            }
                        }

                        Card(
                            onClick = { selectedTab = 4 },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = palette.softSurface
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📊", fontSize = 26.sp)

                                Text(
                                    "My Progress",
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Text(
                                    "Track your growth",
                                    fontSize = 12.sp,
                                    color = palette.muted
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Small steps every day lead to big achievements.",
                    color = palette.muted,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

            }

            // Screen overlay

            if (selectedTab != 0 || showFocusScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.background)
                ) {
                    when {
                        showFocusScreen -> {
                            FocusModeScreen(
                                palette = palette,
                                schedule = focusSchedule,
                                onBack = {
                                    showFocusScreen = false
                                    selectedTab = 3
                                }
                            )
                        }

                        selectedTab == 1 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp)
                            ) {
                                Text(
                                    "📚 My Studies",
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Spacer(Modifier.height(16.dp))

                                SubjectTaskManager(
                                    palette = palette,
                                    subjects = subjects,
                                    tasks = tasks,
                                    availableHours = availableHours,
                                    onAvailableHoursChange = { availableHours = it },
                                    generatedSchedule = generatedSchedule,
                                    onGeneratedScheduleChange = {
                                        generatedSchedule = it
                                    },
                                    onOpenFocus = { schedule ->
                                        focusSchedule = schedule
                                        showFocusScreen = true
                                    },
                                    onOpenSchedule = { selectedTab = 2}
                                )
                            }
                        }


                        selectedTab == 2 -> {
                            val studyItems = generatedSchedule.filterNot { it.isBreak }
                            val breakItems = generatedSchedule.filter { it.isBreak }

                            val totalStudyMinutes = studyItems.sumOf { it.durationMinutes }
                            val totalBreakMinutes = breakItems.sumOf { it.durationMinutes }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp)
                            ) {
                                Text(
                                    "My Schedule",
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    "Your daily study planner",
                                    color = palette.muted,
                                    fontSize = 14.sp
                                )

                                Spacer(Modifier.height(20.dp))



                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    Text(
                                        text = "Available study time",
                                        fontSize = 14.sp,
                                        color = palette.muted,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        TextButton(
                                            onClick = {
                                                if (availableHours > 1) availableHours--
                                            }
                                        ) {
                                            Text(
                                                text = "−",
                                                fontSize = 26.sp,
                                                color = palette.primary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "$availableHours",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = palette.text
                                            )

                                            Text(
                                                text = if (availableHours == 1) "hour" else "hours",
                                                fontSize = 16.sp,
                                                color = palette.muted
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                if (availableHours < 12) availableHours++
                                            }
                                        ) {
                                            Text(
                                                text = "+",
                                                fontSize = 26.sp,
                                                color = palette.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))


                                    Button(
                                        onClick = {
                                            if (tasks.isEmpty()) {
                                                selectedTab = 1
                                            } else {
                                                generatedSchedule = generateStudySchedule(
                                                    tasks = tasks.toList(),
                                                    availableMinutes = availableHours * 60
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.primary
                                        )
                                    ) {
                                        Text(
                                            text = if (tasks.isEmpty()) {
                                                "Add Tasks to Get Started"
                                            } else {
                                                "Create Study Plan"
                                            },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }


                                Spacer(Modifier.height(20.dp))
                                Spacer(Modifier.height(20.dp))

                                if (generatedSchedule.isEmpty()) {

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = palette.card
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🗓️", fontSize = 40.sp)

                                            Spacer(Modifier.height(14.dp))

                                            Text(
                                                "Your schedule is waiting",
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.text
                                            )

                                            Spacer(Modifier.height(8.dp))

                                            Text(
                                                "Your study plan will appear here once you generate it using Smart Scheduler above.",
                                                color = palette.muted,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(Modifier.height(18.dp))

                                        }

                                    }
                                }else {

                                    // Daily summary
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = palette.card
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(18.dp)
                                        ) {
                                            Text(
                                                "Today's Overview",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.text
                                            )

                                            Spacer(Modifier.height(16.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column (
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ){
                                                    Text(
                                                        "${studyItems.size}",
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = palette.primary
                                                    )
                                                    Text("Sessions", color = palette.muted)
                                                }

                                                Column (
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        "${totalStudyMinutes / 60}h ${totalStudyMinutes % 60}m",
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = palette.primary
                                                    )
                                                    Text("Study Time", color = palette.muted)
                                                }

                                                Column (
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ){
                                                    Text(
                                                        "${breakItems.size}",
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = palette.primary
                                                    )
                                                    Text("Breaks", color = palette.muted)
                                                }
                                            }

                                            Spacer(Modifier.height(14.dp))

                                            Text(
                                                "Total break time: $totalBreakMinutes minutes",
                                                color = palette.muted,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(24.dp))

                                    Text(
                                        "YOUR TIMELINE",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.muted
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    generatedSchedule.forEachIndexed { index, item ->

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {

                                            // Timeline indicator
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(30.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .background(
                                                            color = if (item.isBreak)
                                                                palette.muted
                                                            else
                                                                palette.primary,
                                                            shape = CircleShape
                                                        )
                                                )

                                                if (index != generatedSchedule.lastIndex) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(105.dp)
                                                            .background(
                                                                palette.muted.copy(alpha = 0.3f)
                                                            )
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.width(10.dp))

                                            // Timeline card
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(bottom = 14.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = palette.card
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text(
                                                        item.time,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = palette.primary
                                                    )

                                                    Spacer(Modifier.height(8.dp))

                                                    Text(
                                                        if (item.isBreak)
                                                            "☕ Break Time"
                                                        else
                                                            "📚 ${item.title}",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = palette.text
                                                    )

                                                    Spacer(Modifier.height(6.dp))

                                                    Text(
                                                        if (item.isBreak)
                                                            "Relax and recharge"
                                                        else
                                                            "Focused study session",
                                                        fontSize = 13.sp,
                                                        color = palette.muted
                                                    )

                                                    Spacer(Modifier.height(8.dp))

                                                    Text(
                                                        "${item.durationMinutes} minutes",
                                                        fontSize = 12.sp,
                                                        color = palette.muted
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            focusSchedule = generatedSchedule
                                            selectedTab = 3
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Go to Focus Mode →")
                                    }
                                }
                            }
                        }


                        selectedTab == 3 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Text("⏱️", fontSize = 64.sp)

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    "Focus Mode",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Stay focused. One task at a time.",
                                    color = palette.muted
                                )

                                Spacer(Modifier.height(24.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = palette.card
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Your study plan",
                                            fontWeight = FontWeight.Bold,
                                            color = palette.text
                                        )

                                        Text(
                                            "${focusSchedule.count { !it.isBreak }} study sessions",
                                            color = palette.muted,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            "${focusSchedule.sumOf {
                                                if (!it.isBreak) it.durationMinutes else 0
                                            }} minutes of study time",
                                            color = palette.muted,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        focusSchedule = generatedSchedule
                                        showFocusScreen = true
                                    },
                                    enabled = generatedSchedule.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("▶ Start Focus Session")
                                }

                                if (generatedSchedule.isEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Generate a study schedule on Home first.",
                                        color = palette.muted
                                    )
                                }
                            }
                        }

                        selectedTab == 4 -> {
                            val completedTasks = tasks.count { it.completed }
                            val totalTasks = tasks.size
                            val pendingTasks = totalTasks - completedTasks

                            val progress = if (totalTasks > 0) {
                                completedTasks.toFloat() / totalTasks
                            } else {
                                0f
                            }

                            val completedMinutes = tasks
                                .filter { it.completed }
                                .sumOf { it.durationMinutes }

                            val totalMinutes = tasks.sumOf { it.durationMinutes }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp)
                            ) {
                                Text(
                                    "📊 My Progress",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.text
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Track your learning journey.",
                                    color = palette.muted
                                )

                                Spacer(Modifier.height(20.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = palette.card
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Text(
                                            "Overall Completion",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.text
                                        )

                                        Spacer(Modifier.height(16.dp))

                                        Text(
                                            "${(progress * 100).toInt()}%",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.primary
                                        )

                                        Spacer(Modifier.height(12.dp))

                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp),
                                            color = palette.primary,
                                            trackColor = palette.muted.copy(alpha = 0.2f)
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            "$completedTasks completed out of $totalTasks tasks",
                                            color = palette.muted
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = palette.card
                                        )
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("✅", fontSize = 24.sp)
                                            Text(
                                                "$completedTasks",
                                                fontSize = 25.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.primary
                                            )
                                            Text("Completed", color = palette.muted)
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = palette.card
                                        )
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("📌", fontSize = 24.sp)
                                            Text(
                                                "$pendingTasks",
                                                fontSize = 25.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.primary
                                            )
                                            Text("Pending", color = palette.muted)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = palette.card
                                    )
                                ) {
                                    Column(Modifier.padding(20.dp)) {
                                        Text(
                                            "Study Time",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = palette.text
                                        )

                                        Spacer(Modifier.height(12.dp))

                                        Text(
                                            "Completed: ${completedMinutes / 60}h ${completedMinutes % 60}m",
                                            color = palette.primary
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        Text(
                                            "Total planned: ${totalMinutes / 60}h ${totalMinutes % 60}m",
                                            color = palette.muted
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = palette.card
                                    )
                                ) {
                                    Column(Modifier.padding(20.dp)) {
                                        Text(
                                            "Subjects",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = palette.text
                                        )

                                        Spacer(Modifier.height(12.dp))

                                        if (subjects.isEmpty()) {
                                            Text("No subjects added yet.", color = palette.muted)
                                        } else {
                                            subjects.forEach { subject ->
                                                val subjectTasks = tasks.filter {
                                                    it.subject == subject.name
                                                }
                                                val subjectCompleted = subjectTasks.count {
                                                    it.completed
                                                }

                                                Text(
                                                    "${subject.name}: $subjectCompleted / ${subjectTasks.size} tasks",
                                                    color = palette.text,
                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // Full-screen Focus Mode appears above the dashboard.
            if (showFocusScreen) {
                FocusModeScreen(
                    palette = palette,
                    schedule = focusSchedule,
                    onBack = {
                        showFocusScreen = false
                        selectedTab = 3
                    }
                )
            }
        }
    }
}




data class ScheduleItem(
    val title: String,
    val time: String,
    val isBreak: Boolean = false,
    val durationMinutes: Int,
    val startMinutes: Int
)


@RequiresApi(Build.VERSION_CODES.GINGERBREAD)

fun generateStudySchedule(
    tasks: List<ManagedTask>,
    availableMinutes: Int
): List<ScheduleItem> {

    fun priorityRank(priority: String): Int =
        when (priority) {
            "High" -> 0
            "Medium" -> 1
            else -> 2
        }

    val pendingTasks = tasks
        .filterNot { it.completed }
        .sortedBy { priorityRank(it.priority) }

    val schedule = mutableListOf<ScheduleItem>()

    var currentMinutes = 9 * 60
    val endMinutes = currentMinutes + availableMinutes.coerceAtLeast(0)
    var studyMinutesSinceBreak = 0

    fun formatTime(minutes: Int): String {
        val hour = (minutes / 60) % 12
        val displayHour = if (hour == 0) 12 else hour
        val minute = minutes % 60
        val period = if (minutes / 60 < 12) "AM" else "PM"

        return "%d:%02d %s".format(
            displayHour, minute, period
        )
    }

    for (task in pendingTasks) {
        val duration = task.durationMinutes.coerceAtLeast(1)

        if (currentMinutes + duration > endMinutes) continue

        val start = currentMinutes
        val finish = start + duration

        schedule.add(
            ScheduleItem(
                title = "${task.subject}: ${task.title} (${task.priority})",
                time = "${formatTime(start)} – ${formatTime(finish)}",
                durationMinutes = duration,
                startMinutes = start
            )
        )

        currentMinutes = finish
        studyMinutesSinceBreak += duration

        if (
            studyMinutesSinceBreak >= 90 &&
            currentMinutes + 15 <= endMinutes
        ) {
            schedule.add(
                ScheduleItem(
                    title = "Take a break ☕",
                    time = "${formatTime(currentMinutes)} – ${formatTime(currentMinutes + 15)}",
                    isBreak = true,
                    durationMinutes = 15,
                    startMinutes = currentMinutes
                )
            )

            currentMinutes += 15
            studyMinutesSinceBreak = 0
        }
    }

    return schedule
}


@Composable
fun SubjectTaskManager(
    palette: ThemePalette,
    subjects: SnapshotStateList<StudySubject>,
    tasks: SnapshotStateList<ManagedTask>,
    availableHours: Int,
    onAvailableHoursChange: (Int) -> Unit,
    generatedSchedule: List<ScheduleItem>,
    onGeneratedScheduleChange: (List<ScheduleItem>) -> Unit,
    onOpenFocus: (List<ScheduleItem>) -> Unit,
    onOpenSchedule: () -> Unit
) {


    var showSubjectDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var subjectName by remember { mutableStateOf("") }
    var taskTitle by remember { mutableStateOf("") }
    var taskDeadline by remember { mutableStateOf("") }
    var taskDuration by remember { mutableStateOf("30") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name.orEmpty()) }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var priorityMenuExpanded by remember { mutableStateOf(false) }



    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "My Subjects 📚",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = palette.text
        )

        Button(
            onClick = { showSubjectDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add Subject")
        }

        subjects.forEach { subject ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = palette.softSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.text
                    )
                    Text(
                        text = "${tasks.count { it.subject == subject.name }} tasks",
                        color = palette.muted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Study Tasks ✏️",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = palette.text
            )

            TextButton(onClick = { showTaskDialog = true }) {
                Text("+ Add", color = palette.primary)
            }
        }

        if (tasks.isEmpty()) {
            Text(
                text = "No study tasks yet. Add one whenever you're ready.",
                modifier = Modifier.padding(vertical = 4.dp),
                color = palette.muted,
                fontSize = 14.sp
            )
        } else {
            tasks.forEachIndexed { index, task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = palette.card
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = { checked ->
                                tasks[index] = task.copy(completed = checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = palette.primary
                            )
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                color = palette.text,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${task.subject} • ${task.priority} priority",
                                color = palette.muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (tasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Your tasks are ready!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.text
                )

                Text(
                    text = "Head to Schedule to turn them into a study plan.",
                    fontSize = 13.sp,
                    color = palette.muted
                )

                TextButton(
                    onClick = { onOpenSchedule() }
                ) {
                    Text(
                        text = "Create Study Plan  →",
                        color = palette.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }


        if (showSubjectDialog) {
            AlertDialog(
                onDismissRequest = { showSubjectDialog = false },
                title = { Text("Add New Subject") },
                text = {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val name = subjectName.trim()
                            if (
                                name.isNotEmpty() &&
                                subjects.none { it.name.equals(name, ignoreCase = true) }
                            ) {
                                subjects.add(StudySubject(name))
                                subjectName = ""
                                showSubjectDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubjectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showTaskDialog) {
            AlertDialog(
                onDismissRequest = { showTaskDialog = false },
                title = { Text("Add Study Task") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Task title") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = taskDuration,
                            onValueChange = { value ->
                                taskDuration = value.filter(Char::isDigit)
                            },
                            label = { Text("Study time (minutes)") },
                            singleLine = true
                        )

                        if (subjects.isEmpty()) {
                            Text(
                                text = "Add a subject before creating a task.",
                                color = palette.muted
                            )
                        } else {
                            Box {
                                TextButton(onClick = { subjectMenuExpanded = true }) {
                                    Text("Subject: $selectedSubject")
                                }

                                DropdownMenu(
                                    expanded = subjectMenuExpanded,
                                    onDismissRequest = { subjectMenuExpanded = false }
                                ) {
                                    subjects.forEach { subject ->
                                        DropdownMenuItem(
                                            text = { Text(subject.name) },
                                            onClick = {
                                                selectedSubject = subject.name
                                                subjectMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Box {
                                TextButton(onClick = { priorityMenuExpanded = true }) {
                                    Text("Priority: $selectedPriority")
                                }

                                DropdownMenu(
                                    expanded = priorityMenuExpanded,
                                    onDismissRequest = { priorityMenuExpanded = false }
                                ) {
                                    listOf("High", "Medium", "Low").forEach { priority ->
                                        DropdownMenuItem(
                                            text = { Text(priority) },
                                            onClick = {
                                                selectedPriority = priority
                                                priorityMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val duration = taskDuration.toIntOrNull()

                            if (taskTitle.isNotBlank() && subjects.isNotEmpty() && duration != null && duration > 0) {
                                val taskSubject = subjects
                                    .firstOrNull { it.name == selectedSubject }
                                    ?.name
                                    ?: subjects.first().name

                                tasks.add(
                                    ManagedTask(
                                        title = taskTitle.trim(),
                                        subject = taskSubject,
                                        priority = selectedPriority,
                                        durationMinutes = duration
                                    )
                                )

                                taskTitle = ""
                                taskDuration = "30"
                                showTaskDialog = false
                            }
                        }
                    ) {
                        Text("Add Task")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTaskDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}



    enum class FocusType {
        FOCUS,
        SHORT_BREAK,
        LONG_BREAK
    }


    @Composable
    fun FocusModeScreen(
        palette: ThemePalette,
        schedule: List<ScheduleItem>,
        onBack: () -> Unit
    ) {
        var currentIndex by remember(schedule) {
            mutableIntStateOf(0)
        }

        var remainingSeconds by remember(schedule) {
            mutableIntStateOf(
                (schedule.firstOrNull()?.durationMinutes ?: 0) * 60
            )
        }

        var isRunning by remember(schedule) {
            mutableStateOf(false)
        }

        val currentSession = schedule.getOrNull(currentIndex)

        LaunchedEffect(isRunning, currentIndex) {
            while (isRunning && currentIndex < schedule.size) {
                delay(1000)

                if (remainingSeconds <= 1) {
                    if (currentIndex < schedule.lastIndex) {
                        currentIndex++
                        remainingSeconds =
                            schedule[currentIndex].durationMinutes * 60
                    } else {
                        remainingSeconds = 0
                        isRunning = false
                    }
                } else {
                    remainingSeconds--
                }
            }
        }

        BackHandler(
            enabled = true,
            onBack = onBack
        )

        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = palette.primary)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "FOCUS MODE",
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )
            }

            if (currentSession == null) {
                Text(
                    text = "No study plan available",
                    color = palette.text,
                    fontSize = 22.sp
                )

                Text(
                    text = "Generate your study plan first.",
                    color = palette.muted
                )

                Button(onClick = onBack) {
                    Text("Back to Dashboard")
                }
            } else {
                Text(
                    text = if (currentSession.isBreak) {
                        "You've earned a Break ☕"
                    } else {
                        "Time to Focus 🎯"
                    },
                    color = palette.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = palette.softSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = currentSession.title,
                            color = palette.text,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = currentSession.time,
                            color = palette.muted,
                            fontSize = 13.sp
                        )

                        Text(
                            text = "%02d:%02d".format(
                                remainingSeconds / 60,
                                remainingSeconds % 60
                            ),
                            color = palette.primary,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LinearProgressIndicator(
                            progress = {
                                if (currentSession.durationMinutes > 0) {
                                    1f - remainingSeconds.toFloat() /
                                            (currentSession.durationMinutes * 60)
                                } else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = palette.primary,
                            trackColor = palette.track
                        )

                        Text(
                            text = if (isRunning) "Session in progress" else "Ready to begin",
                            color = palette.muted
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    isRunning = !isRunning
                                },
                                enabled = remainingSeconds > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.primary
                                )
                            ) {
                                Text(if (isRunning) "Pause" else "Start")
                            }

                            OutlinedButton(
                                onClick = {
                                    isRunning = false
                                    currentIndex = 0
                                    remainingSeconds =
                                        schedule.first().durationMinutes * 60
                                }
                            ) {
                                Text("Restart Plan")
                            }
                        }
                    }
                }

                Text(
                    text = "YOUR SCHEDULE",
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )

                schedule.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                index == currentIndex -> palette.softSurface
                                else -> palette.card
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    index < currentIndex -> "✓"
                                    index == currentIndex -> "●"
                                    item.isBreak -> "☕"
                                    else -> "○"
                                },
                                color = palette.primary
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = palette.text,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = item.time,
                                    color = palette.muted,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "${item.durationMinutes} min",
                                color = palette.muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (!isRunning && remainingSeconds == 0) {
                    Text(
                        text = "Your study plan is complete! 🎉",
                        color = palette.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }


