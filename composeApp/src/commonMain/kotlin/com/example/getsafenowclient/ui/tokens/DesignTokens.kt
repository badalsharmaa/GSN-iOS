package com.example.getsafenowclient.ui.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens for consistent spacing, sizing, and styling across the app.
 * 
 * This replaces hardcoded values with semantic tokens that:
 * - Ensure visual consistency
 * - Make the app more maintainable
 * - Enable easier theming and customization
 * - Support responsive design
 * 
 * Usage:
 * ```kotlin
 * .padding(DesignTokens.Spacing.md)
 * .size(DesignTokens.IconSize.medium)
 * ```
 */
object DesignTokens {
    
    /**
     * Spacing tokens for consistent padding and margins.
     * Based on 4dp grid system.
     */
    object Spacing {
        /** 4dp - Minimal spacing for tight layouts */
        val xs: Dp = 4.dp
        
        /** 8dp - Small spacing for compact elements */
        val sm: Dp = 8.dp
        
        /** 12dp - Medium-small spacing for cards and bubbles */
        val md: Dp = 12.dp
        
        /** 16dp - Standard spacing for most layouts */
        val lg: Dp = 16.dp
        
        /** 24dp - Large spacing for sections and dialogs */
        val xl: Dp = 24.dp
        
        /** 32dp - Extra large spacing for major sections */
        val xxl: Dp = 32.dp
        
        /** 48dp - Maximum spacing for hero sections */
        val xxxl: Dp = 48.dp
    }
    
    /**
     * Icon size tokens for consistent icon sizing.
     * Follows Material Design guidelines.
     */
    object IconSize {
        /** 12dp - Extra small icons for dense UIs */
        val xsmall: Dp = 12.dp
        
        /** 14dp - Small icons for menu items */
        val small: Dp = 14.dp
        
        /** 16dp - Small-medium icons for inline text */
        val smallMedium: Dp = 16.dp
        
        /** 18dp - Medium-small icons */
        val mediumSmall: Dp = 18.dp
        
        /** 20dp - Medium icons for compact UIs */
        val medium: Dp = 20.dp
        
        /** 22dp - Medium-large icons */
        val mediumLarge: Dp = 22.dp
        
        /** 24dp - Standard icon size (Material Design default) */
        val standard: Dp = 24.dp
        
        /** 25dp - Large-ish icons */
        val largish: Dp = 25.dp
        
        /** 32dp - Large icons for emphasis */
        val large: Dp = 32.dp
        
        /** 48dp - Extra large icons for hero elements */
        val xlarge: Dp = 48.dp
    }
    
    /**
     * Avatar size tokens for consistent avatar sizing.
     */
    object AvatarSize {
        /** 32dp - Small avatar for compact lists */
        val small: Dp = 32.dp
        
        /** 40dp - Medium avatar for standard lists */
        val medium: Dp = 40.dp
        
        /** 56dp - Large avatar for conversation lists */
        val large: Dp = 56.dp
        
        /** 80dp - Extra large avatar for profiles */
        val xlarge: Dp = 80.dp
        
        /** 100dp - Maximum avatar for profile screens */
        val xxlarge: Dp = 100.dp
    }
    
    /**
     * Touch target sizes following Material Design accessibility guidelines.
     * All interactive elements should meet minimum touch target size.
     */
    object TouchTarget {
        /** 48dp - Minimum touch target size (Material Design guideline) */
        val minimum: Dp = 48.dp
        
        /** 56dp - Comfortable touch target for primary actions */
        val comfortable: Dp = 56.dp
    }
    
    /**
     * Button and interactive element size tokens.
     */
    object ButtonHeight {
        /** 32dp - Extra small button */
        val xsmall: Dp = 32.dp
        
        /** 35dp - Small button for compact UIs */
        val small: Dp = 35.dp
        
        /** 36dp - Small-medium button */
        val smallMedium: Dp = 36.dp
        
        /** 40dp - Medium button */
        val medium: Dp = 40.dp
        
        /** 48dp - Standard button height */
        val standard: Dp = 48.dp
        
        /** 56dp - Large button for primary actions */
        val large: Dp = 56.dp
    }
    
