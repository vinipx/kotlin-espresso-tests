package com.example.catsonactivity.apps.fragment

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.fragments.CatsFragmentContainerActivity
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.espresso.actionOnItemAtPosition
import com.example.catsonactivity.testutils.espresso.atPosition
import com.example.catsonactivity.testutils.espresso.clickOnView
import com.example.catsonactivity.testutils.espresso.scrollToPosition
import com.example.catsonactivity.testutils.espresso.withDrawable
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class)
@LargeTest
class CatsFragmentsIntegrationTest : BaseTest() {

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat1.jpg",
        description = "The first cat",
        isFavorite = false
    )

    private val catsFlow = MutableStateFlow(listOf(cat))

    private lateinit var scenario: ActivityScenario<CatsFragmentContainerActivity>

    @Before
    override fun setup() {
        super.setup()
        every { catsRepository.getCats() } returns catsFlow
        every { catsRepository.getCatById(any()) } returns catsFlow.map { it.first() }
        every { catsRepository.toggleIsFavorite(any()) } answers {
            catsFlow.value = catsFlow.value.map { it.copy(isFavorite = !it.isFavorite) }
        }
        scenario = ActivityScenario.launch(CatsFragmentContainerActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testCatsFragmentsIntegration_whenFavoriteFlagIsSelected_thenFavoriteFlagSwitchFromInactiveToActive() {
        clickOnToggleFavoriteInListenScreen()
        clickOnCat()
        assertIsFavoriteFlagActiveInDetailScreen()
        clickOnGoBack()
        assertIsFavouriteFlagActiveInListScreen()
        clickOnCat()
        clickOnToggleFavoriteInDetails()
        clickOnGoBack()
        assertIsFavoriteFlagInactiveInListScreen()
    }

    @Test
    fun testCatsFragmentsIntegration_whenCatListTitleIsSelected_thenListTitleInActionBarIsDisplayed() {
        assertCatsListTitle()
        clickOnCat()
        assertCatDetailsTitle()
        clickOnGoBack()
        assertCatsListTitle()
    }

    @Test
    fun testCatsFragmentsIntegration_whenNavigateUpIsSelected_thenCatTitleIsDisplayed() {
        clickOnCat()
        clickOnNavigateUp()
        assertCatsListTitle()
    }

    @Test
    fun testCatsFragmentsIntegration_whenBackButtonIsSelected_thenCatTitleIsDisplayed() {
        clickOnCat()
        Espresso.pressBack()
        assertCatsListTitle()
    }

    private fun clickOnToggleFavoriteInListenScreen() {
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, clickOnView(R.id.favoriteImageView)))
    }

    private fun clickOnCat() {
        onView(withId(R.id.catsRecyclerView))
            .perform(actionOnItemAtPosition(1, ViewActions.click()))
    }

    private fun assertIsFavoriteFlagActiveInDetailScreen() {
        onView(
            allOf(
                withId(R.id.favoriteImageView), Matchers.not(
                    isDescendantOfA(
                        withId(R.id.catsRecyclerView)
                    )
                )
            )
        )
            .check(matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
    }

    private fun clickOnGoBack() {
        onView(withId(R.id.goBackButton)).perform(ViewActions.click())
    }

    private fun assertIsFavouriteFlagActiveInListScreen() {
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(
                matches(
                    atPosition(
                        1, ViewMatchers.hasDescendant(
                            allOf(
                                withId(R.id.favoriteImageView),
                                withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)
                            )
                        )
                    )
                )
            )
    }

    private fun clickOnToggleFavoriteInDetails() {
        onView(
            allOf(
                withId(R.id.favoriteImageView),
                Matchers.not(isDescendantOfA(withId(R.id.catsRecyclerView)))
            )
        )
            .perform(ViewActions.click())
    }

    private fun assertIsFavoriteFlagInactiveInListScreen() {
        onView(withId(R.id.catsRecyclerView))
            .perform(scrollToPosition(1))
            .check(
                matches(
                    atPosition(
                        1, ViewMatchers.hasDescendant(
                            allOf(
                                withId(R.id.favoriteImageView),
                                withDrawable(R.drawable.ic_favorite_not, R.color.action)
                            )
                        )
                    )
                )
            )
    }

    private fun assertCatsListTitle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        scenario.onActivity { activity ->
            assertEquals(
                context.getString(R.string.fragment_cats_title),
                activity.supportActionBar?.title
            )
        }
    }

    private fun assertCatDetailsTitle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        scenario.onActivity { activity ->
            assertEquals(
                context.getString(R.string.fragment_cat_details),
                activity.supportActionBar?.title
            )
        }
    }

    private fun clickOnNavigateUp() {
        onView(
            withContentDescription(
                androidx.appcompat.R.string.abc_action_bar_up_description
            )
        ).perform(ViewActions.click())
    }

}

