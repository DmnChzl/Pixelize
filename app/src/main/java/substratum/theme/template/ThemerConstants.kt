/**
 * Copyright (C) 2017 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package substratum.theme.template

object ThemerConstants {
    // Simple AntiPiracy Configuration
    val PIRACY_CHECK = false

    // Play Store AntiPiracy LVL Configurations (Relies On PIRACY_CHECK)
    internal val BASE_64_LICENSE_KEY = ""
    internal val APK_SIGNATURE_PRODUCTION = ""

    // AntiPiracy Library Configurations (Relies On PIRACY_CHECK)
    internal val ENFORCE_INTERNET_CHECK = false
    internal val ENFORCE_GOOGLE_PLAY_INSTALL = false
    internal val ENFORCE_AMAZON_APP_STORE_INSTALL = false

    // Theme Ready Google Apps Checker
    internal val THEME_READY_GOOGLE_APPS = false

    // Dynamic Filter That Only Works On Substratum 627+
    internal val SUBSTRATUM_FILTER_CHECK = false

    // Miscellaneous Checks
    val ENFORCE_MINIMUM_SUBSTRATUM_VERSION = true
    val MINIMUM_SUBSTRATUM_VERSION = 712 // 510 Is The Final MM Build
    val ENABLE_KNOWN_THIRD_PARTY_THEME_MANAGERS = BuildConfig.SUPPORTS_THIRD_PARTY_THEME_SYSTEMS

    // Blacklisted APKs To Prevent Theme Launching
    val ENABLE_BLACKLISTED_APPLICATIONS = false

    val BLACKLISTED_APPLICATIONS = arrayOf(
            "com.android.vending.billing.InAppBillingService.LOCK",
            "com.android.vending.billing.InAppBillingService.LACK",
            "cc.madkite.freedom",
            "zone.jasi2169.uretpatcher",
            "uret.jasi2169.patcher",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.forpda.lp",
            "com.android.vending.billing.InAppBillingService.LUCK",
            "com.android.protips",
            "com.android.vending.billing.InAppBillingService.CLON",
            "com.android.vendinc"
    )

    val OTHER_THEME_SYSTEMS = arrayOf(
            "com.slimroms.thememanager",
            "com.slimroms.omsbackend"
    )
}
