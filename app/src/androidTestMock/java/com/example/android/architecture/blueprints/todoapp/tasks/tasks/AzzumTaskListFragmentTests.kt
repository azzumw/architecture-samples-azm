package com.example.android.architecture.blueprints.todoapp.tasks.tasks

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragmentDirections
import com.example.android.architecture.blueprints.todoapp.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class AzzumTaskListFragmentTests {

    private lateinit var repository: TasksRepository
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setUp() {
        repository = FakeRepository()
        ServiceLocator.tasksRepository = repository
    }

    @Before
    fun registerIdlingResources() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @After
    fun tearDown() = runTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun noTasks_displaysNoTaskIconAndMessage() {

        //GIVEN - no tasks exists
        //WHEN - task fragment is launched
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        //THEN - no task icon is displayed with message
        onView(withId(R.id.no_tasks_icon)).check(matches(isDisplayed()))
        onView(withText(R.string.no_tasks_all)).check(matches(isDisplayed()))

    }

    @Test
    fun oneActiveTask_showsActiveTask() {

        //GIVEN - an active task
        val activeTask = Task("New Task", "Desc")
        repository.saveTaskBlocking(activeTask)

        ////WHEN - task fragment is launched
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //THEN - the task is shown
        //I could've used RecyclerViewActions... I feel it's an overkill
        onView(withText("New Task")).check(matches(isDisplayed()))
        onView(withId(R.id.complete_checkbox)).check(matches(isNotChecked()))

        val result = repository.getTasksBlocking(true) as Result.Success
        assertThat(result.data, `is`(not(emptyList())))
        assertThat(result.data.size, `is`(1))
        assertThat(result.data.first().title, `is`("New Task"))
        assertThat(result.data.first().isCompleted, `is`(false))

    }

    @Test
    fun oneCompletedTask_showsCompletedTask() {

        //GIVEN - a compelted task
        val completedTask = Task("New Task", "Desc", true)
        repository.saveTaskBlocking(completedTask)

        ////WHEN - task fragment is launched
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //THEN - the task is shown
        //I could've used RecyclerViewActions... i feel its an overkill
        onView(withText("New Task")).check(matches(isDisplayed()))
        onView(withId(R.id.complete_checkbox)).check(matches(isChecked()))
        val result = repository.getTasksBlocking(true) as Result.Success

        assertThat(result.data, `is`(not(emptyList())))
        assertThat(result.data.size, `is`(1))
        assertThat(result.data.first().title, `is`("New Task"))
        assertThat(result.data.first().isCompleted, `is`(true))

    }

    @Test
    fun activeAndCompletedAreShown() {

        //GIVEN - an active task
        val activeTask = Task("Active Task", "Active Desc")
        // a completed task
        val completedTask = Task("Completed Task", "Completed Desc", true)

        repository.saveTaskBlocking(activeTask)
        repository.saveTaskBlocking(completedTask)

        //WHEN - task fragment is launched
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //THEN - the active/completed tasks are shown
        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText("Active Task"))
            )
        ).check(matches(isNotChecked()))

        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText("Completed Task"))
            )
        ).check(matches(isChecked()))

        val result = repository.getTasksBlocking(true) as Result.Success

        assertThat(result.data, `is`(not(emptyList())))
        assertThat(result.data.size, `is`(2))

        assertThat(result.data.first().title, `is`("Active Task"))
        assertThat(result.data.first().isCompleted, `is`(false))

        assertThat(result.data.last().title, `is`("Completed Task"))
        assertThat(result.data.last().isCompleted, `is`(true))

    }

    @Test
    fun clickATask_navigateToTaskDetailFragment() {

        val activeTask = Task("Active Task", "Active Task Desc", false, "actId1")
        val completedTask = Task("Completed Task", "Completed Task Desc", true, "comId1")

        repository.saveTaskBlocking(activeTask)
        repository.saveTaskBlocking(completedTask)

        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val mockedNav = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), mockedNav)
        }

        // WHEN - Click on the first list item
        onView(withId(R.id.tasks_list))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Active Task")), click()
                )
            )

        // THEN - Verify that we navigate to the first detail screen
        verify(mockedNav).navigate(
            TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment("actId1")
        )
    }

    @Test
    fun clickAddTaskFab_navigatesToAddEditTaskFragment() {

        // GIVEN - task fragment is launched
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val mockedNav = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), mockedNav)
        }

        //WHEN - add task fab is clicked
        onView(withId(R.id.add_task_fab)).perform(click())

        //THEN - verify that we navigate to AddEditTaskFragment
        verify(mockedNav).navigate(
            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(null, "New Task")
        )
    }
}