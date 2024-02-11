package com.example.catsonactivity.apps.navigation

import androidx.navigation.NavController
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.navcomponent.NavCatDetailsFragment
import com.example.catsonactivity.apps.navcomponent.NavCatDetailsFragmentArgs
import com.example.catsonactivity.apps.navcomponent.NavCatsListFragment
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.FakeImageLoader
import com.example.catsonactivity.testutils.espresso.withDrawable
import com.example.catsonactivity.testutils.launchNavHiltFragment
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class)
@MediumTest
class NavCatDetailsFragmentTest: BaseTest() {

    @RelaxedMockK
    lateinit var navController: NavController

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "Meow-meow",
        isFavorite = true
    )

    private val catFlow = MutableStateFlow(cat)

    private lateinit var scenario: AutoCloseable

    @Before
    override fun setup() {
        super.setup()
        every { catsRepository.getCatById(any()) } returns catFlow
        val args = NavCatDetailsFragmentArgs(catId = 1L)
        scenario = launchNavHiltFragment<NavCatDetailsFragment>(navController, args.toBundle())
    }

    @After
    fun tearDown(){
        scenario.close()
    }

    @Test
    fun testNavCatDetailsFragment_whenOpened_thenCatDetailsIsDisplayed() {
        onView(ViewMatchers.withId(R.id.catNameTextView))
            .check(matches(ViewMatchers.withText("Lucky")))
        onView(ViewMatchers.withId(R.id.catDescriptionTextView))
            .check(matches(ViewMatchers.withText("Meow-meow")))
        onView(ViewMatchers.withId(R.id.favoriteImageView))
            .check(matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
        onView(ViewMatchers.withId(R.id.catImageView))
            .check(matches(withDrawable(FakeImageLoader.createDrawable(cat.photoUrl))))
    }

    @Test
    fun testNavCatDetailsFragment_whenFavoriteIsSelectedThenFavoriteFlagIsDisplayed(){
        every { catsRepository.toggleIsFavorite(any()) } answers {
            val cat = firstArg<Cat>()
            val newCat = cat.copy(isFavorite = !cat.isFavorite)
            catFlow.value = newCat
        }

        onView(ViewMatchers.withId(R.id.favoriteImageView))
            .perform(click())
            .check(matches(withDrawable(R.drawable.ic_favorite_not, R.color.action)))

        onView(ViewMatchers.withId(R.id.favoriteImageView))
            .perform(click())
            .check(matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
    }

    @Test
    fun testNavCatDetailsFragment_whenBackButtonIsSelected_thenFinishActivity(){
        onView(ViewMatchers.withId(R.id.goBackButton)).perform(click())
        verify(exactly = 1) { navController.popBackStack() }
    }

}