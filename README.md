# Proton Android Test Engineer Espresso Tests Exercise

This exercise is completed as part of the Android Test Engieer role at Proton. 
The task's focus is however limited to just designing test scripts for the add, edit and delete features of the To-Do application.

**Please note:** all the tests reside inside the **androidTestMock** folder under their respective packages.

#### Happy Reviewing!

![alt text](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/test_files.png "Test Files")

### To be noted:
- Due to time constraints, no unit tests have been written
- Only Integration and E2E tests have been designed
- Navigation of the entire app is not covered due to time constraints
- AddEditTaskFragmentTests - are separated into two separate files: AzzumAddTaskTests.kt and AzzumEditTasksTests.kt
- UiAutomator: some tests made use of uiautomator for the snackbar testing. I encountered an issue with testing snackbar but I did not want to go down in to that rabbit hole for now, so as an alternative, I have made use of UiAutomator. You will even find some comment out code in AzzumTaskDetailFragmentTests. 
- Mockito: I have used mockito library to mock NavigationController of the app. 
- Due to time constraints could not refactor the code. Although, AzzumTasksActivityTests has been refactored to some extent. 


### What the exercise contains: 
Firstly, ensure you are on **azzum-tests** branch in order to view the tests. 

#### Test Suite: (runs all the tests - Integration/E2E)
- [AndroidTestSuite](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/app/src/androidTestMock/java/com/example/android/architecture/blueprints/todoapp/tasks/AndroidTestSuite.kt) 

#### Integration Tests: 
@MediumTests

- [AzzumTasksListFragmentTests](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/app/src/androidTestMock/java/com/example/android/architecture/blueprints/todoapp/tasks/tasks/AzzumTaskListFragmentTests.kt) : This test file contains 6 tests that test the integration between TasksFragment and TasksViewModel. Also tests navigation to AddEditTaskFragment and TaskDetailFragment. This is achieved using Mockito library. 

- [AzzumAddTaskTests](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/app/src/androidTestMock/java/com/example/android/architecture/blueprints/todoapp/tasks/addedittask/AzzumAddTaskTests.kt) : contains 4 tests. This file's focus is to just test 'adding' of the task for AddEditTaskFragment. I have deliberately segregated the add and edit tests into two separate files (see the other file AzzumEditTasksTests) just for brevity sake. 

- [AzzumEditTasksTests](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/app/src/androidTestMock/java/com/example/android/architecture/blueprints/todoapp/tasks/addedittask/AzzumEditTasksTests.kt) : contains 4 tests. Since, editing task requires saving and navigating to TasksFragment, I stubbed out the navigation behaviour using Mockito's mock(). 

- [AzzumTaskDetailFragmentTests](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/app/src/androidTestMock/java/com/example/android/architecture/blueprints/todoapp/tasks/taskdetail/AzzumTaskDetailFragmentTests.kt) : There are 6 tests. The last two tests essentially are the same; Test 5 uses mocked navigation, and where as Test 6 uses the android's TestNavHostController to assert we navigate to correct destination fragment by matching the fragment id. The need arose due to the error I was facing on lines 191-196: I was passing in an incorrect argument. 

#### E2E tests:
@LargeTests

- [AzzumTasksActivityTests.kt](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/app/src/androidTestMock/java/com/example/android/architecture/blueprints/todoapp/tasks/AzzumTasksActivityTests.kt) : 
These are the main activity tests that test the entire flow of the add, edit, delete features. This file consists of 14 tests.
**Note:** I had started to refactor the code. As such, you will see I have used Kotlin's extension functions to improve scripts' readability.
However, refactoring has not been applied to other tests due to the time constraints. 


### The End Result - And....!!! Voila - Happy Reviewing!
![alt text](https://github.com/azzumw/architecture-samples-azm/blob/azzum-tests/alll_tests_passed.png "Tests Passed")


### License


```
Copyright 2019 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership. The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
