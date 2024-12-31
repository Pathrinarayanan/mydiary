package com.example.diary

import android.icu.text.CaseMap.Title
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    data: DocumentSnapshot? =null,
    navcontroller: NavHostController,
    isEdit : Boolean,
    updateData: (String, String, Long) -> Unit = { _, _, _ -> },
    addData: (String, String, Long) -> Unit = { _, _, _ -> }
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = datePickerState.selectedDateMillis?.let {
        dateFormat.format(Date(it))
    } ?: dateFormat.format(Date(System.currentTimeMillis()))

    LaunchedEffect (data) {// update title when it comes from edit page
        data?.let {
            title = it.data?.get("title").toString()
            message = it.data?.get("message").toString()
            datePickerState.selectedDateMillis =  it.data?.get("date").toString().toLong()
        }
    }
    Column(
        modifier = Modifier
            .background(Color.White)
            .wrapContentSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TextField(
            value = title,
            onValueChange = {
                title = it
            },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(
                    "Set an Title",
                    color = colorResource(R.color.sandal_theme_font),
                    fontSize = 18.sp
                )
            }
        )
        Text(
            text = formattedDate,
            fontSize = 18.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth()
                .background(
                    colorResource(R.color.sandal)
                )
                .padding(16.dp)
                .clickable {
                    datePickerState.selectedDateMillis = null
                    showDatePicker = true
                },


            )
        if (showDatePicker && datePickerState.selectedDateMillis == null) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp)
            )
            Text("Selected date timestamp: ${datePickerState.selectedDateMillis ?: "no selection"}")

        }

        TextField(
            value = message,
            onValueChange = {
                message = it
            },
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .heightIn(250.dp, 450.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 18.sp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(
                    "How was your day",
                    color = colorResource(R.color.sandal_theme_font),
                    fontSize = 18.sp
                )
            }
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            onClick = {
                if(isEdit){
                    datePickerState.selectedDateMillis?.let {
                        updateData(title, message, it)
                    }
                }
                else {
                    if (datePickerState.selectedDateMillis == null) {
                        datePickerState.selectedDateMillis = System.currentTimeMillis()
                        datePickerState.selectedDateMillis?.let {
                            if (title != "" && message != "") {
                                addData(title, message, it)
                            }
                        }
                    } else {
                        datePickerState.selectedDateMillis?.let {
                            if (title != "" && message != "") {
                                addData(title, message, it)
                            }
                        }
                    }
                }

            }
        ) {
            Text("Save")
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navcontroller: NavHostController,
    onLoginClick: (String, String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val textgradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1271CA),
            Color(0xFF5D21DD)
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "MY\nDIARY",
                style = TextStyle(
                    brush = textgradient,
                    fontSize = 65.sp,
                    fontWeight = FontWeight.W700
                ),
                textAlign = TextAlign.Center
            )
        }
        TextField(
            value = email,
            onValueChange = {
                email = it
            },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null
                    )
                    Text("Enter your Email", color = colorResource(R.color.black), fontSize = 18.sp)
                }
            }
        )
        TextField(
            value = password,
            onValueChange = {
                password = it
            },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null
                    )
                    Text(
                        "Enter your Password",
                        color = colorResource(R.color.black),
                        fontSize = 18.sp
                    )
                }
            }
        )
        Text(
            text = "Forgot Password?",
            fontSize = 16.sp,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            onClick = {
                onLoginClick(email, password)
            },

            ) {
            Text("Sign In")
        }
        Text(
            "Create Account",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navcontroller.navigate("signup")
                },
            color = Color(0xFF1272ca),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600
        )

    }
}

@Composable
fun ItemViewPage(title: String, message :String, date : String, navcontroller: NavHostController) {
    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.weight(0.9f)) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                 navcontroller.popBackStack()
                            }
                    )
                    Text(
                        "Back",
                        fontSize = 18.sp
                    )
                }

            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(Color.White)
                    .fillMaxSize()
                    .padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = title,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .fillMaxWidth(),
                    fontWeight = FontWeight.W700

                    )

                Text(
                    text = date,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .fillMaxWidth()
                        .wrapContentHeight()
                    )

                Text(
                    text =  message,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .fillMaxWidth()
                        .wrapContentHeight()


                    )

            }
        }
    )

}


