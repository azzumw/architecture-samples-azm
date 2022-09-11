package com.example.android.architecture.blueprints.todoapp.tasks

import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*

fun viewWithId(id:Int):ViewInteraction = Espresso.onView(withId(id))

fun viewWithText(text:String):ViewInteraction = Espresso.onView(withText(text))

fun viewWithText(id:Int):ViewInteraction = Espresso.onView(withText(id))

fun ViewInteraction.click():ViewInteraction = perform(ViewActions.click())

fun ViewInteraction.type(text: String) : ViewInteraction = perform(ViewActions.replaceText(text),ViewActions.closeSoftKeyboard())

fun ViewInteraction.isDisplayed() : ViewInteraction = check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

fun navigateBackToTaskFragment():ViewInteraction = onView(isAssignableFrom(AppCompatImageButton::class.java)).check(matches(
        withParent(isAssignableFrom(Toolbar::class.java)))).perform(ViewActions.click())