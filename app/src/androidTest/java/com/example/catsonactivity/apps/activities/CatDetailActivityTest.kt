package com.example.catsonactivity.apps.activities

import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.FakeImageLoader
import com.example.catsonactivity.testutils.espresso.withDrawable
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class)
@MediumTest
class CatDetailActivityTest : BaseTest(){

    private lateinit var scenario: ActivityScenario<CatDetailsActivity>

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "Meow-meow",
        isFavorite = true
    )

    private val catFlow = MutableStateFlow(cat)

    @Before
    override fun setup() {
        super.setup()
        every{catsRepository.getCatById(any())} returns catFlow
        Intents.init()
        scenario = ActivityScenario.launch(
            CatDetailsActivity::class.java,
            bundleOf(
                CatDetailsActivity.EXTRA_CAT_ID to 1L
            )
        )
    }

    @After
    fun tearDown(){
        Intents.release()
        scenario.close()
    }

    @Test
    fun testCatDetailActivity_whenOpened_thenCatDetailsAreLoaded() {
        //assert
        onView(withId(R.id.catNameTextView))
            .check(matches(withText("Lucky")))
        onView(withId(R.id.catDescriptionTextView))
            .check(matches(withText("Meow-meow")))
        onView(withId(R.id.favoriteImageView))
            .check(matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
        onView(withId( R.id.catImageView))
            .check(matches(withDrawable(FakeImageLoader.createDrawable(cat.photoUrl))))
    }


    @Test
    fun testCatDetailActivity_whenFavoriteIsSelected_thenCatIsFavorite(){
        //arranged favorite cat
        every { catsRepository.toggleIsFavorite(any()) } answers {
            val cat = firstArg<Cat>()
            val newCat = cat.copy(isFavorite = !cat.isFavorite)
            catFlow.value = newCat
        }

        //act 1 - turn off
        onView(withId(R.id.favoriteImageView)).perform(click())
        onView(withId(R.id.favoriteImageView))
            .check(matches(withDrawable(R.drawable.ic_favorite_not, R.color.action)))

        //act 2 - turn on
        onView(withId(R.id.favoriteImageView)).perform(click())
        onView(withId(R.id.favoriteImageView))
            .check(matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
    }

    @Test
    fun testCatDetailActivity_whenClickBackButton_thenCatDetailActivityIsDestroyed(){
        onView(withId(R.id.goBackButton)).perform(click())
        Assert.assertTrue(scenario.state == Lifecycle.State.DESTROYED)
    }

}