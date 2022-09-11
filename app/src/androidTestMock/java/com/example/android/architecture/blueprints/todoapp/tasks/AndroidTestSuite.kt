package com.example.android.architecture.blueprints.todoapp.tasks

import com.example.android.architecture.blueprints.todoapp.tasks.addedittask.AzzumAddTaskTests
import com.example.android.architecture.blueprints.todoapp.tasks.addedittask.AzzumEditTasksTests
import com.example.android.architecture.blueprints.todoapp.tasks.taskdetail.AzzumTaskDetailFragmentTests
import com.example.android.architecture.blueprints.todoapp.tasks.tasks.AzzumTaskListFragmentTests
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
    AzzumAddTaskTests::class,
    AzzumEditTasksTests::class,
    AzzumTaskDetailFragmentTests::class,
    AzzumTaskListFragmentTests::class,
    AzzumTasksActivityTests::class
)
class AndroidTestSuite {
}