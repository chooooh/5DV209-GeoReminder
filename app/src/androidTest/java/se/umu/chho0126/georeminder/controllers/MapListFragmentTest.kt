package se.umu.chho0126.georeminder.controllers

import junit.framework.TestCase

class MapListFragmentTest : TestCase() {

    private val mapListFragment: MapListFragment = MapListFragment()
    fun testRound() {
        with(mapListFragment) {
            var expectedDecimal = 25.55
            var actualDecimal = 25.55444.round(2)
            assertEquals(expectedDecimal, actualDecimal)

            expectedDecimal = 25.56
            actualDecimal = 25.56.round(2)
            assertEquals(expectedDecimal, actualDecimal)

            expectedDecimal = 25.56
            actualDecimal = 25.56.round(2)
            assertEquals(expectedDecimal, actualDecimal)


            expectedDecimal = 25.6
            actualDecimal = 25.56.round(1)
            assertEquals(expectedDecimal, actualDecimal)
        }

    }
}