    /**
     * Common component sizes for various UI elements.
     */
    object ComponentSize {
        /** 35dp - Small interactive elements */
        val small: Dp = 35.dp
        
        /** 40dp - Medium interactive elements */
        val medium: Dp = 40.dp
        
        /** 48dp - Standard interactive elements */
        val standard: Dp = 48.dp
        
        /** 55dp - Large text fields */
        val textFieldHeight: Dp = 55.dp
        
        /** 100dp - PIP video width */
        val pipWidth: Dp = 100.dp
        
        /** 160dp - PIP video height */
        val pipHeight: Dp = 160.dp
        
        /** 240dp - Media bubble width */
        val mediaBubbleWidth: Dp = 240.dp
    }
    
    /**
     * Corner radius tokens for consistent rounded corners.
     */
    object CornerRadius {
        /** 4dp - Subtle rounding */
        val xs: Dp = 4.dp
        
        /** 8dp - Small rounding for chips */
        val sm: Dp = 8.dp
        
        /** 12dp - Standard rounding for cards and bubbles */
        val md: Dp = 12.dp
        
        /** 16dp - Large rounding for dialogs */
        val lg: Dp = 16.dp
        
        /** 24dp - Extra large rounding for sheets */
        val xl: Dp = 24.dp
    }
    
    /**
     * Content width constraints for responsive design.
     * Prevents content from becoming too wide on tablets.
     */
    object ContentWidth {
        /** Maximum width for message bubbles (400dp) */
        val messageBubbleMax: Dp = 400.dp
        
        /** Maximum fraction of screen width for bubbles (80%) */
        const val messageBubbleFraction: Float = 0.8f
        
        /** Maximum width for dialogs (560dp) */
        val dialogMax: Dp = 560.dp
        
        /** Maximum width for forms (480dp) */
        val formMax: Dp = 480.dp
        
        /** Maximum width for content on large screens (840dp) */
        val contentMax: Dp = 840.dp
    }
    
    /**
     * Elevation tokens for consistent shadows and depth.
     * Based on Material Design elevation system.
     */
    object Elevation {
        /** 0dp - No elevation (flat) */
        val none: Dp = 0.dp
        
        /** 1dp - Subtle elevation for cards */
        val xs: Dp = 1.dp
        
        /** 2dp - Small elevation for raised elements */
        val sm: Dp = 2.dp
        
        /** 4dp - Medium elevation for floating elements */
        val md: Dp = 4.dp
        
        /** 8dp - Large elevation for dialogs */
        val lg: Dp = 8.dp
        
        /** 16dp - Maximum elevation for modals */
        val xl: Dp = 16.dp
    }
    
    /**
     * Animation duration tokens for consistent timing.
     * Based on Material Design motion guidelines.
     */
    object AnimationDuration {
        /** 100ms - Quick transitions */
        const val quick: Int = 100
        
        /** 200ms - Standard transitions */
        const val standard: Int = 200
        
        /** 300ms - Emphasized transitions */
        const val emphasized: Int = 300
        
        /** 500ms - Long transitions */
        const val long: Int = 500
    }
    
    /**
     * Z-index tokens for consistent layering.
     * Higher values appear on top.
     */
    object ZIndex {
        /** 0 - Base layer */
        const val base: Float = 0f
        
        /** 1 - Raised content */
        const val raised: Float = 1f
        
        /** 10 - Floating action buttons */
        const val fab: Float = 10f
        
        /** 100 - App bars and navigation */
        const val appBar: Float = 100f
        
        /** 1000 - Dialogs and modals */
        const val modal: Float = 1000f
        
        /** 10000 - Snackbars and toasts */
        const val snackbar: Float = 10000f
    }
    
    /**
     * Opacity tokens for consistent transparency.
     */
    object Opacity {
        /** 0.04 - Subtle hover state */
        const val hover: Float = 0.04f
        
        /** 0.08 - Subtle pressed state */
        const val pressed: Float = 0.08f
        
        /** 0.12 - Disabled state */
        const val disabled: Float = 0.12f
        
        /** 0.38 - Secondary content */
        const val secondary: Float = 0.38f
        
        /** 0.60 - Medium emphasis */
        const val medium: Float = 0.60f
        
        /** 0.87 - High emphasis */
        const val high: Float = 0.87f
    }
}
