package com.example.gallery.models

import android.util.TypedValue
import androidx.fragment.app.FragmentActivity
import com.example.gallery.R

internal data class GalleryStyleAttrs(
    val galleryBackgroundColor: Int = 0,
    val galleryColorPrimary: Int = 0,
    val galleryColorPrimaryDark: Int = 0,
    val galleryColorAccent: Int = 0,
    val galleryTintColor: Int = 0,
    val galleryToolbarIconTintColor: Int = 0,
    val galleryPrimaryTextColor: Int = 0,
    val gallerySecondaryTextColor: Int = 0,
    val galleryHintTextColor: Int = 0,
    val galleryPlaceHolderColor: Int = 0,
    val galleryToolbarTextColor: Int = 0
)

internal fun FragmentActivity.getGalleryStyleAttrs(): GalleryStyleAttrs = this.theme.let { activityTheme ->

    var galleryStyleAttrs = GalleryStyleAttrs()
    val typeValue = TypedValue()

    activityTheme.resolveAttribute(R.attr.gallery_background_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryBackgroundColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_color_primary, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryColorPrimary = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_color_primary_dark, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryColorPrimaryDark = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_color_accent, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryColorAccent = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_icon_tint_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryTintColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_toolbar_icon_tint_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryToolbarIconTintColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_primary_text_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryPrimaryTextColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_secondary_text_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(gallerySecondaryTextColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_hint_text_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryHintTextColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_place_holder_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryPlaceHolderColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.gallery_toolbar_text_color, typeValue, true)
    galleryStyleAttrs = galleryStyleAttrs.copy(galleryToolbarTextColor = typeValue.data)


    galleryStyleAttrs
}