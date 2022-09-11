package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class AzzumTasksActivityTests {

    companion object {
        const val TIMEOUT_ONE_SEC = 1000L
        const val TIMEOUT_TWO_SEC = 2000L

        const val ACTIVE_TASK_TITLE = "Active Task"
        const val ACTIVE_TASK_DESC = "Active Task Description"
        const val ACTIVE_TASK_ID = "actId1"
        const val ACTIVE_NEW_TASK_TITLE = "New Active Task"
        const val ACTIVE_NEW_TASK_DESC = "New Active Task Description"

        const val COMPLETED_TASK_TITLE = "Completed Task"
        const val COMPLETED_TASK_DESC = "Completed Task Description"
        const val COMPLETED_TASK_ID = "compId1"
        const val COMPLETED_NEW_TASK_TITLE = "New Completed Task"
        const val COMPLETED_NEW_TASK_DESC = "New Completed Task Description"

        val addTaskFab = viewWithId(R.id.add_task_fab)
        val addTaskTitleEditText = viewWithId(R.id.add_task_title_edit_text)
        val addTaskDescEditText = viewWithId(R.id.add_task_description_edit_text)

        val saveTaskFab = viewWithId(R.id.save_task_fab)
        val editTaskFab = viewWithId(R.id.edit_task_fab)

        val deleteMenu = viewWithId(R.id.menu_delete)
        val filterMenu = viewWithId(R.id.menu_filter)
        val menuNavActive = viewWithText(R.string.nav_active)
        val menuNavCompleted = viewWithText(R.string.nav_completed)
        val menuNavAll = viewWithText(R.string.nav_all)
        val kebabMenuClearCompleted = viewWithText(R.string.menu_clear)

        val noTaskIcon = viewWithId(R.id.no_tasks_icon)

    }

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var repository: TasksRepository
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val uiDevice: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())


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

    @Before
    fun init() {
        repository = ServiceLocator.provideTasksRepository(getApplicationContext())
        runBlocking {
            repository.deleteAllTasks()
        }
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    @Test
    fun createATask_checkIsDisplayed() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //WHEN - task is created
        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        //THEN - verify the task is shown in the task list
        viewWithText(ACTIVE_TASK_TITLE).isDisplayed()

        activity.close()

    }

    @Test
    fun addTask_markCompleted() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //WHEN - task is created
        addATask(COMPLETED_TASK_TITLE, COMPLETED_TASK_DESC)

        //THEN - verify that the task is shown
        //task is not checked
        //task is marked completed, and is checked
        onView(withId(R.id.complete_checkbox))
            .check(matches(hasSibling(withText(COMPLETED_TASK_TITLE))))
            .check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        activity.close()

    }

    @Test
    fun addTask_markCompleted_thenActivate() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //WHEN - task is created
        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        //THEN - verify that the task is shown
        //task is not checked
        //task is marked completed, and is checked
        //task is re-activated, and not checked
        onView(withId(R.id.complete_checkbox))
            .check(matches(hasSibling(withText(ACTIVE_TASK_TITLE))))
            .check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))
            .perform(click())
            .check(matches(isNotChecked()))

        activity.close()
    }

    @Test
    fun addTask_gotoTaskDetail_isDisplayed() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //WHEN - a task is created
        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        viewWithText(ACTIVE_TASK_TITLE).isDisplayed().click()

        //THEN - verify the task is displayed in Task Detail screen
        uiDevice.waitForWindowUpdate(
            "com.example.android.architecture.blueprints.todoapp.taskdetail",
            1000
        )
        viewWithText(ACTIVE_TASK_TITLE).isDisplayed()

        activity.close()
    }

    @Test
    fun addTask_showDetail_markComplete() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //WHEN - task is added
        addATask(COMPLETED_TASK_TITLE, COMPLETED_TASK_DESC)

        //task is shown, clicked to show details, marked as completed
        viewWithText(COMPLETED_TASK_TITLE).isDisplayed().click()

        uiDevice.waitForWindowUpdate(
            "com.example.android.architecture.blueprints.todoapp.taskdetail",
            1000
        )
        onView(
            allOf(
                withId(R.id.task_detail_complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        //navigate back to task list
        onView(isAssignableFrom(AppCompatImageButton::class.java)).check(
            matches(
                withParent(
                    isAssignableFrom(Toolbar::class.java)
                )
            )
        ).perform(click())

        //THEN - verify task is marked completed
        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isChecked()))

        activity.close()

    }

    @Test
    fun addTask_markComplete_showDetail_activate() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //WHEN - task is added
        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        onView(withId(R.id.complete_checkbox))
            .check(matches(hasSibling(withText(ACTIVE_TASK_TITLE))))
            .check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        viewWithText(ACTIVE_TASK_TITLE).click()

        uiDevice.waitForWindowUpdate(
            "com.example.android.architecture.blueprints.todoapp.taskdetail",
            TIMEOUT_ONE_SEC
        )

        onView(
            allOf(
                withId(R.id.task_detail_complete_checkbox),
                hasSibling(withText(ACTIVE_TASK_TITLE))
            )
        ).check(matches(isChecked()))
            .perform(click())
            .check(matches(isNotChecked()))

        navigateBackToTaskFragment()

        //THEN - verify task is now active
        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(ACTIVE_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))

        activity.close()
    }

    @Test
    fun addTask_deleteActiveTask() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        noTaskIcon.isDisplayed()
        //WHEN - task is added
        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        viewWithText(ACTIVE_TASK_TITLE).isDisplayed().click()

        uiDevice.waitForWindowUpdate(
            "com.example.android.architecture.blueprints.todoapp.taskdetail",
            TIMEOUT_ONE_SEC
        )

        viewWithText(ACTIVE_TASK_TITLE).isDisplayed()

        deleteMenu.click()

        noTaskIcon.isDisplayed()

        activity.close()
    }

    @Test
    fun addTask_deleteCompletedTask() {
        //GIVEN - task activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        noTaskIcon.isDisplayed()

        //WHEN - task is added
        addATask(COMPLETED_TASK_TITLE, COMPLETED_TASK_DESC)

        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        viewWithText(COMPLETED_TASK_TITLE).click()

        uiDevice.waitForWindowUpdate(
            "com.example.android.architecture.blueprints.todoapp.taskdetail",
            1000
        )

        onView(
            allOf(
                withId(R.id.task_detail_complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isChecked()))

        deleteMenu.click()

        noTaskIcon.isDisplayed()

        activity.close()

    }

    @Test
    fun addTask_editTask() {
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        noTaskIcon.isDisplayed()

        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        viewWithText(ACTIVE_TASK_TITLE).click()

        uiDevice.waitForWindowUpdate(
            "com.example.android.architecture.blueprints.todoapp.taskdetail",
            TIMEOUT_ONE_SEC
        )

        viewWithText(ACTIVE_TASK_TITLE).isDisplayed()
        editTaskFab.click()

        //change title and description
        addTaskTitleEditText.type(ACTIVE_NEW_TASK_TITLE)
        addTaskDescEditText.type(ACTIVE_NEW_TASK_DESC)

        saveTaskFab.click()

        //THEN - verify task title/description are updated
        viewWithText(ACTIVE_NEW_TASK_TITLE).isDisplayed().click()
        viewWithText(ACTIVE_NEW_TASK_DESC).isDisplayed()

        activity.close()
    }

    @Test
    fun addTask_showActiveTask() {
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        noTaskIcon.isDisplayed()

        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        viewWithText(R.string.label_all).isDisplayed()

        filterMenu.click()
        viewWithText(R.string.nav_active).click()

        viewWithText(R.string.label_active).isDisplayed()
        viewWithText(ACTIVE_TASK_TITLE).isDisplayed()

        filterMenu.click()
        viewWithText(R.string.nav_completed).click()

        onView(
            allOf(
                withId(R.id.title_text),
                withText(ACTIVE_TASK_TITLE)
            )
        ).check(matches(not(isDisplayed())))

        viewWithText(R.string.no_tasks_completed).isDisplayed()

        activity.close()
    }

    @Test
    fun addTask_markCompleted_showCompletedFilter() {
        //GIVEN - task fragment is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        noTaskIcon.isDisplayed()
        //task is added
        addATask(COMPLETED_TASK_TITLE, COMPLETED_TASK_DESC)

        //ALL TASKS filter is set by default
        viewWithText(R.string.label_all).isDisplayed()

        //WHEN - task is marked completed,
        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        //THEN - verify completed task does not appear in active tasks
        filterMenu.click()
        menuNavActive.click()

        viewWithText(R.string.no_tasks_active).isDisplayed()

        onView(
            allOf(
                withId(R.id.title_text),
                withText(COMPLETED_TASK_TITLE)
            )
        ).check(matches(not(isDisplayed())))

        //and verify completed task appears in completed tasks only
        filterMenu.click()
        menuNavCompleted.click()

        viewWithText(R.string.label_completed).isDisplayed()
        viewWithText(COMPLETED_TASK_TITLE).isDisplayed()

        activity.close()

    }

    @Test
    fun addActiveAndCompletedTasks_displaysInActiveAndCompletedTasks() {
        //GIVEN - Tasks Activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        //no tasks exist
        noTaskIcon.isDisplayed()

        //add an active task and a completed task
        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)
        addATask(COMPLETED_TASK_TITLE, COMPLETED_TASK_DESC)

        //active task is not checked
        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(ACTIVE_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))

        //mark completed task as complete and is checked
        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        //click filter menu
        filterMenu.click()

        //filter active tasks
        menuNavActive.click()

        //THEN - verify active task appears in active tasks list only
        viewWithText(R.string.label_active).isDisplayed()

        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(ACTIVE_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))

        //and completed task does not appear in active task list
        onView(
            allOf(
                withId(R.id.title_text),
                withText(COMPLETED_TASK_TITLE)
            )
        ).check(doesNotExist())

        //verify completed task appears in completed tasks list
        filterMenu.click()
        menuNavCompleted.click()

        viewWithText(R.string.label_completed).isDisplayed()

        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isChecked()))

        //and active task does not appear in completed tasks list
        onView(
            allOf(
                withId(R.id.title_text),
                withText(ACTIVE_TASK_TITLE)
            )
        ).check(doesNotExist())

        activity.close()
    }

    @Test
    fun addTask_markCompleted_clearCompleted() {
        //GIVEN: tasks activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        noTaskIcon.isDisplayed()

        //a completed task is added
        addATask(COMPLETED_TASK_TITLE, COMPLETED_TASK_DESC)

        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(COMPLETED_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        //WHEN - completed task is cleared
        uiDevice.pressMenu()
        kebabMenuClearCompleted.click()
//        onView(withText(R.string.menu_clear)).perform(click())

        uiDevice.findObject(
            UiSelector()
                .textContains("Completed tasks cleared")
        ).waitUntilGone(1000)


        //THEN - verify task icon is shown
        viewWithText(R.string.no_tasks_all).isDisplayed()
//        onView(withText(R.string.no_tasks_all))
//            .check(matches(isDisplayed()))

        //and title text view with task title does not exist
        onView(
            allOf(
                withId(R.id.title_text),
                withText(COMPLETED_TASK_TITLE)
            )

        ).check(matches(not(isDisplayed())))

        activity.close()
    }

    @Test
    fun activeTask_clear_doesNotClearTask() {
        //GIVEN: tasks activity is launched
        val activity = launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activity)

        noTaskIcon.isDisplayed()

        //an active task added
        addATask(ACTIVE_TASK_TITLE, ACTIVE_TASK_DESC)

        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(ACTIVE_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))

        //WHEN - completed task is cleared
        uiDevice.pressMenu()

        kebabMenuClearCompleted.click()
//        viewWithText(R.string.menu_clear).click()

        uiDevice.findObject(
            UiSelector()
                .textContains("Completed tasks cleared")
        ).waitUntilGone(TIMEOUT_ONE_SEC)

        //THEN verify the active task is not cleared
        onView(
            allOf(
                withId(R.id.complete_checkbox),
                hasSibling(withText(ACTIVE_TASK_TITLE))
            )
        ).check(matches(isNotChecked()))

        activity.close()

    }

    private fun addATask(taskTypeTitle: String, taskTypeDescription: String) {

        addTaskFab.click()

        addTaskTitleEditText.type(taskTypeTitle)

        addTaskDescEditText.type(taskTypeDescription)

        saveTaskFab.click()

        //explain why have i used uiautomator due to lack of time =]
        uiDevice.wait(Until.gone(By.text(context.resources.getString(R.string.successfully_added_task_message))),TIMEOUT_TWO_SEC)

    }
}