@Composable
fun HomeItems(data: QuerySnapshot, navcontroller: NavHostController, onDelete : (String) ->Unit) {
    val textgradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1271CA),
            Color(0xFF5D21DD)
        )
    )
    Column {

        Text(
            "MY DIARY",
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.white))
                .padding(vertical = 20.dp),
            style = TextStyle(
                brush = textgradient,
                fontSize = 24.sp,
                fontWeight = FontWeight.W700
            ),
            textAlign = TextAlign.Center
        )
        LazyColumn(
            modifier = Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxWidth()
                .fillMaxHeight()
        ) {

            data.forEach {
                item {
                    var expanded by remember { mutableStateOf(false) }
                    val title =it.data.get("title").toString()
                    val message = it.data.get("message").toString()
                    var dateTimeformat = SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault())
                    var dates = it.data.get("date")
                    var mydate = dateTimeformat.format(dates)
                    ListItem(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .clickable {
                                navcontroller.navigate("itempage/${URLEncoder.encode(title, "UTF-8")}/${URLEncoder.encode(message, "UTF-8")}/${URLEncoder.encode(mydate, "UTF-8")}")
                            }
                            .background(
                                shape = RoundedCornerShape(11.dp),
                                color = colorResource(R.color.sandal)
                            ),
                        headlineContent = {
                            Text(title, fontWeight = FontWeight.W600)
                        },
                        supportingContent = {
                            Text( message, maxLines = 1)
                        },
                        overlineContent = {
                            Text(mydate.toString())

                        },
                        trailingContent = {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    expanded = true
                                }
                            )
                            DropdownMenu(
                                onDismissRequest = {
                                    expanded = false
                                },
                                expanded = expanded
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text("edit")
                                    },
                                    onClick = {
                                        navcontroller.navigate("add/${it.id}")
                                        expanded = false

                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text("delete")
                                    },
                                    onClick = {
                                        onDelete(it.id)
                                        expanded = false
                                    }
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = colorResource(R.color.sandal))

                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navcontroller: NavHostController,
    signUp: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    val textgradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1271CA),
            Color(0xFF5D21DD)
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),

        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "MY\nDIARY",
                style = TextStyle(
                    brush = textgradient,
                    fontSize = 65.sp,
                    fontWeight = FontWeight.W700
                ),
                textAlign = TextAlign.Center
            )
        }
        TextField(
            value = email,
            onValueChange = {
                email = it
            },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null
                    )
                    Text("Enter your Email", color = colorResource(R.color.black), fontSize = 18.sp)
                }
            }
        )
        TextField(
            value = username,
            onValueChange = {
                username = it
            },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null
                    )
                    Text(
                        "Enter your Username",
                        color = colorResource(R.color.black),
                        fontSize = 18.sp
                    )
                }
            }
        )
        TextField(
            value = password,
            onValueChange = {
                password = it
            },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null
                    )
                    Text(
                        "Enter your Password",
                        color = colorResource(R.color.black),
                        fontSize = 18.sp
                    )
                }
            }
        )
        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            textStyle = TextStyle(fontSize = 18.sp),
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = colorResource(R.color.sandal),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null
                    )
                    Text(
                        "Confirm Password",
                        color = colorResource(R.color.black),
                        fontSize = 18.sp
                    )
                }
            }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = !isChecked
                }
            )
            Text(
                text = "I accept terms and conditions and privacy policy",
                fontSize = 12.sp,
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            onClick = {
                signUp(username, email, password, confirmPassword)
            },

            ) {
            Text("Create Account")
        }
        Text(
            "Login",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navcontroller.navigate("login")
                },
            color = Color(0xFF1272ca),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600
        )

    }
}

