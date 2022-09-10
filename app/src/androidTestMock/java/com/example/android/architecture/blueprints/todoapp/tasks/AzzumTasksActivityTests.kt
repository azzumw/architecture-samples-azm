package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.snackbar.Snackbar
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AzzumTasksActivityTests {

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())


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


    //create a single task and check it is displayed
    @Test
    fun createATask_checkIsDisplayed() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //WHEN - task is created
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(
            typeText("Task 1"), closeSoftKeyboard()
        )
        onView(withId(R.id.add_task_description_edit_text)).perform(
            typeText("Description for Task 1"), closeSoftKeyboard()
        )
        onView(withId(R.id.save_task_fab)).perform(click())


        uiDevice.waitForWindowUpdate(" com.example.android.architecture.blueprints.todoapp.tasks",1000)

//        onView(withId(com.google.android.material.R.id.snackbar_text))
//            .check(matches(withText(R.string.successfully_added_task_message)))

        //THEN - verify the task is shown in the task list
        onView(withText("Task 1")).check(matches(isDisplayed()))
        activity.close()

    }

    //edit a task that is already created

    //delete a task that is already created

}