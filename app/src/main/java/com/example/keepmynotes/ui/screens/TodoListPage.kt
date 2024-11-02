package com.example.keepmynotes.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.keepmynotes.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.keepmynotes.model.TodoItem
import com.example.keepmynotes.ui.theme.lightBlack
import com.example.keepmynotes.ui.theme.lightBlue
import com.example.keepmynotes.ui.theme.veryLightBlack
import com.example.keepmynotes.ui.viewmodel.AuthViewModel
import com.example.keepmynotes.ui.viewmodel.TodoViewModel
import com.example.keepmynotes.utils.RestrictedAPI
import com.example.keepmynotes.utils.Utils
import com.example.keepmynotes.utils.Utils.SCREEN_AUTH
import com.example.keepmynotes.utils.Utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ToDoListPage(navController: NavController) {

    val todoViewModel : TodoViewModel = hiltViewModel()
    val authViewModel : AuthViewModel = hiltViewModel()

    val todoList by todoViewModel.todoList.observeAsState()
    val isSavingTodo by todoViewModel.isSavingTodo.observeAsState()
    val deletingTodoID by todoViewModel.isDeletingTodo.observeAsState()
    val todoErrorText by todoViewModel.todoErrorText.observeAsState()
    val authenticationState by authViewModel.authenticationState.observeAsState()

    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var isMenuShown by rememberSaveable {
        mutableStateOf(false)
    }

    var searchText by rememberSaveable {
        mutableStateOf("")
    }

    if (!todoErrorText.isNullOrBlank()) {
        showToast(LocalContext.current, todoErrorText!!)
        todoViewModel.resetErrorText()
    }

    if (authenticationState == Utils.AuthenticationState.UNAUTHENTICATED) {
        navController.navigate(SCREEN_AUTH)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)) {
        Row {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    todoViewModel.searchTodo(searchText)
                },
                modifier = Modifier
                    .weight(1f)
                    .border(width = 6.dp, color = lightBlue, shape = RoundedCornerShape(20.dp)),
                placeholder = {
                    Text(text = "Search here ...")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                )
            )

            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                IconButton(onClick = {
                    isMenuShown = isMenuShown.not()
                }) {
                    Image(painter = painterResource(id = R.drawable.baseline_person_24), contentDescription = "delete icon", modifier = Modifier.size(40.dp))
                }

                DropdownMenu(expanded = isMenuShown, onDismissRequest = {
                    isMenuShown = isMenuShown.not()
                }) {
                    Text(text = "Add Todo",
                        modifier = Modifier
                            .padding(10.dp)
                            .clickable {
                                showDialog = true
                                isMenuShown = isMenuShown.not()
                            },
                        fontSize = 16.sp,
                    )
                    Text(text = "Log Out",
                        modifier = Modifier
                            .padding(10.dp)
                            .clickable {
                                todoViewModel.deleteAllTodo()
                                authViewModel.logOut()
                                isMenuShown = isMenuShown.not()
                            },
                        fontSize = 16.sp,
                        )
                }
            }
        }

        if (showDialog || isSavingTodo == true) {
            CreateTodoDialog(isSavingTodo = isSavingTodo ?: false, onClose = {
                showDialog = false
            }, onSubmit = { title, description ->
                todoViewModel.addTodo(title, description)
                showDialog = false
            })
        }

        if (todoList.isNullOrEmpty()) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "OPPS !! No item found", modifier = Modifier.padding(5.dp), fontSize = 24.sp)
                Image(painter = painterResource(id = R.drawable.baseline_add_circle_outline_24), contentDescription = "Add todo", modifier = Modifier
                    .clickable(enabled = true, onClick = {
                        showDialog = true
                    })
                    .padding(5.dp)
                    .size(52.dp))
                Text(text = "Click to add a new todo item", modifier = Modifier.padding(5.dp), fontSize = 18.sp, )
            }
        } else {
            todoList?.let {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .wrapContentHeight()) {
                    itemsIndexed(it){ index, item ->
                        TodoListItem(item,
                            deletingTodoID = deletingTodoID ?: "",
                            onDeleteTodo = {
                                todoViewModel.deleteTodo(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodoListItem(todoItem: TodoItem,
                 deletingTodoID : String,
                 onDeleteTodo: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .background(color = lightBlue)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todoItem.title,
                modifier = Modifier.padding(3.dp),
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = todoItem.description,
                modifier = Modifier.padding(3.dp),
                fontSize = 16.sp,
                color = lightBlack
            )
            Text(
                text = getDate(todoItem.createdAt),
                modifier = Modifier.padding(3.dp),
                fontSize = 14.sp,
                color = veryLightBlack
            )
        }
        IconButton(onClick = {
            if (deletingTodoID.isBlank()) {
                onDeleteTodo()
            }
        }) {
            if (deletingTodoID == todoItem.id) {
                CircularProgressIndicator(color = Color.Black)
            } else {
                Image(painter = painterResource(id = R.drawable.baseline_delete_sweep_24),
                    contentDescription = "delete icon",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

fun getDate(createdAt: Long): String {
    val date = Date(createdAt)
    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
    return sdf.format(date)
}