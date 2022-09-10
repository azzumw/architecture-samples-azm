package com.example.android.architecture.blueprints.todoapp.taskdetail


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.`is`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@ExperimentalCoroutinesApi
@MediumTest
@RunWith(AndroidJUnit4::class)
class AzzumTaskDetailFragmentTests {

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val context =  InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var repository: TasksRepository
    private val uiDevice: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun initRepository() {
        repository = FakeRepository()
        ServiceLocator.tasksRepository = repository

    }

    @After
    fun cleanup() = runTest {
        ServiceLocator.resetRepository()
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

    //launch TaskDetailFragment
    @Test
    fun activeTaskDetails_DisplayedInUI() {

        //GIVEN - an active task
        val activeTask = Task("Task1", "Desc1")
        repository.saveTaskBlocking(activeTask)

        //WHEN - TaskDetailFragment is launched to display task
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle()
        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //THEN - Task Details are displayed on the screen
        onView(withId(R.id.task_detail_title_text)).check(matches(withText("Task1")))
        onView(withId(R.id.task_detail_description_text)).check(matches(withText("Desc1")))
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isNotChecked()))
    }

    @Test
    fun completedTaskDetails_DisplayedInUI() {
        //GIVEN - a completed task
        val completedTask = Task("Completed Task", "This task is marked as completed!", true)
        repository.saveTaskBlocking(completedTask)

        //WHEN - Detail fragment is launched
        val bundle = TaskDetailFragmentArgs(completedTask.id).toBundle()
        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.task_detail_title_text)).check(matches(withText("Completed Task")))
        onView(withId(R.id.task_detail_description_text)).check(matches(withText("This task is marked as completed!")))
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isChecked()))

    }

    @Test
    fun activeTask_isMarkedComplete_showsSnackBar() {
        //GIVEN - an active task
        val activeTask = Task("Task1", "Desc1")
        repository.saveTaskBlocking(activeTask)

        //WHEN - Details fragment is launched
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle()
        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //and the task is marked complete
        onView(withId(R.id.task_detail_complete_checkbox))
            .check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        //TODO: visit snackbar again
        //THEN - the snackbar is shown with the correct text
        //implement condition watcher
//        onView(withId(com.google.android.material.R.id.snackbar_text))
//            .check(matches(withText(R.string.task_marked_complete)))
        val snackbarMsg = context.resources.getString(R.string.task_marked_complete)
        uiDevice.findObject(UiSelector().text(snackbarMsg)).waitUntilGone(1000)

        //check repo - task is marked completed
        val result = repository.getTasksBlocking(true) as Result.Success
        assertThat(result.data.first().isCompleted, `is`(true))

    }

    @Test
    fun completedTask_isActivated_showsSnackBar() {
        //GIVEN - a completed task
        val completedTask = Task("Completed Task","This task needs to be activated",true)
        repository.saveTaskBlocking(task = completedTask)

        //WHEN - detail fragment is launched
        val bundle = TaskDetailFragmentArgs(completedTask.id).toBundle()
        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle,R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //and the task is activated
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isChecked()))
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isNotChecked()))

        //THEN - verify that:
        //snack bar is shown
        val snackbarMsg = context.resources.getString(R.string.task_marked_complete)
        uiDevice.findObject(UiSelector().className(Snackbar::class.java).textContains(snackbarMsg))

    }

    @Test
    fun editTask_navigatesToAddEditTask() {
        //GIVEN - an active task
        val activeTask = Task("New Task","Description")
        repository.saveTaskBlocking(activeTask)

        //WHEN - detail fragment is launched
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle()
        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle,R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //THEN - clicking Edit fab navs to AddEditFragment screen
        val mockedNav = mock(NavController::class.java)

        scenario.onFragment{
            mockedNav.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(it.requireView(), mockedNav)
        }

        onView(withId(R.id.edit_task_fab)).perform(click())

        //ln 186-188: this throws NoClassDefFoundError: Failed resolution of: Lorg/opentest4j/AssertionError
        //tried to play around with the version but the issue persists
        //https://github.com/mockito/mockito/issues/1716
        verify(mockedNav).navigate(
            TaskDetailFragmentDirections.actionTaskDetailFragmentToAddEditTaskFragment(activeTask.id,activeTask.title)
        )
    }

    @Test
    fun navtest() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext())

        //GIVEN - an active task
        val activeTask = Task("New Task","Description")
        repository.saveTaskBlocking(activeTask)

        //and detail fragment is launched
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle()

        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.task_detail_fragment_dest)
            Navigation.setViewNavController(it.requireView(), navController)
        }

        dataBindingIdlingResource.monitorFragment(scenario)

        //WHEN - edit task fab is clicked
        onView(withId(R.id.edit_task_fab)).perform(click())

        //THEN - verify we navigate to add_edit_task_fragment
        assertThat(navController.currentDestination?.id, `is`(R.id.add_edit_task_fragment_dest))

    }
}