@Composable
fun HomeScreen(
    navcontroller: NavHostController,
    data: QuerySnapshot? = null,
    username: DocumentSnapshot? = null,
    onDeleteClick: (() -> Unit)? = null,
    onDeleteItemClick :  ((String) -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    updatePassword: ((String) -> Unit)? = null,
    updateUsername: ((String) -> Unit)? = null,
    onAddlick: (String, String, Long) -> Unit = { _, _, _ -> }, signout: () -> Unit
) {
    var pagerstate = rememberPagerState(0, pageCount = { 3 })
    var scope = rememberCoroutineScope()
    var showDropdownMenu by remember { mutableStateOf(false) }
    Scaffold(
        bottomBar = {
            TabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.sandal))
                    .padding(top = 20.dp, bottom = 20.dp),
                selectedTabIndex = pagerstate.currentPage,
            ) {
                Tab(
                    selected = (pagerstate.currentPage == 0),
                    onClick = {
                        scope.launch {
                            pagerstate.scrollToPage(0)
                        }

                    },
                    selectedContentColor = Color.Black,
                    modifier = Modifier.background(colorResource(R.color.sandal))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null,

                            )
                        Text(
                            text = "Home"
                        )
                    }
                }
                Tab(
                    selected = (pagerstate.currentPage == 1),
                    onClick = {
                        scope.launch {
                            pagerstate.scrollToPage(1)
                        }
                    },
                    selectedContentColor = Color.Black,
                    modifier = Modifier.background(colorResource(R.color.sandal))

                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,

                            )
                        Text(
                            text = "Add"
                        )
                    }
                }
                Tab(
                    selected = (pagerstate.currentPage == 2),
                    onClick = {
                        scope.launch {
                            pagerstate.scrollToPage(2)
                        }

                    },
                    selectedContentColor = Color.Black,
                    modifier = Modifier.background(colorResource(R.color.sandal))

                )


                {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,

                            )
                        Text(
                            text = "Profile"
                        )
                    }
                }
            }
        },
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.weight(0.9f)) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                navcontroller.popBackStack()
                            }
                    )
                    Text(
                        "Back",
                        fontSize = 18.sp
                    )
                }
                Row(modifier = Modifier.weight(0.1f)) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .weight(0.1f)
                            .padding(end = 16.dp)
                            .clickable {
                                showDropdownMenu = true

                            }
                    )
                    DropdownMenu(
                        modifier = Modifier,
                        onDismissRequest = {
                            showDropdownMenu = false
                        },
                        expanded = showDropdownMenu,

                        ) {
                        DropdownMenuItem(text = {
                            Text(
                                "logout"
                            )
                        },
                            onClick = {
                                signout()
                            }
                        )
                    }

                }
            }
        }
    ) {


        Box(
            modifier = Modifier
                .padding(it)
                .wrapContentHeight()
        ) {
            HorizontalPager(
                state = pagerstate
            ) {
                when (it) {
                    0 -> {
                        ItemsPage(data, navcontroller , onDelete = {
                            if (onDeleteItemClick != null) {
                                onDeleteItemClick(it)
                            }
                        })
                    }

                    1 -> {
                        AddScreen(navcontroller =navcontroller, isEdit = false) { title, msg, date ->
                            onAddlick(title, msg, date)
                        }
                    }

                    2 -> ProfileScreen(
                        username,
                        onLogoutClick = {
                            onLogoutClick?.invoke()
                        },
                        onDeleteClick = {
                            onDeleteClick?.invoke()
                        },
                        updatePassword = { mail ->
                            if (updatePassword != null) {
                                updatePassword(mail)
                            }
                        },
                        onUpdateUsernameClick = {
                            if (updateUsername != null) {
                                updateUsername(it)
                            }
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun ItemsPage(data: QuerySnapshot? = null, navcontroller: NavHostController, onDelete: ((String) -> Unit)?) {
    val textgradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1271CA),
            Color(0xFF5D21DD)
        )
    )

    if (data?.isEmpty == false) {
        HomeItems(data,navcontroller, onDelete = {id->
            if (onDelete != null) {
                onDelete(id)
            }
        })
    } else {
        Column(Modifier.background(Color.White)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 20.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center,

                ) {
                Text(
                    "MY DIARY",
                    style = TextStyle(
                        brush = textgradient,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W700
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 200.dp)
                        .size(100.dp)
                )
                Text(
                    "Create your First Memories",

                    )

                Button(
                    onClick = {
                        navcontroller.navigate("add")
                    },
                    modifier = Modifier
                        .width(150.dp)
                        .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.DarkGray
                    )
                ) {
                    Text(
                        "Create"
                    )
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: DocumentSnapshot?,
    onLogoutClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onUpdateUsernameClick: ((String) -> Unit)? = null,
    updatePassword: ((String) -> Unit)? = null
) {
    var myname by remember { mutableStateOf("") }
    var updateUsername by remember { mutableStateOf("") }
    var showDelete by remember { mutableStateOf(false) }
    var showPasswordUpdateDialog by remember { mutableStateOf(false) }
    var showusernameDialog by remember { mutableStateOf(false) }
    var password by remember {
        mutableStateOf("")
    }
        myname = username?.data?.get("name").toString()

    val textgradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1271CA),
            Color(0xFF5D21DD)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "MY DIARY",
                style = TextStyle(
                    brush = textgradient,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W700
                ),
                textAlign = TextAlign.Center
            )
        }
        Icon(
            Icons.Filled.Person,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .size(100.dp),
        )
        Text(
            text = "Hi ${myname}",
            fontWeight = FontWeight.W600,
            fontSize = 28.sp,
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            modifier = Modifier.width(180.dp),
            onClick = {
                showusernameDialog = true
            }
        ) {
            Text("Update Username")
        }

        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            modifier = Modifier.width(180.dp),
            onClick = {
                showPasswordUpdateDialog = true
            }
        ) {
            Text("Update Password")
        }
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            modifier = Modifier.width(180.dp),
            onClick = {
                showDelete = true
            }
        ) {
            Text("Delete Account")
        }
        if (showDelete) {
            AlertDialog(
                onDismissRequest = {

                    showDelete = false
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteClick?.invoke()
                            showDelete = false
                        }
                    ) {
                        Text("delete")
                    }

                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDelete = false
                        }
                    ) {
                        Text("cancel")
                    }
                },
                title = {
                    Text("Are you sure you want to delete Account?")

                }
            )
        }
        if (showPasswordUpdateDialog) {
            password = ""
            AlertDialog(
                onDismissRequest = {

                    showPasswordUpdateDialog = false
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPasswordUpdateDialog = false
                            if (updatePassword != null) {
                                updatePassword(password)
                            }

                        }
                    ) {
                        Text("update")
                    }

                },
                dismissButton = {
                    Button(
                        onClick = {
                            showPasswordUpdateDialog = false
                        }
                    ) {
                        Text("cancel")
                    }
                },
                title = {
                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = {
                            Text("Enter the password", color = Color.DarkGray)
                        }
                    )

                }
            )
        }
        if (showusernameDialog) {

            AlertDialog(
                onDismissRequest = {

                    showusernameDialog = false
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showusernameDialog = false

                            if (onUpdateUsernameClick != null) {
                                onUpdateUsernameClick(updateUsername)
                            }


                        }
                    ) {
                        Text("update")
                    }

                },
                dismissButton = {
                    Button(
                        onClick = {
                            showusernameDialog = false
                        }
                    ) {
                        Text("cancel")
                    }
                },
                title = {
                    TextField(
                        value = updateUsername,
                        onValueChange = {
                            updateUsername = it
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = {
                            Text("current name: ${myname}", color = Color.DarkGray)
                        }
                    )

                }
            )
        }
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            modifier = Modifier.width(180.dp),
            onClick = {
                onLogoutClick?.invoke()
            }
        ) {
            Text("Logout")
        }
    }
}