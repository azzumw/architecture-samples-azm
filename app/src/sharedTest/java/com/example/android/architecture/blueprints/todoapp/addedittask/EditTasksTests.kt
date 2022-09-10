package com.example.android.architecture.blueprints.todoapp.addedittask

import android.os.SystemClock
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.getTasksBlocking
import com.example.android.architecture.blueprints.todoapp.util.monitorFragment
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*


@MediumTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class EditTasksTests {

    companion object{
        const val ACTIVE_TASK_TITLE = "Active Task"
        const val ACTIVE_TASK_DESC = "Active Task Description"
        const val ACTIVE_TASK_ID = "actId1"
        const val ACTIVE_NEW_TASK_TITLE = "New Active Task"
        const val ACTIVE_NEW_TASK_DESC = "New Active Task Description"

        const val COMPLETED_TASK_TITLE = "Completed Task"
        const val COMPLETED_TASK_dESC = "Completed Task Description"
        const val COMPLETED_TASK_ID = "compId1"
        const val COMPLETED_NEW_TASK_TITLE = "New Completed Task"
        const val COMPLETED_NEW_TASK_DESC = "New Completed Task Description"
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var repository: FakeRepository
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())


    @Before
    fun setUp() {
        repository = FakeRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun tearDown() = runTest{
        ServiceLocator.resetRepository()
    }

    @Test
    fun editTask_noChanges() {
        //GIVEN - an active task
        val activeTask = Task(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC,false, ACTIVE_TASK_ID)
        repository.saveTaskBlocking(activeTask)

        //launch edit fragment
        val bundle = AddEditTaskFragmentArgs(ACTIVE_TASK_ID, ACTIVE_TASK_TITLE).toBundle()
        val scenario = launchFragmentInContainer<AddEditTaskFragment>(bundle, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val mockedNav = mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.requireView(),mockedNav)
        }

        //WHEN - save fab is clicked
        onView(withId(R.id.save_task_fab)).perform(click())

        doNothing().`when`(mockedNav).navigate(
            AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
        )

        //repo doesn't contain changes
        val result = repository.getTasksBlocking(true) as Result.Success
        assertThat(result.data.first().title,`is`(ACTIVE_TASK_TITLE))
        assertThat(result.data.first().description,`is`(ACTIVE_TASK_DESC))
        assertThat(result.data.size, `is`(1))
    }


    @Test
    fun editTask_changeTitleAndDesc_changesTaskDetails() {
        //GIVEN - an active task
        val activeTask = Task(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC,false, ACTIVE_TASK_ID)
        repository.saveTaskBlocking(activeTask)

        //launch edit fragment
        val bundle = AddEditTaskFragmentArgs(ACTIVE_TASK_ID, ACTIVE_TASK_TITLE).toBundle()
        val scenario = launchFragmentInContainer<AddEditTaskFragment>(bundle, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val mockedNav = mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.requireView(),mockedNav)
        }

        //WHEN - title and description values are changed, and saved fab is clicked
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText(ACTIVE_NEW_TASK_TITLE))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText(ACTIVE_NEW_TASK_DESC))
        onView(withId(R.id.save_task_fab)).perform(click())

        doCallRealMethod().`when`(mockedNav).navigate(
            AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
        )

        //THEN - repo contains changes
        val result = repository.getTasksBlocking(true) as Result.Success
        assertThat(result.data.first().title,`is`(ACTIVE_NEW_TASK_TITLE))
        assertThat(result.data.first().description,`is`(ACTIVE_NEW_TASK_DESC))
        assertThat(result.data.first().title, `is`(not(ACTIVE_TASK_TITLE)))
        assertThat(result.data.first().description, `is`(not(ACTIVE_TASK_DESC)))
    }

    @Test
    fun editTask_changeTitleOnly_changesTitleDetailsOnly() {
        //GIVEN - an active task
        val activeTask = Task(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC,false, ACTIVE_TASK_ID)
        repository.saveTaskBlocking(activeTask)

        //launch the edit fragment
        val bundle = AddEditTaskFragmentArgs(ACTIVE_TASK_ID, ACTIVE_TASK_TITLE).toBundle()
        val scenario = launchFragmentInContainer<AddEditTaskFragment>(bundle,R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //mock the nav
        val mockedNav = mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.requireView(),mockedNav)
        }

        //WHEN - only the title is changed, save fab is clicked
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText(ACTIVE_NEW_TASK_TITLE))
        onView(withId(R.id.save_task_fab)).perform(click())

        //stub navigation behaviour
        doCallRealMethod().`when`(mockedNav).navigate(
            AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
        )

        //THEN - verify
        //repo contains changes
        val result = repository.getTasksBlocking(true) as Result.Success
        assertThat(result.data.last().title,`is`(ACTIVE_NEW_TASK_TITLE))
        assertThat(result.data.last().description,`is`(ACTIVE_TASK_DESC))
        assertThat(result.data.last().title, `is`(not(ACTIVE_TASK_TITLE)))
        assertThat(result.data.last().description, `is`(not(ACTIVE_NEW_TASK_DESC)))
        assertThat(result.data.size, `is`(1))

    }

    @Test
    fun editTask_changeDescriptionOnly_changesDescriptionDetailsOnly() {
        //GIVEN - an active task
        val activeTask = Task(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC,false, ACTIVE_TASK_ID)
        repository.saveTaskBlocking(activeTask)

        //launch the edit fragment
        val bundle = AddEditTaskFragmentArgs(ACTIVE_TASK_ID, ACTIVE_TASK_TITLE).toBundle()
        val scenario = launchFragmentInContainer<AddEditTaskFragment>(bundle,R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        //mock the nav
        val mockedNav = mock(NavController::class.java)
        scenario.onFragment{
            Navigation.setViewNavController(it.requireView(),mockedNav)
        }

        //WHEN - only the description is changed, save fab is clicked
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText(ACTIVE_NEW_TASK_DESC))
        onView(withId(R.id.save_task_fab)).perform(click())

        //stub navigation behaviour
        doCallRealMethod().`when`(mockedNav).navigate(
            AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
        )

        //THEN - verify
        //repo contains changes
        val result = repository.getTasksBlocking(true) as Result.Success
        assertThat(result.data.last().title,`is`(ACTIVE_TASK_TITLE))
        assertThat(result.data.last().description,`is`(ACTIVE_NEW_TASK_DESC))
        assertThat(result.data.last().description, `is`(not(ACTIVE_TASK_DESC)))
        assertThat(result.data.size, `is`(1))

    }


}