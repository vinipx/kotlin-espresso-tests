package com.example.catsonactivity.apps.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.catsonactivity.R
import com.example.catsonactivity.apps.fragments.CatDetailsFragment
import com.example.catsonactivity.apps.fragments.FragmentRouter
import com.example.catsonactivity.apps.fragments.di.FragmentRouterModule
import com.example.catsonactivity.di.RepositoriesModule
import com.example.catsonactivity.model.Cat
import com.example.catsonactivity.testutils.BaseTest
import com.example.catsonactivity.testutils.FakeImageLoader
import com.example.catsonactivity.testutils.espresso.withDrawable
import com.example.catsonactivity.testutils.launchHiltFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(RepositoriesModule::class, FragmentRouterModule::class)
@MediumTest
class CatDetailsFragmentTest: BaseTest() {

    @Inject
    lateinit var fragmentRouter: FragmentRouter

    private val cat = Cat(
        id = 1,
        name = "Lucky",
        photoUrl = "cat.jpg",
        description = "Meow-meow",
        isFavorite = true
    )

    private val catsFlow = MutableStateFlow(cat)

    private lateinit var scenario: AutoCloseable

    @Before
    override fun setup() {
        super.setup()
        every { catsRepository.getCatById(any()) } returns catsFlow
        scenario = launchHiltFragment { CatDetailsFragment.newInstance(cat.id) }
    }

    @After
    fun tearDown(){
        scenario.close()
    }

    @Test
    fun testCatDetailsFragment_whenOpened_thenCatIsDisplayed() {
        onView(withId(R.id.catNameTextView))
            .check(matches(ViewMatchers.withText("Lucky")))
        onView(withId(R.id.catDescriptionTextView))
            .check(matches(ViewMatchers.withText("Meow-meow")))
        onView(withId(R.id.favoriteImageView))
            .check(matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
        onView(withId(R.id.catImageView))
            .check(matches(withDrawable(FakeImageLoader.createDrawable(cat.photoUrl))))
    }


    @Test
    fun testCatDetailsFragment_whenFavoriteFlagSelected_thenFavoriteFlagIsDisplayed(){
        every { catsRepository.toggleIsFavorite(any()) } answers {
            val cat = firstArg<Cat>()
            val newCat = cat.copy(isFavorite = !cat.isFavorite)
            catsFlow.value = newCat
        }

        onView(withId(R.id.favoriteImageView))
            .perform(ViewActions.click())
            .check(matches(withDrawable(R.drawable.ic_favorite_not, R.color.action)))

        onView(withId(R.id.favoriteImageView))
            .perform(ViewActions.click())
            .check(matches(withDrawable(R.drawable.ic_favorite, R.color.highlighted_action)))
    }

    @Test
    fun ctestCatDetailsFragment_whenBackButtonSelected_thenFinishActivity(){
        onView(withId(R.id.goBackButton)).perform(ViewActions.click())
        verify(exactly = 1) { fragmentRouter.goBack() }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class FakeFragmentRouterModule{
        @Provides
        @Singleton
        fun bindRouter(): FragmentRouter{
            return mockk(relaxed = true)
        }
    }
}