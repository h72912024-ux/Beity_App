package com.example

import com.example.data.model.EgyptCatalog
import com.example.data.model.ListingType
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyType
import com.example.data.model.formatPriceAr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatPriceAr_formatsMillionsAndThousands() {
        assertEquals("5 مليون ج.م", formatPriceAr(5_000_000.0))
        assertEquals("2.5 مليون ج.م", formatPriceAr(2_500_000.0))
        assertEquals("45,000 ج.م", formatPriceAr(45_000.0))
    }

    @Test
    fun propertyFilter_activeStateAndSummaryWork() {
        val empty = PropertyFilter()
        assertFalse(empty.hasActiveFilters())

        val filtered = PropertyFilter(
            governorate = "القاهرة",
            propertyType = PropertyType.VILLA,
            listingType = ListingType.SALE,
            maxPrice = 10_000_000.0
        )
        assertTrue(filtered.hasActiveFilters())
        val summary = filtered.summaryAr()
        assertTrue(summary.contains("للبيع"))
        assertTrue(summary.contains("فيلات"))
        assertTrue(summary.contains("القاهرة"))
    }

    @Test
    fun egyptCatalog_hasSevenCategoriesAndEightGovernorates() {
        assertEquals(7, EgyptCatalog.defaultCategories.size)
        assertEquals(8, EgyptCatalog.governorates.size)
        assertTrue(EgyptCatalog.sampleDemoPropertiesForAdmin("admin1", "إدارة بيتي").isNotEmpty())
    }
}
