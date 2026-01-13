package io.getsafenow.libraries.gsn_theme.customtheme.themeutils

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UIUserInterfaceStyle

@Composable
actual fun platformDynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme? = null

@Composable
actual fun platformStatusBarColorScheme(
    lightStatusBar: Boolean,
    darkTheme: Boolean,
    fallback: ColorScheme
): ColorScheme = fallback

@Composable
actual fun applyPlatformSystemBarsUpdate(
    statusBarScheme: ColorScheme,
    darkTheme: Boolean,
    lightStatusBar: Boolean
) {
    // No-op on iOS (Compose MPP). If you want, manage system bars in Swift/UIKit layer.
}



@Composable
actual fun PlatformRestoreSystemBarsOnDispose(
    currentScheme: ColorScheme,
    wasDarkTheme: Boolean,
) {
    val bg = uiColorFromArgb(currentScheme.background.toArgb())
    val on = uiColorFromArgb(currentScheme.onBackground.toArgb())

    // Apply while the forced-dark scope is active
    SideEffect {
        // UINavigationBar appearance
        val nav = UINavigationBarAppearance().apply {
            configureWithOpaqueBackground()
            backgroundColor = bg
            titleTextAttributes = mapOf(NSForegroundColorAttributeName to on)
            largeTitleTextAttributes = mapOf(NSForegroundColorAttributeName to on)
        }
        UINavigationBar.appearance().standardAppearance = nav
        UINavigationBar.appearance().scrollEdgeAppearance = nav
        UINavigationBar.appearance().tintColor = on

        // UITabBar appearance
        val tab = UITabBarAppearance().apply {
            configureWithOpaqueBackground()
            backgroundColor = bg
        }
        UITabBar.appearance().standardAppearance = tab
        if (isAtLeastIOS(15)) {
            UITabBar.appearance().scrollEdgeAppearance = tab
        }
        UITabBar.appearance().tintColor = on

        // Status bar: global hint via window interface style (best control is VC override)
        keyWindow()?.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleDark
    }

    // Restore on dispose
    DisposableEffect(Unit) {
        onDispose {
            val navDefault = UINavigationBarAppearance().apply {
                configureWithDefaultBackground()
            }
            UINavigationBar.appearance().standardAppearance = navDefault
            UINavigationBar.appearance().scrollEdgeAppearance = navDefault
            UINavigationBar.appearance().setTintColor(null)

            val tabDefault = UITabBarAppearance().apply {
                configureWithDefaultBackground()
            }
            UITabBar.appearance().standardAppearance = tabDefault
            if (isAtLeastIOS(15)) {
                UITabBar.appearance().scrollEdgeAppearance = tabDefault
            }
            UITabBar.appearance().setTintColor(null)

            keyWindow()?.overrideUserInterfaceStyle =
                UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            keyWindow()?.rootViewController?.setNeedsStatusBarAppearanceUpdate()
        }
    }
}