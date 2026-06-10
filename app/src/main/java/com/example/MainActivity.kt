package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SapphireDark
import com.example.ui.theme.SapphirePrimary
import com.example.ui.viewmodel.DentalLabViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: DentalLabViewModel = viewModel()
                MainAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: DentalLabViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // جمع بيانات الولايات من فيو موديل بشكل تفاعلي آمن
    val cases by viewModel.cases.collectAsStateWithLifecycle()
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val technicians by viewModel.technicians.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "معمل العلوي لتقنية الأسنان",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SapphireDark,
                        titleContentColor = Color.White
                    ),
                    modifier = Modifier.zIndex(2f)
                )
            }
        },
        bottomBar = {
            // شريط التنقل السفلي الاحترافي Material 3 مع pills جذابة متناسقة مع الهوية
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = SapphirePrimary,
                        indicatorColor = SapphirePrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "الطلبات") },
                    label = { Text("الطلبات", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = SapphirePrimary,
                        indicatorColor = SapphirePrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "الأطباء") },
                    label = { Text("الأطباء", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = SapphirePrimary,
                        indicatorColor = SapphirePrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.People, contentDescription = "الفنيين") },
                    label = { Text("الفنيين", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = SapphirePrimary,
                        indicatorColor = SapphirePrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "المصروفات") },
                    label = { Text("المصروفات", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = SapphirePrimary,
                        indicatorColor = SapphirePrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                    label = { Text("الإعدادات", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = SapphirePrimary,
                        indicatorColor = SapphirePrimary,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    cases = cases,
                    doctors = doctors,
                    technicians = technicians,
                    expenses = expenses,
                    onNavigateToTab = { tabIndex -> selectedTab = tabIndex }
                )
                1 -> CasesScreen(
                    cases = cases,
                    doctors = doctors,
                    technicians = technicians,
                    onAddCase = { c -> viewModel.addCase(c) },
                    onUpdateCase = { c -> viewModel.updateCase(c) },
                    onUpdateStatus = { id, status -> viewModel.updateCaseStatus(id, status) },
                    onAddPayment = { id, p -> viewModel.recordCasePayment(id, p) },
                    onDeleteCase = { c -> viewModel.deleteCase(c) },
                    onAddDoctor = { d -> viewModel.addDoctor(d) },
                    onAddTechnician = { t -> viewModel.addTechnician(t) }
                )
                2 -> DoctorsScreen(
                    doctors = doctors,
                    cases = cases,
                    onAddDoctor = { d -> viewModel.addDoctor(d) },
                    onUpdateDoctor = { d -> viewModel.updateDoctor(d) },
                    onDeleteDoctor = { d -> viewModel.deleteDoctor(d) }
                )
                3 -> TechniciansScreen(
                    technicians = technicians,
                    cases = cases,
                    onAddTechnician = { t -> viewModel.addTechnician(t) },
                    onUpdateTechnician = { t -> viewModel.updateTechnician(t) },
                    onDeleteTechnician = { t -> viewModel.deleteTechnician(t) }
                )
                4 -> ExpensesScreen(
                    expenses = expenses,
                    onAddExpense = { e -> viewModel.addExpense(e) },
                    onDeleteExpense = { e -> viewModel.deleteExpense(e) }
                )
                5 -> SettingsScreen(
                    viewModel = viewModel,
                    cases = cases,
                    doctors = doctors,
                    technicians = technicians,
                    expenses = expenses
                )
            }
        }
    }
}
